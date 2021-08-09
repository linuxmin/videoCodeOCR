package at.javaprofi.ocr.recognition.api.service;

public interface RecognitionService
{
    /**
     * performing OCR with tesseract on already extracted *.jpeg frame files
     * and writing the resulting lines to a JSON file which is needed prior
     * calculating matching lines from the original source code
     *
     * @param fileName           file name of the video
     * @param hocr
     * @param trainedDataQuality
     */
    void extractTextFromFramesToJSON(String fileName, boolean hocr, String trainedDataQuality);
}
