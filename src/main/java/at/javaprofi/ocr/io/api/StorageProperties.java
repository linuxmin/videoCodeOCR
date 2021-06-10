package at.javaprofi.ocr.io.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * properties containing root locations of data used by video processing requests
 */
@ConfigurationProperties("storage")
public class StorageProperties
{

    /**
     * Folder location for storing videos
     */
    private String videoLocation = "filestorage-dir";

    /**
     * Folder location for storing frames
     */
    private String frameLocation = "extracted-dir";

    public String getVideoLocation()
    {
        return videoLocation;
    }

    public void setVideoLocation(String videoLocation)
    {
        this.videoLocation = videoLocation;
    }

    public String getFrameLocation()
    {
        return frameLocation;
    }

    public void setFrameLocation(String frameLocation)
    {
        this.frameLocation = frameLocation;
    }

}
