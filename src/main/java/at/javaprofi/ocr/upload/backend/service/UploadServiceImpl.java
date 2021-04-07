package at.javaprofi.ocr.upload.backend.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import at.javaprofi.ocr.upload.api.StorageProperties;
import at.javaprofi.ocr.upload.api.UploadService;

@Service
public class UploadServiceImpl implements UploadService
{

    private static final Logger LOG = LoggerFactory.getLogger(UploadServiceImpl.class);

    private final Path videoLocation;
    private final Path frameLocation;

    @Autowired
    public UploadServiceImpl(StorageProperties properties)
    {
        this.videoLocation = Paths.get(properties.getVideoLocation());
        this.frameLocation = Paths.get(properties.getFrameLocation());
    }

    @Override
    public void saveHocrStringAsXML(String videoFileName, List<String> hocrList)
    {

        for (int i = 0, frameHocrListSize = hocrList.size(); i < frameHocrListSize; i++)
        {
            String frame = hocrList.get(i);
            try
            {
                final Path path = frameLocation.resolve(
                    Paths.get(videoFileName + i + ".xml"))
                    .normalize().toAbsolutePath();

                final File file = path.toFile();
                FileUtils.writeStringToFile(file, frame,
                    "UTF-8");
            }
            catch (IOException e)
            {
                throw new RuntimeException(videoFileName + " Saving hocr xml failed! ", e);
            }

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
                throw new RuntimeException(originalFilename + "Uploading file outside upload directory not allowed!");
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
    public Path load(String fileName)
    {
        return videoLocation.resolve(fileName);
    }

    @Override
    public Resource loadAsResource(String fileName)
    {
        final Path file = load(fileName);
        try
        {
            return new UrlResource(file.toUri());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("Failed to load file:" + fileName + " as resource! ", e);
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
}
