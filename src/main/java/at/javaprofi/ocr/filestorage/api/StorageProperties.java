package at.javaprofi.ocr.filestorage.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

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

	public String getVideoLocation() {
		return videoLocation;
	}

	public void setVideoLocation(String videoLocation) {
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
