package at.javaprofi.ocr.parsing.backend.service;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import at.javaprofi.ocr.frame.api.dto.ClassContainer;
import at.javaprofi.ocr.frame.api.dto.MethodContainer;
import at.javaprofi.ocr.io.api.dto.PathContainer;
import at.javaprofi.ocr.io.api.service.FileService;
import at.javaprofi.ocr.parsing.api.ClassMethodVisitorAdapter;
import at.javaprofi.ocr.parsing.api.enums.ColorForTime;
import at.javaprofi.ocr.parsing.api.service.ParsingService;
import net.sourceforge.plantuml.SourceStringReader;

@Service
public class ParsingServiceImpl implements ParsingService
{
    private static final Logger LOG = LoggerFactory.getLogger(ParsingServiceImpl.class);

    private final FileService fileService;

    @Autowired
    public ParsingServiceImpl(FileService fileService)
    {
        this.fileService = fileService;
    }

    @Override
    public void parsingOriginalSourceCodeAndWriteCalculatedMatchingLinesToJSONFiles(String fileName)
    {
        final PathContainer pathContainer =
            fileService.createDirectoriesAndRetrievePathContainerFromVideoFileName(fileName);
        try
        {
            matchMethodContainerWithSourceCodeAndCreateVisualizationFiles(pathContainer,
                createMethodContainerListFromExtractedLinesJSON(pathContainer));
        }
        catch (IOException e)
        {
            LOG.error("exception during creating viz data from json: {}", pathContainer.getExtractedLinesPath(), e);
        }

    }

    private List<MethodContainer> createMethodContainerListFromExtractedLinesJSON(
        PathContainer pathContainer) throws IOException
    {
        final JSONArray extractedLinesJSONArray = fileService.readExtractedLinesFromJSON(pathContainer);
        final List<MethodContainer> extractedMethodContainerList = new ArrayList<>();

        if (extractedLinesJSONArray != null)
        {
            extractedLinesJSONArray.forEach(linesForDurationJSON -> extractedMethodContainerList.addAll(
                extractMethodContainerListFromDuration((JSONObject) linesForDurationJSON)));
        }

        return extractedMethodContainerList;
    }

    private List<MethodContainer> extractMethodContainerListFromDuration(JSONObject extractedLinesJSON)
    {
        final JSONArray extractedLinesForDuration = (JSONArray) extractedLinesJSON.get("wordList");

        final List<MethodContainer> methodContainerForDurationList = new ArrayList<>();
        final Long duration = (Long) extractedLinesJSON.get("duration");

        for (Object rawLineObject : extractedLinesForDuration)
        {
            JSONObject extractedLineJSON = (JSONObject) rawLineObject;
            final MethodContainer methodContainer = new MethodContainer();
            methodContainer.setDuration(duration);

            final String text = (String) extractedLineJSON.get("text");
            final Long width = (Long) extractedLineJSON.get("width");
            final Long height = (Long) extractedLineJSON.get("height");
            final Long x = (Long) extractedLineJSON.get("x");
            final Long y = (Long) extractedLineJSON.get("y");

            methodContainer.setExtractedLine(text);

            final Rectangle boundingBox =
                new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());

            methodContainer.setBoundingBox(boundingBox);
            methodContainerForDurationList.add(methodContainer);
        }

        return methodContainerForDurationList;
    }

    private void matchMethodContainerWithSourceCodeAndCreateVisualizationFiles(PathContainer pathContainer,
        List<MethodContainer> extractedRawMethodContainerList) throws IOException
    {
        final List<ClassContainer> visitedClassContainerList =
            parseCodeFromGroundTruthAndBuildMatchingClassContainerList(pathContainer);

        final List<MethodContainer> matchedMethodList =
            calculateMatchingSourceMethodsOfClassCandidates(extractedRawMethodContainerList, visitedClassContainerList);

        final List<MethodContainer> totalDurationMethodList =
            calculateTotalVisibilityDurationPerMatchedMethod(matchedMethodList);

        writePlantUMLFile(visitedClassContainerList, totalDurationMethodList, matchedMethodList);

        LOG.info("Writing matches to json");

        fileService.writeVisualizationDataToJSON(pathContainer,
            matchedMethodList, totalDurationMethodList
        );

        LOG.info("Finished writing json");
    }

    private void writePlantUMLFile(List<ClassContainer> visitedClassContainerList,
        List<MethodContainer> totalDurationMethodList,
        List<MethodContainer> matchedMethodList) throws IOException
    {
        totalDurationMethodList.forEach(methodContainer -> visitedClassContainerList.forEach(classContainer -> {
            if (StringUtils.equals(methodContainer.getClassName(), classContainer.getFullyQualifiedClassName()))
            {
                classContainer.getMethodContainerList().add(methodContainer);
            }
        }));

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("@startuml")
            .append('\n')
            .append("hide empty members")
            .append('\n')
            .append("skinparam roundcorner 20")
            .append('\n')
            .append("skinparam linetype ortho")
            .append('\n')
            .append("skinparam nodesep 200")
            .append('\n')
            .append("skinparam ranksep 200")
            .append('\n');

        buildPlantUmlClasses(visitedClassContainerList, totalDurationMethodList, stringBuilder);
        buildPlantUmlClassLinks(visitedClassContainerList, stringBuilder);
        buildPlantUmlMethodLinks(matchedMethodList, stringBuilder);
        stringBuilder.append("@enduml");

        final String plantUmlString = stringBuilder.toString();

        final SourceStringReader sourceStringReader = new SourceStringReader(plantUmlString);
        sourceStringReader.generateImage(new File("plantuml.png"));

        try (FileWriter file = new FileWriter("plantuml.txt"))
        {
            file.write(plantUmlString);
        }
    }

    private void buildPlantUmlMethodLinks(List<MethodContainer> matchedMethodList, StringBuilder stringBuilder)
    {
        String previousClass = null;
        String previousMethodAlias = null;

        final Map<Pair<String, String>, Integer> prevClassMethodCurrMethodPairLinkCuntMap = new HashMap<>();

        for (MethodContainer methodContainer : matchedMethodList)
        {
            final String currentClass = methodContainer.getClassName();
            final String currentMethodAlias =
                StringUtils.substringAfterLast(StringUtils.substringBefore(methodContainer.getMethodName(), "("), " ");

            if (previousClass != null && previousMethodAlias != null)
            {
                if (!StringUtils.equals(currentMethodAlias, previousMethodAlias) ||
                    !StringUtils.equals(currentClass, previousClass))
                {
                    if (StringUtils.equals(currentClass, previousClass))
                    {
                        final Pair<String, String> prevClassMethodCurrMethodPair =
                            Pair.of(StringUtils.remove(currentClass, ".") + ":" + previousMethodAlias,
                                currentMethodAlias);

                        if (!prevClassMethodCurrMethodPairLinkCuntMap.containsKey(prevClassMethodCurrMethodPair))
                        {
                            prevClassMethodCurrMethodPairLinkCuntMap.put(prevClassMethodCurrMethodPair, 1);
                        }
                        else
                        {
                            Integer currentValue =
                                prevClassMethodCurrMethodPairLinkCuntMap.get(prevClassMethodCurrMethodPair);
                            prevClassMethodCurrMethodPairLinkCuntMap.replace(prevClassMethodCurrMethodPair,
                                currentValue,
                                ++currentValue);
                        }
                    }
                    else
                    {
                        stringBuilder.append(StringUtils.remove(previousClass, "."))
                            .append("::")
                            .append(previousMethodAlias)
                            .append("->")
                            .append(StringUtils.remove(currentClass, "."))
                            .append("::").append(currentMethodAlias).append('\n');
                    }
                }
            }

            previousClass = currentClass;
            previousMethodAlias = currentMethodAlias;
        }

        prevClassMethodCurrMethodPairLinkCuntMap.forEach((previousCurrentMethodPair, linkCount) -> {
            final String previousMethod = previousCurrentMethodPair.getLeft();
            final String currentMethod = previousCurrentMethodPair.getRight();

            final String className = StringUtils.substringBefore(previousMethod, ":");
            final String previousMethodName = StringUtils.substringAfter(previousMethod, ":");
            stringBuilder.append(className)
                .append("::")
                .append(previousMethodName)
                .append("-[#black]>")
                .append(className)
                .append("::").append(currentMethod).append(" : x").append(linkCount).append('\n');
        });
    }

    private void buildPlantUmlClassLinks(List<ClassContainer> visitedClassContainerList, StringBuilder stringBuilder)
    {
        String previousClass = null;

        visitedClassContainerList.sort(Comparator.comparingLong(ClassContainer::getOpenedFrom));

        for (ClassContainer classContainer : visitedClassContainerList)
        {
            final String currentClass = StringUtils.remove(classContainer.getFullyQualifiedClassName(), ".");

            if (previousClass != null && !StringUtils.equals(currentClass, previousClass))
            {
                stringBuilder.append(previousClass)
                    .append("..>")
                    .append(currentClass)
                    .append(" :")
                    .append(classContainer.getClosedAt())
                    .append('\n');
            }

            previousClass = currentClass;
        }
    }

    private void buildPlantUmlClasses(List<ClassContainer> visitedClassContainerList,
        List<MethodContainer> totalDurationMethodList,
        StringBuilder stringBuilder)
    {
        final Map<String, List<ClassContainer>> classesPerPackageMap =
            visitedClassContainerList.stream().collect(Collectors.groupingBy(ClassContainer::getPackageName));

        final Set<String> fullyQualifiedClassNameSet = new HashSet<>();

        classesPerPackageMap.forEach((packageName, classContainerList) -> {
            stringBuilder.append("package ").append(packageName).append("{").append('\n');

            for (ClassContainer classContainer : classContainerList)
            {
                if (fullyQualifiedClassNameSet.add(classContainer.getFullyQualifiedClassName()))
                {
                    stringBuilder
                        .append("class ")
                        .append('"')
                        .append(classContainer.getSimpleClassName())
                        .append('"')
                        .append(" as ")
                        .append(StringUtils.remove(classContainer.getFullyQualifiedClassName(), "."))
                        .append(" #")
                        .append(ColorForTime.getColorForCurrentDuration(
                            classContainer.getOpenedFrom()))  //TODO: because this is a distinct list, only first opening/closing is visualized, bette solution?
                        .append("-")
                        .append(ColorForTime.getColorForCurrentDuration(classContainer.getClosedAt()))
                        .append(" {")
                        .append('\n');

                    //TODO: method container list contains method entry for multiple durations, plant uml can't handle this many entries - other solutions?
                    /*
                    classContainer.getMethodContainerList().forEach(
                        methodContainer -> stringBuilder.append("<back:")
                            .append(ColorForTime.getColorForCurrentDuration(methodContainer.getDuration()))
                            .append(">")
                            .append(methodContainer.getMethodName())
                            .append('\n'));

                     */

                    final Long minTotalDuration = totalDurationMethodList.stream()
                        .min(Comparator.comparingLong(MethodContainer::getDuration))
                        .map(
                            MethodContainer::getDuration)
                        .orElse(0L);

                    final Long maxTotalDuration = totalDurationMethodList.stream()
                        .max(Comparator.comparingLong(MethodContainer::getDuration))
                        .map(
                            MethodContainer::getDuration)
                        .orElse(0L);

                    classContainer.getMethodNameList().forEach(
                        method -> {
                            stringBuilder.append("..").append('\n');
                            totalDurationMethodList.stream()
                                .filter(methodContainer -> StringUtils.equals(methodContainer.getMethodName(), method)
                                    && StringUtils.equals(methodContainer.getClassName(),
                                    classContainer.getFullyQualifiedClassName()))
                                .findFirst()
                                .ifPresentOrElse(methodContainer -> stringBuilder.append("<size:")
                                    .append(scale(methodContainer.getDuration(), minTotalDuration, maxTotalDuration))
                                    .append(">"), () -> stringBuilder.append("<size:1>"));
                            stringBuilder
                                .append(method)
                                .append('\n');
                        });

                    stringBuilder
                        .append("}")
                        .append('\n');
                }
            }
            stringBuilder.append("}").append('\n');
        });
    }

    private long scale(final double valueIn, long minTotalDuration, long maxTotalDuration)
    {
        return Math.round((32 * (valueIn - minTotalDuration) / (maxTotalDuration - minTotalDuration)) + 8);
    }

    private List<ClassContainer> parseCodeFromGroundTruthAndBuildMatchingClassContainerList(
        PathContainer pathContainer) throws IOException
    {
        LOG.info("Finding class matching candidates and creating class/methods map for method name similarity search");

        final Map<String, List<String>> parsedMethodNamesPerClass = buildParsedClassMethodMap();
        final List<ClassContainer> visitedClassesFromTraceEditor =
            readVisitedClassesFromEditorTraceFiles(pathContainer);

        return addParsedMethodsToVisitedClasses(parsedMethodNamesPerClass, visitedClassesFromTraceEditor);
    }

    private Map<String, List<String>> buildParsedClassMethodMap() throws IOException
    {
        final GenericVisitorAdapter<Map<String, List<String>>, Object> classMethodVisitorAdapter =
            new ClassMethodVisitorAdapter();

        LOG.info("Reading and parsing java source code from ground truth");

        final Path pathToProjectRoot = Paths.get("../microservice-sales-app");
        final ProjectRoot projectRoot = new SymbolSolverCollectionStrategy().collect(pathToProjectRoot);

        final Map<String, List<String>> parsedMethodNamesPerClass = new HashMap<>();

        for (SourceRoot sourceRoot : projectRoot.getSourceRoots())
        {
            sourceRoot.tryToParse();

            for (CompilationUnit unit : sourceRoot.getCompilationUnits())
            {
                final Map<String, List<String>> classMethodMap = classMethodVisitorAdapter.visit(unit, null);

                if (classMethodMap != null)
                {
                    parsedMethodNamesPerClass.putAll(classMethodMap);
                }
            }
        }
        return parsedMethodNamesPerClass;
    }

    //TODO move file reading to file service
    private List<ClassContainer> readVisitedClassesFromEditorTraceFiles(
        PathContainer pathContainer)
    {

        final Path pathToFile = pathContainer.getTraceEditorPath();
        final List<ClassContainer> classContainerList = new ArrayList<>();

        try
        {
            final List<String> strings = Files.readAllLines(pathToFile);
            final JSONParser jsonParser = new JSONParser();

            for (String line : strings)
            {
                final String timeStamp = StringUtils.substringBefore(line, ":");
                final String jsonToWrite = StringUtils.substringAfter(line, ":");
                final JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonToWrite);

                jsonObject.put("opened", timeStamp);

                final String fullFileName = (String) jsonObject.get("fileName");

                if (StringUtils.containsIgnoreCase(fullFileName, ".java"))
                {
                    final String fullJavaFilePathWithoutExtension =
                        StringUtils.substringBefore(fullFileName, ".java");
                    final String subString =
                        StringUtils.substringAfter(fullJavaFilePathWithoutExtension, "java\\");
                    final String[] split = StringUtils.split(subString, "\\");
                    final String fullyQualifiedClassName = split != null ? StringUtils.join(split, '.') : "";
                    final String packageName = StringUtils.substringBeforeLast(fullyQualifiedClassName, ".");
                    final String simpleClassName = StringUtils.substringAfterLast(fullyQualifiedClassName, ".");
                    final ClassContainer classContainer = new ClassContainer();
                    final String opened = (String) jsonObject.get("opened");
                    classContainer.setOpenedFrom(Long.valueOf(opened));
                    final String closed = (String) jsonObject.get("closed");
                    classContainer.setClosedAt(Long.valueOf(closed));
                    classContainer.setFullyQualifiedClassName(fullyQualifiedClassName);
                    classContainer.setSimpleClassName(simpleClassName);
                    classContainer.setPackageName(packageName);
                    classContainerList.add(classContainer);
                }
            }
        }
        catch (IOException | NumberFormatException | ParseException e)
        {
            e.printStackTrace();
        }

        return classContainerList;
    }

    private List<ClassContainer> addParsedMethodsToVisitedClasses(Map<String, List<String>> parsedMethodNamesPerClass,
        List<ClassContainer> visitedClassesFromTraceEditor)
    {
        parsedMethodNamesPerClass.forEach((className, methodNames) ->
            methodNames.forEach(methodName ->
                visitedClassesFromTraceEditor.forEach(classContainer ->
                    {
                        if (StringUtils.equals(className, classContainer.getFullyQualifiedClassName()))
                        {
                            classContainer.setMethodNameList(parsedMethodNamesPerClass.get(className));
                        }
                    }
                )
            )
        );
        return visitedClassesFromTraceEditor;
    }

    private List<MethodContainer> calculateTotalVisibilityDurationPerMatchedMethod(
        List<MethodContainer> matchedMethodList)
    {

        final Set<Pair<String, String>> classMethodPairSet = matchedMethodList.stream()
            .map(methodContainer -> Pair.of(methodContainer.getClassName(), methodContainer.getMethodName()))
            .collect(Collectors.toSet());

        matchedMethodList.sort(Comparator.comparingLong(MethodContainer::getDuration));

        final Map<Long, Long> durationCountMap = matchedMethodList.stream()
            .collect(Collectors.groupingBy(MethodContainer::getDuration, Collectors.counting()));

        final List<MethodContainer> totalDurationMethodList = new ArrayList<>();
        long previousFrame;
        long totalDurationPerMethod;
        long containerCountForCurrentDuration;

        for (Pair<String, String> classMethodPair : classMethodPairSet)
        {
            previousFrame = 0L;
            totalDurationPerMethod = 0L;
            containerCountForCurrentDuration = 0L;

            final String className = classMethodPair.getLeft();
            final String methodName = classMethodPair.getRight();

            for (MethodContainer container : matchedMethodList)
            {
                long currentFrame = container.getDuration();

                containerCountForCurrentDuration =
                    containerCountForCurrentDuration == 0L ? durationCountMap.get(currentFrame) :
                        containerCountForCurrentDuration;

                containerCountForCurrentDuration--;

                if (StringUtils.equals(className, container.getClassName()) && StringUtils.equals(methodName,
                    container.getMethodName()))
                {
                    if (previousFrame != 0L)
                    {
                        totalDurationPerMethod = totalDurationPerMethod + (currentFrame - previousFrame);
                    }

                    previousFrame = currentFrame;
                }
                else if (containerCountForCurrentDuration == 0L && previousFrame != currentFrame)
                {
                    previousFrame = 0L;
                }
            }

            final MethodContainer totalDurationPerMethodContainer = new MethodContainer();
            totalDurationPerMethodContainer.setMethodName(methodName);
            totalDurationPerMethodContainer.setDuration(totalDurationPerMethod);
            totalDurationPerMethodContainer.setClassName(className);

            totalDurationMethodList.add(totalDurationPerMethodContainer);
        }

        totalDurationMethodList.sort(Comparator.comparingLong(MethodContainer::getDuration));
        return totalDurationMethodList;
    }

    private List<MethodContainer> calculateMatchingSourceMethodsOfClassCandidates(
        List<MethodContainer> extractedRawMethodContainerList,
        List<ClassContainer> matchedClassContainer)
    {

        LOG.info(
            "Calculating similarity of extracted methods and parsed original methods of identified classes and add matches");

        extractedRawMethodContainerList.sort(Comparator.comparingLong(MethodContainer::getDuration));

        final List<MethodContainer> matchedMethodList = new ArrayList<>();

        for (ClassContainer classContainer : matchedClassContainer)
        {
            for (String sourceCodeMethodName : classContainer.getMethodNameList())
            {
                for (MethodContainer extractedMethodContainer : extractedRawMethodContainerList)
                {
                    final String extractedLine = extractedMethodContainer.getExtractedLine();
                    if (StringUtils.isNotEmpty(extractedLine))
                    {
                        final String[] lineWords = StringUtils.split(extractedLine);
                        final List<String> lineWordsWithoutLineNumber = Arrays.stream(lineWords)
                            .filter(string -> !StringUtils.isNumericSpace(string)).collect(Collectors.toList());
                        final String extractedLineWithoutLineNumber = String.join(" ", lineWordsWithoutLineNumber);

                        final String sourceCodeMethodShort = StringUtils.substringBefore(sourceCodeMethodName, "(");
                        final String possibleMethodName =
                            StringUtils.substringBefore(extractedLineWithoutLineNumber, "(");
                        final Long openedFrom = classContainer.getOpenedFrom();
                        final Long closedAt = classContainer.getClosedAt();

                        final Long extractedDuration = extractedMethodContainer.getDuration();
                        if (StringUtils.containsIgnoreCase(possibleMethodName, sourceCodeMethodShort))
                        {
                            if ((extractedDuration >= openedFrom) && (extractedDuration <= closedAt))
                            {
                                final JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
                                final Double calculatedJaccardSimilarity =
                                    jaccardSimilarity.apply(sourceCodeMethodShort, possibleMethodName);

                                if (calculatedJaccardSimilarity != null && calculatedJaccardSimilarity > 0.96)
                                {
                                    final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
                                    final Double calculatedJaroWinklerSimilarity =
                                        jaroWinklerSimilarity.apply(sourceCodeMethodShort, possibleMethodName);

                                    if (calculatedJaroWinklerSimilarity != null
                                        && calculatedJaroWinklerSimilarity > 0.95)
                                    {
                                        MethodContainer matchedMethodContainer = new MethodContainer();
                                        matchedMethodContainer.setMethodName(sourceCodeMethodName);
                                        matchedMethodContainer.setClassName(
                                            classContainer.getFullyQualifiedClassName());
                                        matchedMethodContainer.setDuration(extractedDuration);
                                        matchedMethodContainer.setBoundingBox(
                                            extractedMethodContainer.getBoundingBox());

                                        matchedMethodList.add(matchedMethodContainer);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return matchedMethodList;
    }
}
