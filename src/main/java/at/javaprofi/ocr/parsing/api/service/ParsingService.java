package at.javaprofi.ocr.parsing.api.service;

public interface ParsingService
{
    /**
     * parsing the original source code used within the recording and calculating matching classes/methods
     * writing matches to JSON files to be used for GUI visualization within front-end
     *
     * @param fileName file name of the video
     */
    void parsingOriginalSourceCodeAndWriteCalculatedMatchingLinesToJSONFiles(String fileName);
}
