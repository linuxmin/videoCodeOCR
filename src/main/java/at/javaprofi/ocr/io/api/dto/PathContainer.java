package at.javaprofi.ocr.io.api.dto;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

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
        private Path traceEditorPath;

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

        public PathContainerBuilder traceEditorPath(Path traceEditorPath)
        {
            this.traceEditorPath = traceEditorPath;
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

    private final Path traceEditorPath;

    public PathContainer(PathContainerBuilder pathContainerBuilder)
    {
        this.videoPath = pathContainerBuilder.videoPath;
        this.framesPath = pathContainerBuilder.framesPath;
        this.visualizationPath = pathContainerBuilder.visualizationPath;
        this.extractedLinesPath = pathContainerBuilder.extractedLinesPath;
        this.methodMatchesPath = pathContainerBuilder.methodMatchesPath;
        this.totalDurationPath = pathContainerBuilder.totalDurationPath;
        this.traceEditorPath = pathContainerBuilder.traceEditorPath;
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

    public Path getTraceEditorPath()
    {
        return traceEditorPath;
    }

    public boolean isMicroService()
    {
        return videoPath != null && StringUtils.containsIgnoreCase(videoPath.getFileName().toString(), "micro");
    }
}
