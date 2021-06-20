package at.javaprofi.ocr.io.api.dao;

import java.nio.file.Path;

/**
 * POJO container used to store needed paths for video processing
 */
public class PathContainer
{

    public static final class PathContainerBuilder
    {
        private Path videoPath;
        private Path framesPath;
        private Path visualizationPath;
        private Path extractedLinesPath;
        private Path methodMatchesPath;
        private Path totalDurationPath;

        public PathContainerBuilder()
        {
        }

        public PathContainer build()
        {
            return new PathContainer(this);
        }

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

        public PathContainerBuilder visualizationPath(Path visualizationPath)
        {
            this.visualizationPath = visualizationPath;
            return this;
        }

        public PathContainerBuilder extractedLinesPath(Path extractedLinesPath)
        {
            this.extractedLinesPath = extractedLinesPath;
            return this;
        }

        public PathContainerBuilder methodMatchesPath(Path methodMatchesPath)
        {
            this.methodMatchesPath = methodMatchesPath;
            return this;
        }

        public PathContainerBuilder totalDurationPath(Path totalDurationPath)
        {
            this.totalDurationPath = totalDurationPath;
            return this;
        }

    }

    private final Path videoPath;

    private final Path framesPath;

    private final Path visualizationPath;

    private final Path extractedLinesPath;

    private final Path methodMatchesPath;

    private final Path totalDurationPath;

    public PathContainer(PathContainerBuilder pathContainerBuilder)
    {
        this.videoPath = pathContainerBuilder.videoPath;
        this.framesPath = pathContainerBuilder.framesPath;
        this.visualizationPath = pathContainerBuilder.visualizationPath;
        this.extractedLinesPath = pathContainerBuilder.extractedLinesPath;
        this.methodMatchesPath = pathContainerBuilder.methodMatchesPath;
        this.totalDurationPath = pathContainerBuilder.totalDurationPath;
    }

    public Path getVideoPath()
    {
        return videoPath;
    }

    public Path getFramesPath()
    {
        return framesPath;
    }

    public Path getVisualizationPath()
    {
        return visualizationPath;
    }

    public Path getExtractedLinesPath()
    {
        return extractedLinesPath;
    }

    public Path getMethodMatchesPath()
    {
        return methodMatchesPath;
    }

    public Path getTotalDurationPath()
    {
        return totalDurationPath;
    }
}
