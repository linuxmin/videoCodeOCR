package at.javaprofi.ocr.filestorage.api.service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import at.javaprofi.ocr.filestorage.api.dao.PathContainer;

public interface FileStorageService
{
    void init();

    void writeHocrToXML(String hocrString, Path hocrPath, Path framePath);

    void store(MultipartFile file);

    Stream<Path> loadFrames();

    Stream<Path> loadVideos();

    Path resolveVideoSourcePathFromFileName(String fileName);

    Resource loadAsResource(String fileName) throws MalformedURLException;

    void deleteAll();

    Stream<Path> retrieveContainingFilesAsPathStream(Path path);

    PathContainer createDirectoriesAndRetrievePathContainerFromVideoFileName(String fileName);
}
