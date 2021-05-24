import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.junit.Test;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import at.javaprofi.ocr.frame.api.word.MethodContainer;

public class SrcmlTest
{

    private static final String[] HEADERS = {"duration", "class", "method", "x", "y"};

    @Test
    public void generateSrcml()
    {

        try
        { //this is the file which we want to get its xml representation
            String javaFileName = "extracted-dir/capturescreenOld/hocr/";
            //use ProcessBuilder class and give it the arguements
            ProcessBuilder processBuilder =
                new ProcessBuilder("srcml", "-l", "Java", "--position", javaFileName, "-o", "srcml.xml");
            //spicify the directory of [srcml.exe]
            //processBuilder.directory(new File("/usr/local/bin"));
            //create the process
            Process process = processBuilder.start();
            //let's read the output of this process[ie: the xml data]
            InputStream inputStream = process.getInputStream();
            int i;
            StringBuilder xmlData = new StringBuilder();
            StringBuilder errorData = new StringBuilder();
            while ((i = inputStream.read()) != -1)
            {
                xmlData.append((char) i);
            }

            while ((i = process.getErrorStream().read()) != -1)
            {
                errorData.append((char) i);
            }
            System.out.println(xmlData.toString());
            System.out.println(errorData.toString());
        }
        catch (IOException e)
        {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    @Test
    public void testJavaParserSourceRoot() throws Exception
    {
        final Path pathToSource = Paths.get("../microservice-sales-app/banking-app/src");

        final VoidVisitorAdapter<Void> visitorAdapter = new VoidVisitorAdapter<Void>()
        {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg)
            {
                super.visit(n, arg);
                System.out.println("Class or Interface: " + n.getName());
            }
        };

        final SourceRoot sourceRoot = new SourceRoot(pathToSource);
        sourceRoot.tryToParse();
        sourceRoot.getCompilationUnits().forEach(unit -> visitorAdapter.visit(unit, null));

    }

    @Test
    public void testFindMethodNamesFromCSV() throws Exception
    {
        final Path pathToProjectRoot = Paths.get("../microservice-sales-app");

        final GenericListVisitorAdapter<String, Object> genericListVisitorAdapter =
            new GenericListVisitorAdapter<String, Object>()
            {
                @Override
                public List<String> visit(ClassOrInterfaceDeclaration n, Object arg)
                {

                    return n.getMethods()
                        .stream()
                        .map(method -> method.getDeclarationAsString(true, true, true))
                        .collect(
                            Collectors.toList());
                }
            };

        final List<String> lineStringList = new ArrayList<>();
        final List<String> foundMethodNameList = new ArrayList<>();
        final Set<String> matchedMethods = new TreeSet<>();

        final CSVParser csvParser = new CSVParser(new FileReader("capture_wholeLines.csv"), CSVFormat.DEFAULT);
        csvParser.getRecords().forEach(record -> lineStringList.add(record.get(3)));

        final ProjectRoot projectRoot = new SymbolSolverCollectionStrategy().collect(pathToProjectRoot);
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
                .forEach(unit -> foundMethodNameList.addAll(genericListVisitorAdapter.visit(unit, null)));

            foundMethodNameList.forEach(methodName ->
                lineStringList.forEach(line ->
                {
                    final String[] lineWords = StringUtils.split(line);
                    final List<String> wordsInLineWithoutNumber = Arrays.stream(lineWords)
                        .filter(string -> !StringUtils.isNumericSpace(string)).collect(Collectors.toList());

                    final String lineWithoutNumber = String.join(" ", wordsInLineWithoutNumber);
                    final int posBlockOpen = StringUtils.lastIndexOf(lineWithoutNumber, "{");

                    if (posBlockOpen != -1)
                    {
                        final String methodNameFromLineSubstring =
                            StringUtils.substringBeforeLast(lineWithoutNumber, "{");
                        final JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
                        final Double jaccardResult = jaccardSimilarity.apply(methodName, methodNameFromLineSubstring);

                        if (jaccardResult != null && jaccardResult > 0.96)
                        {
                            final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
                            final Double jws = jaroWinklerSimilarity.apply(methodName, methodNameFromLineSubstring);

                            if (jws != null && jws > 0.95)
                            {
                                matchedMethods.add(methodName);
                            }
                        }
                    }
                }));
        });

        matchedMethods.forEach(System.out::println);
    }

    /*
    @Test
    public void testFindMethodNamesFromCSVWithClassNames() throws Exception
    {




        final Path pathToProjectRoot = Paths.get("../microservice-sales-app");

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

        final List<MethodContainer> extractedMethodContainerList = new ArrayList<>();
        final Map<String, List<String>> parsedMethodNamesPerClass = new HashMap<>();
        final Map<String, List<String>> matchedClassesMethodMap = new HashMap<>();

        final Set<MethodContainer> matchedMethods =
            new TreeSet<>(Comparator.comparingLong(MethodContainer::getDuration));

        final CSVParser csvParser = new CSVParser(new FileReader("capture_wholeLines.csv"), CSVFormat.DEFAULT);

        System.out.println("Reading extracted source code from capture_wholeLines.csv");
        csvParser.getRecords().forEach(record ->
        {
            final MethodContainer methodContainer = new MethodContainer();
            final String durationString = record.get(0);

            if (StringUtils.isNumericSpace(durationString))
            {
                methodContainer.setDuration(Long.parseLong(durationString));
            }

            final String xString = record.get(1);
            final String yString = record.get(2);

            if (StringUtils.isNumericSpace(xString) && StringUtils.isNumericSpace(yString))
            {
                final Rectangle boundingBox = new Rectangle();
                boundingBox.setLocation(Integer.parseInt(xString), Integer.parseInt(yString));
                methodContainer.setBoundingBox(boundingBox);
            }

            methodContainer.setExtractedLine(record.get(3));
            extractedMethodContainerList.add(methodContainer);
        });

        final ProjectRoot projectRoot = new SymbolSolverCollectionStrategy().collect(pathToProjectRoot);

        System.out.println("Reading and parsing java source code from microservice-sales-app project");
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

        System.out.println(
            "Finding matching class candidates and creating class/methods map for method name similarity search");

        parsedMethodNamesPerClass.forEach((className, methodNames) ->
            methodNames.forEach(methodName ->
                extractedMethodContainerList.forEach(extractedRawMethodContainer ->
                {
                    final String extractedLine = extractedRawMethodContainer.getExtractedLine();

                    if (StringUtils.containsIgnoreCase(extractedLine, "class")
                        && StringUtils.containsIgnoreCase(extractedLine, className))
                    {
                        matchedClassesMethodMap.putIfAbsent(className, parsedMethodNamesPerClass.get(className));
                    }
                })));

        System.out.println(
            "Calculating similarity of extracted methods and parsed original methods of identified classes and add matches");

        matchedClassesMethodMap.forEach((matchedClass, containingMethods) -> {
            containingMethods.forEach(methodName ->
                extractedMethodContainerList.forEach(extractedRawMethodContainer ->
                {
                    final String extractedLine = extractedRawMethodContainer.getExtractedLine();

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
                            jaccardSimilarity.apply(methodName, extractedPossibleMethodName);

                        if (calculatedJaccardSimilarity != null && calculatedJaccardSimilarity > 0.96)
                        {
                            final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
                            final Double calculatedJaroWinklerSimilarity =
                                jaroWinklerSimilarity.apply(methodName, extractedPossibleMethodName);

                            if (calculatedJaroWinklerSimilarity != null && calculatedJaroWinklerSimilarity > 0.95)
                            {
                                MethodContainer matchedMethodContainer = new MethodContainer();
                                matchedMethodContainer.setMethodName(methodName);
                                matchedMethodContainer.setClassName(matchedClass);
                                matchedMethodContainer.setDuration(extractedRawMethodContainer.getDuration());
                                matchedMethodContainer.setX(extractedRawMethodContainer.getX());
                                matchedMethodContainer.setY(extractedRawMethodContainer.getY());
                                matchedMethods.add(matchedMethodContainer);
                            }
                        }
                    }
                }));
        });

        FileWriter out = null;

        try
        {
            out = new FileWriter("sorted_classes_methods.csv");

            System.out.println("Write matches to sorted_classes_methods.csv");

            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS)))
            {
                matchedMethods.forEach((methodContainer) -> {
                    try
                    {
                        printer.printRecord(methodContainer.getDuration(), methodContainer.getClassName(),
                            methodContainer.getMethodName(),
                            methodContainer.getX(),
                            methodContainer.getY());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                });
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

     */
}


