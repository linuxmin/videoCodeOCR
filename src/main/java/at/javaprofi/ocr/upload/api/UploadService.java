package at.javaprofi.ocr.upload.api;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService
{
    void init();

    void saveHocrStringAsXML(String videoFileName, List<String> hocrList);

    void store(MultipartFile file);

    Stream<Path> loadFrames();

    Stream<Path> loadVideos();

    Path load(String fileName);

    Resource loadAsResource(String fileName) throws MalformedURLException;

    void deleteAll();

}
