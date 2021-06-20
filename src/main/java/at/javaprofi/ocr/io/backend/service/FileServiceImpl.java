package at.javaprofi.ocr.io.backend.service;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import at.javaprofi.ocr.frame.api.word.MethodContainer;
import at.javaprofi.ocr.io.api.StorageProperties;
import at.javaprofi.ocr.io.api.dao.PathContainer;
import at.javaprofi.ocr.io.api.service.FileService;

@Service
public class FileServiceImpl implements FileService
{

    private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);

    private final Path videoLocation;
    private final Path frameLocation;

    @Autowired
    public FileServiceImpl(StorageProperties properties)
    {
        this.videoLocation = Paths.get(properties.getVideoLocation());
        this.frameLocation = Paths.get(properties.getFrameLocation());
    }

    @Override
    public void storePupilFile(MultipartFile file, String videoFileName)
    {
        final String originalFilename = file.getOriginalFilename();
        LOG.info("uploading pupil data file {} for video {}", originalFilename, videoFileName);

        try
        {
            if (file.isEmpty())
            {
                throw new RuntimeException(videoFileName + "Cannot store empty file");
            }

            final PathContainer pathsForFile =
                createDirectoriesAndRetrievePathContainerFromVideoFileName(videoFileName);

            final Path destinationFile = Paths.get(pathsForFile.getVisualizationPath().toString(), originalFilename);

            if (Files.exists(destinationFile, LinkOption.NOFOLLOW_LINKS))
            {
                throw new RuntimeException(videoFileName + "File already exists and will not be overridden!");
            }

            try (InputStream inputStream = file.getInputStream())
            {
                Files.copy(inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING);
            }

        }
        catch (IOException e)
        {
            throw new RuntimeException(videoFileName + "Uploading pupil file failed!",e);
        }

        LOG.info("uploading {} successful", originalFilename);
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
                    originalFilename + "Uploading file outside io directory not allowed!");
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

            LOG.info("retrieving frame file list from path:  {}", pathToTraverse);
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
        LOG.info("retrieving and creating directories if not exist for file: {}", fileName);

        final Path videoFileName = Paths.get(fileName).getFileName();
        final Path videoPath = videoLocation.resolve(videoFileName);
        final Path framePath = frameLocation.resolve(FilenameUtils.removeExtension(videoFileName.toString()));
        final Path visualizationPath = framePath.resolve("vizData");
        final Path extractedLinesPath = Paths.get(visualizationPath.toString(), "extracted_lines.json");
        final Path methodMatchesPath = Paths.get(visualizationPath.toString(), "method_matches.json");
        final Path totalDurationPath = Paths.get(visualizationPath.toString(), "total_duration.json");

        try
        {
            if (Files.notExists(framePath.toAbsolutePath()))
            {
                Files.createDirectories(framePath.toAbsolutePath());

            }
            if (Files.notExists(visualizationPath))
            {
                Files.createDirectories(visualizationPath);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Exception while creating frame/json directories", e);
        }

        return new PathContainer.PathContainerBuilder().videoPath(videoPath)
            .framesPath(framePath)
            .visualizationPath(visualizationPath)
            .extractedLinesPath(extractedLinesPath)
            .methodMatchesPath(methodMatchesPath)
            .totalDurationPath(totalDurationPath)
            .build();
    }

    @Override
    public void writeMethodContainerListToJSON(Path visualizationPath, List<MethodContainer> matchedMethodList,
        String[] header)
    {

        try (FileWriter file = new FileWriter(visualizationPath.toFile()))
        {
            final JSONArray jsonMethodContainerList = new JSONArray();

            matchedMethodList.forEach(methodContainer -> {

                final JSONObject jsonMethodContainer = new JSONObject();
                jsonMethodContainer.put("duration_current", methodContainer.getDuration());

                jsonMethodContainer.put("class_name", methodContainer.getClassName());
                jsonMethodContainer.put("method_name", methodContainer.getMethodName());

                final Rectangle boundingBox = methodContainer.getBoundingBox();

                if (boundingBox != null)
                {
                    jsonMethodContainer.put("x_pos", boundingBox.x);
                    jsonMethodContainer.put("y_pos", boundingBox.y);
                    jsonMethodContainer.put("width_box", boundingBox.width);
                    jsonMethodContainer.put("height_box", boundingBox.height);
                }

                jsonMethodContainerList.add(jsonMethodContainer);

            });
            file.write(jsonMethodContainerList.toJSONString());
            file.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException("writing extracted lines to json failed", e);
        }

    }
}
