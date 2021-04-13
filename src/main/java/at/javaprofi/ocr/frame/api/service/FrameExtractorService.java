package at.javaprofi.ocr.frame.api.service;

import java.util.List;

import net.sourceforge.tess4j.Word;

public interface FrameExtractorService
{
    void extractCodeFromVideo(String fileName);

    List<Word> extractWordsFromVideo(String fileName);

}
