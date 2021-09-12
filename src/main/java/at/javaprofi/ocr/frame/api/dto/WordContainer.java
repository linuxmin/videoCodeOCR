package at.javaprofi.ocr.frame.api.dto;

import java.util.List;

import net.sourceforge.tess4j.Word;

/**
 * POJO container with fields to be written to a JSON file,
 * containing the result of the tesseract OCR processing
 */
public class WordContainer
{
    private long duration;

    private List<Word> wordList;

    public long getDuration()
    {
        return duration;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public List<Word> getWordList()
    {
        return wordList;
    }

    public void setWordList(List<Word> wordList)
    {
        this.wordList = wordList;
    }
}
