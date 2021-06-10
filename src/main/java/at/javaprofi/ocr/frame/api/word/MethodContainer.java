package at.javaprofi.ocr.frame.api.word;

import java.awt.*;

/**
 * POJO container with fields to be written to JSON files
 * which are processed by the frontend for the visualization of the containing data of this container (e.g. classes/methods visited by participant)
 */
public class MethodContainer
{
    private String className;
    private String methodName;
    private Long duration;
    private Rectangle boundingBox;
    private String extractedLine;

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getClassName()
    {
        return className;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    public Long getDuration()
    {
        return duration;
    }

    public void setDuration(Long duration)
    {
        this.duration = duration;
    }

    public String getExtractedLine()
    {
        return extractedLine;
    }

    public void setExtractedLine(String extractedLine)
    {
        this.extractedLine = extractedLine;
    }

    public Rectangle getBoundingBox()
    {
        return boundingBox;
    }

    public void setBoundingBox(Rectangle boundingBox)
    {
        this.boundingBox = boundingBox;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MethodContainer that = (MethodContainer) o;

        if (className != null ? !className.equals(that.className) : that.className != null)
            return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null)
            return false;
        if (duration != null ? !duration.equals(that.duration) : that.duration != null)
            return false;
        if (!boundingBox.equals(that.boundingBox))
            return false;
        return extractedLine != null ? extractedLine.equals(that.extractedLine) : that.extractedLine == null;
    }

    @Override
    public int hashCode()
    {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + boundingBox.hashCode();
        result = 31 * result + (extractedLine != null ? extractedLine.hashCode() : 0);
        return result;
    }
}
