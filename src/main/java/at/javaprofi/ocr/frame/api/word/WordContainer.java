package at.javaprofi.ocr.frame.api.word;

import java.util.List;

import net.sourceforge.tess4j.Word;

public class WordContainer
{
    private long frameNumber;
    private List<Word> wordList;

    public long getFrameNumber()
    {
        return frameNumber;
    }

    public void setFrameNumber(long frameNumber)
    {
        this.frameNumber = frameNumber;
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
