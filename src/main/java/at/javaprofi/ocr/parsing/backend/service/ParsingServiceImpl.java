package at.javaprofi.ocr.parsing.backend.service;

import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;

import at.javaprofi.ocr.frame.api.word.MethodContainer;
import at.javaprofi.ocr.io.api.dao.PathContainer;
import at.javaprofi.ocr.io.api.service.FileService;
import at.javaprofi.ocr.parsing.api.service.ParsingService;

@Service
public class ParsingServiceImpl implements ParsingService
{
    private static final Logger LOG = LoggerFactory.getLogger(ParsingServiceImpl.class);

    private static final String[] HEADERS_DURATION = {"duration", "class_name", "method_name"};
    private static final String[] HEADERS_MATCHED =
        {"duration", "class_name", "method_name", "x", "y", "height", "width"};

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
        final List<MethodContainer> methodContainerListFromExtractedLinesJSON;

        try
        {
            methodContainerListFromExtractedLinesJSON =
                createMethodContainerListFromExtractedLinesJSON(pathContainer);
        }
        catch (IOException e)
        {
            LOG.error("exception during reading extracted lines from json: {}", pathContainer.getExtractedLinesPath());

            throw new RuntimeException(e);
        }

        parseCodeFromSourceCodeAndMapMatchingMethodsAndWriteResultsToJSONFiles(pathContainer,
            methodContainerListFromExtractedLinesJSON);
    }

    private List<MethodContainer> createMethodContainerListFromExtractedLinesJSON(
        PathContainer pathContainer)
        throws IOException
    {
        final List<MethodContainer> extractedMethodContainerList = new ArrayList<>();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("extracted_lines.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray extractedLinesList = (JSONArray) obj;

            //Iterate over employee array
            extractedLinesList.forEach(extractedLine -> extractedMethodContainerList.add(
                parseMethodContainerObject((JSONObject) extractedLine)));

        }
        catch (ParseException e)
        {
            LOG.error("exception occured while reading extracted lines json: ", e);
        }

        return extractedMethodContainerList;

    }

    private MethodContainer parseMethodContainerObject(JSONObject extractedLinesJSON)
    {
        //Get employee object within list
        JSONArray extractedLinesForDuration = (JSONArray) extractedLinesJSON.get("wordList");

        final Long duration = (Long) extractedLinesJSON.get("duration");
        final MethodContainer methodContainer = new MethodContainer();

        methodContainer.setDuration(duration);

        for (Object rawLineObject : extractedLinesForDuration)
        {
            JSONObject extractedLineJSON = (JSONObject) rawLineObject;

            final String text = (String) extractedLineJSON.get("text");
            final Long width = (Long) extractedLineJSON.get("width");
            final Long height = (Long) extractedLineJSON.get("height");
            final Long x = (Long) extractedLineJSON.get("x");
            final Long y = (Long) extractedLineJSON.get("y");

            methodContainer.setExtractedLine(text);

            final Rectangle boundingBox =
                new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());

            methodContainer.setBoundingBox(boundingBox);
        }

        return methodContainer;
    }

    private void parseCodeFromSourceCodeAndMapMatchingMethodsAndWriteResultsToJSONFiles(PathContainer pathContainer,
        List<MethodContainer> extractedRawMethodContainerList)
    {
        final Map<String, List<String>> matchedClassesMethodMap =
            parseCodeFromGroundTruthAndBuildMapWithMatchingClassCandidates(extractedRawMethodContainerList);

        final List<MethodContainer> matchedMethodList =
            calculateMatchingSourceMethodsOfClassCandidates(extractedRawMethodContainerList, matchedClassesMethodMap);

        final List<MethodContainer> totalDurationMethodList =
            calculateTotalVisibilityDurationPerMatchedMethod(matchedMethodList);

        LOG.info("Writing matches to json");

        fileService.writeMethodContainerListToJSON(pathContainer.getMethodMatchesPath(),
            matchedMethodList,
            HEADERS_MATCHED);

        fileService.writeMethodContainerListToJSON(pathContainer.getTotalDurationPath(),
            totalDurationMethodList,
            HEADERS_DURATION);

        LOG.info("Finished writing json");

    }

    private Map<String, List<String>> parseCodeFromGroundTruthAndBuildMapWithMatchingClassCandidates(
        List<MethodContainer> extractedRawMethodContainerList)
    {

        final GenericVisitorAdapter<Map<String, List<String>>, Object> genericVisitorAdapter =
            new GenericVisitorAdapter<Map<String, List<String>>, Object>()
            {
                @Override
                public Map<String, List<String>> visit(ClassOrInterfaceDeclaration n, Object arg)
                {
                    final Map<String, List<String>> classMethodMap = new HashMap<>();

                    classMethodMap.putIfAbsent(n.getNameAsString(), n.getMethods()
                        .stream()
                        .map(method -> method.getDeclarationAsString(true, true, true))
                        .collect(
                            Collectors.toList()));

                    return classMethodMap;
                }
            };

        LOG.info("Reading and parsing java source code from ground truth");

        final Path pathToProjectRoot = Paths.get("../microservice-sales-app");
        final ProjectRoot projectRoot = new SymbolSolverCollectionStrategy().collect(pathToProjectRoot);

        final Map<String, List<String>> parsedMethodNamesPerClass = new HashMap<>();

        projectRoot.getSourceRoots().forEach(sourceRoot ->
        {
            try
            {
                sourceRoot.tryToParse();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            sourceRoot.getCompilationUnits()
                .forEach(unit -> {
                    final Map<String, List<String>> visit = genericVisitorAdapter.visit(unit, null);

                    if (visit != null)
                    {
                        parsedMethodNamesPerClass.putAll(visit);
                    }
                });
        });

        LOG.info("Finding class matching candidates and creating class/methods map for method name similarity search");

        final Map<String, List<String>> matchedClassesMethodMap = new HashMap<>();

        parsedMethodNamesPerClass.forEach((className, methodNames) ->
            methodNames.forEach(methodName ->
                extractedRawMethodContainerList.forEach(extractedMethodContainer ->
                {
                    final String extractedLine = extractedMethodContainer.getExtractedLine();

                    if (StringUtils.containsIgnoreCase(extractedLine, "class")
                        && StringUtils.containsIgnoreCase(extractedLine, className))
                    {
                        matchedClassesMethodMap.putIfAbsent(className, parsedMethodNamesPerClass.get(className));
                    }
                })));

        return matchedClassesMethodMap;
    }

    private List<MethodContainer> calculateTotalVisibilityDurationPerMatchedMethod(
        List<MethodContainer> matchedMethodList)
    {

        final Set<Pair<String, String>> classMethodPairSet = new HashSet<>();

        matchedMethodList.forEach(methodContainer -> {
            classMethodPairSet.add(Pair.of(methodContainer.getClassName(), methodContainer.getMethodName()));
        });

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
        Map<String, List<String>> matchedClassesMethodMap)
    {

        LOG.info(
            "Calculating similarity of extracted methods and parsed original methods of identified classes and add matches");

        extractedRawMethodContainerList.sort(Comparator.comparingLong(MethodContainer::getDuration));

        final List<MethodContainer> matchedMethodList = new ArrayList<>();
        matchedClassesMethodMap.forEach(
            (matchedSourceCodeClass, containingSourceCodeMethods) -> containingSourceCodeMethods.forEach(
                sourceCodeMethodName -> extractedRawMethodContainerList.forEach(extractedMethodContainer -> {
                    final String extractedLine = extractedMethodContainer.getExtractedLine();
                    if (StringUtils.isNotEmpty(extractedLine))
                    {

                        final String[] lineWords = StringUtils.split(extractedLine);
                        final List<String> lineWordsWithoutLineNumber = Arrays.stream(lineWords)
                            .filter(string -> !StringUtils.isNumericSpace(string)).collect(Collectors.toList());
                        final String extractedLineWithoutLineNumber = String.join(" ", lineWordsWithoutLineNumber);
                        final int posBlockOpen = StringUtils.lastIndexOf(extractedLineWithoutLineNumber, "{");
                        if (posBlockOpen != -1)
                        {
                            final String extractedPossibleMethodName =
                                StringUtils.substringBeforeLast(extractedLineWithoutLineNumber, "{");

                            final JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
                            final Double calculatedJaccardSimilarity =
                                jaccardSimilarity.apply(sourceCodeMethodName, extractedPossibleMethodName);

                            if (calculatedJaccardSimilarity != null && calculatedJaccardSimilarity > 0.96)
                            {

                                final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
                                final Double calculatedJaroWinklerSimilarity =
                                    jaroWinklerSimilarity.apply(sourceCodeMethodName, extractedPossibleMethodName);

                                if (calculatedJaroWinklerSimilarity != null && calculatedJaroWinklerSimilarity > 0.95)
                                {
                                    MethodContainer matchedMethodContainer = new MethodContainer();
                                    matchedMethodContainer.setMethodName(sourceCodeMethodName);
                                    matchedMethodContainer.setClassName(matchedSourceCodeClass);
                                    matchedMethodContainer.setDuration(extractedMethodContainer.getDuration());
                                    matchedMethodContainer.setBoundingBox(extractedMethodContainer.getBoundingBox());

                                    matchedMethodList.add(matchedMethodContainer);

                                }
                            }
                        }
                    }
                })));

        return matchedMethodList;
    }

}
