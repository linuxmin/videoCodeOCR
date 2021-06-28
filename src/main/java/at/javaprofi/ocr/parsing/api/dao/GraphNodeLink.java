package at.javaprofi.ocr.parsing.api.dao;

public class GraphNodeLink
{
    private String sourceClass;
    private Long sourceDuration;

    private String targetClass;
    private Long targetDuration;

    public String getSourceClass()
    {
        return sourceClass;
    }

    public void setSourceClass(String sourceClass)
    {
        this.sourceClass = sourceClass;
    }

    public Long getSourceDuration()
    {
        return sourceDuration;
    }

    public void setSourceDuration(Long sourceDuration)
    {
        this.sourceDuration = sourceDuration;
    }

    public String getTargetClass()
    {
        return targetClass;
    }

    public void setTargetClass(String targetClass)
    {
        this.targetClass = targetClass;
    }

    public Long getTargetDuration()
    {
        return targetDuration;
    }

    public void setTargetDuration(Long targetDuration)
    {
        this.targetDuration = targetDuration;
    }
}
