package at.javaprofi.ocr.filestorage.api.dao;

import java.nio.file.Path;

public class PathContainer
{

    public static final class PathContainerBuilder
    {
        private Path videoPath;
        private Path framesPath;
        private Path hocrPath;

        public PathContainerBuilder videoPath(Path videoPath)
        {
            this.videoPath = videoPath;
            return this;
        }

        public PathContainerBuilder framesPath(Path framesPath)
        {
            this.framesPath = framesPath;
            return this;
        }

        public PathContainerBuilder hocrPath(Path hocrPath)
        {
            this.hocrPath = hocrPath;
            return this;
        }

        public PathContainer build()
        {
            return new PathContainer(this);
        }
    }

    private final Path videoPath;

    private final Path framesPath;

    private final Path hocrPath;

    public PathContainer(PathContainerBuilder pathContainerBuilder)
    {
        this.videoPath = pathContainerBuilder.videoPath;
        this.framesPath = pathContainerBuilder.framesPath;
        this.hocrPath = pathContainerBuilder.hocrPath;
    }

    public Path getVideoPath()
    {
        return videoPath;
    }

    public Path getFramesPath()
    {
        return framesPath;
    }

    public Path getHocrPath()
    {
        return hocrPath;
    }

}
