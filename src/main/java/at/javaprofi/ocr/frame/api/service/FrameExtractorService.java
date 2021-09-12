package at.javaprofi.ocr.frame.api.service;

import java.awt.*;

public interface FrameExtractorService
{
    /**
     * extracts frames from video file and writes a [frame_timestamp_in_ms].jpeg file
     * to extracted-dir/filename for each extracted frame
     *
     * @param fileName    file name of the video
     * @param boundingBox
     */
    void extractFrames(String fileName, Rectangle boundingBox);
}
