package at.javaprofi.ocr.filestorage.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import at.javaprofi.ocr.filestorage.api.StorageProperties;
import at.javaprofi.ocr.filestorage.api.dao.PathContainer;
import at.javaprofi.ocr.filestorage.api.service.FileStorageService;

@Service
public class FileStorageServiceImpl implements FileStorageService
{

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final Path videoLocation;
    private final Path frameLocation;

    @Autowired
    public FileStorageServiceImpl(StorageProperties properties)
    {
        this.videoLocation = Paths.get(properties.getVideoLocation());
        this.frameLocation = Paths.get(properties.getFrameLocation());
    }

    @Override
    public void writeHocrToXML(String hocrString, Path hocrPath, Path framePath)
    {
        try
        {
            final Path xmlFilePath = hocrPath.resolve(FilenameUtils.removeExtension(framePath.getFileName().toString()) + ".xml");
            Files.write(xmlFilePath, Collections.singleton(hocrString));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void store(MultipartFile file)
    {
        final String originalFilename = file.getOriginalFilename();
        LOG.info("uploading file {}", originalFilename);

        try
        {
            if (file.isEmpty())
            {
                throw new RuntimeException(originalFilename + "Cannot store empty file");
            }

            Path destinationFile = videoLocation.resolve(
                Paths.get(originalFilename))
                .normalize().toAbsolutePath();

            if (Files.exists(destinationFile, LinkOption.NOFOLLOW_LINKS))
            {
                throw new RuntimeException(originalFilename + "File already exists and will not be overridden!");
            }

            if (!destinationFile.getParent().equals(this.videoLocation.toAbsolutePath()))
            {
                throw new RuntimeException(
                    originalFilename + "Uploading file outside filestorage directory not allowed!");
            }
            try (InputStream inputStream = file.getInputStream())
            {
                Files.copy(inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING);
            }

        }
        catch (IOException e)
        {
            throw new RuntimeException(originalFilename + "Uploading file failed!");
        }

        LOG.info("uploading {} successful", file.getOriginalFilename());
    }

    @Override
    public Stream<Path> loadFrames()
    {
        try
        {
            LOG.debug("Loading frames from {}", frameLocation);

            return Files.walk(this.frameLocation, 1)
                .filter(path -> !path.equals(frameLocation))
                .map(frameLocation::relativize)
                .sorted(Comparator.comparingLong(this::getLastModified));
        }

        catch (IOException e)
        {
            LOG.error("Error loading frames from: {}", frameLocation);
            throw new RuntimeException("Failed to read stored frames! ", e);
        }
    }

    private long getLastModified(Path path)
    {
        return path.toFile().getAbsoluteFile()
            .lastModified();
    }

    @Override
    public Stream<Path> loadVideos()
    {
        try
        {
            LOG.debug("Loading videos from {}", videoLocation);
            return Files.walk(videoLocation, 1)
                .filter(path -> !path.equals(videoLocation))
                .map(videoLocation::relativize);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read stored videos! ", e);
        }
    }

    @Override
    public Path resolveVideoSourcePathFromFileName(String fileName)
    {
        return videoLocation.resolve(fileName);
    }

    @Override
    public Resource loadAsResource(String fileName)
    {
        final Path file = resolveVideoSourcePathFromFileName(fileName);
        try
        {
            return new UrlResource(file.toUri());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(
                "Failed to resolveVideoSourcePathFromFileName file:" + fileName + " as resource! ", e);
        }
    }

    @Override
    public void deleteAll()
    {
        LOG.info("Deleting all files... ");
        FileSystemUtils.deleteRecursively(videoLocation.toFile());
    }

    @Override
    public void init()
    {
        try
        {
            Files.createDirectories(videoLocation);
            Files.createDirectories(frameLocation);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not initialize storage! ", e);
        }
    }

    @Override
    public Stream<Path> retrieveContainingFilesAsPathStream(Path pathToTraverse)
    {
        {
            try
            {
                return Files.walk(pathToTraverse, 1)
                    .filter(path -> !Files.isDirectory(path));

            }
            catch (IOException e)
            {
                throw new RuntimeException("Could not retrieve files from path!", e);
            }
        }
    }

    @Override
    public PathContainer createDirectoriesAndRetrievePathContainerFromVideoFileName(String fileName)
    {
        final Path videoFileName = Paths.get(fileName).getFileName();
        final Path videoPath = videoLocation.resolve(videoFileName);
        final Path framePath = frameLocation.resolve(FilenameUtils.removeExtension(videoFileName.toString()));
        final Path hocrPath = framePath.resolve("hocr");

        try
        {
            Files.createDirectories(framePath.toAbsolutePath());
            Files.createDirectories(hocrPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Exception while creating frame/hocr directories", e);
        }

        return new PathContainer.PathContainerBuilder().videoPath(videoPath)
            .framesPath(framePath)
            .hocrPath(hocrPath).build();
    }

}
