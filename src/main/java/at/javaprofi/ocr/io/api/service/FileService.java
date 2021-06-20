package at.javaprofi.ocr.io.api.service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import at.javaprofi.ocr.frame.api.word.MethodContainer;
import at.javaprofi.ocr.io.api.dao.PathContainer;

public interface FileService
{
    /**
     * creating root directories for video processing requests if not existing
     */
    void init();

    /**
     * store the uploaded file on the server
     *
     * @param file the uploaded file
     */
    void store(MultipartFile file);

    /**
     * fetching stream of uploaded file paths (videos)
     *
     * @return the stream of uploaded file paths
     */
    Stream<Path> loadVideos();

    /**
     * fetches the location path of a video
     *
     * @param fileName the file name of the video
     * @return @Link{Path} the path of the videe
     */
    Path resolveVideoSourcePathFromFileName(String fileName);

    /**
     * fetches the file found by the given file name as resource to be downloaded within the browser
     *
     * @param fileName
     * @return
     * @throws MalformedURLException
     */
    Resource loadAsResource(String fileName) throws MalformedURLException;

    /**
     * fetching stream of file paths contained in the given path
     *
     * @param path the path to be searched for containing paths (depth = 1, directories contained in input path are not searched for containing sub-paths)
     * @return the stream of paths found within given path
     */
    Stream<Path> retrieveContainingFilesAsPathStream(Path path);

    /**
     * creating the needed directory used for processing the video with the given input file name
     *
     * @param fileName the file name of the video
     * @return the path container with needed paths
     */
    PathContainer createDirectoriesAndRetrievePathContainerFromVideoFileName(String fileName);

    void writeMethodContainerListToJSON(Path visualizationPath, List<MethodContainer> matchedMethodList,
        String[] header);

    void storePupilFile(MultipartFile file, String fileName);
}
