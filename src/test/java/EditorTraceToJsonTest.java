import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import at.javaprofi.ocr.frame.api.word.MethodContainer;

public class EditorTraceToJsonTest
{


    @Test
    public void readFileAndWriteToJson()
    {
        final String userRunDir = System.getProperties().getProperty("user.dir");
        final String pathToFile = userRunDir + "/src/test/resources/trace_editor.txt";
        final String pathToWrite = userRunDir + "/src/test/resources/";

        try
        {
            final List<String> strings = Files.readAllLines(Paths.get(pathToFile));

            for (String line : strings)
            {
                final String timeStamp = StringUtils.substringBefore(line, "{");
                final String jsonToWrite = StringUtils.substringAfter(line, timeStamp);
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonToWrite);
                final String s = jsonObject.toJSONString();
                System.out.println(s);
                final BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get(pathToWrite, timeStamp + ".json"));
                fileWriter.write(s);
                fileWriter.flush();

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Test
    public void readFromJson()
    {
        final String userRunDir = System.getProperties().getProperty("user.dir");
        final String pathToWrite = userRunDir + "/src/test/resources/";
        JSONParser jsonParser = new JSONParser();
        final List<String> visitedJavaClasses = new ArrayList<>();

        try (Stream<Path> pathsOfFiles = Files.walk(Paths.get(pathToWrite), 1))
        {
            pathsOfFiles.forEach(path -> {
                if(!Files.isDirectory(path)){
                    try (FileReader fileReader = new FileReader(path.toFile()))
                    {
                        final JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);
                        final String fullFileName = (String) jsonObject.get("fileName");
                        System.out.println(fullFileName);
                        final String fileName = StringUtils.substringAfterLast(fullFileName,"\\");
                        if(StringUtils.containsIgnoreCase(fullFileName,".java"))
                        {
                            final String javaClassFile = StringUtils.substringBefore(fileName, ".java");
                            visitedJavaClasses.add(javaClassFile);
                        }
                    }


                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }


            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}


