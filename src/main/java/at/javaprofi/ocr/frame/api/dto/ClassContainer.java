package at.javaprofi.ocr.frame.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO container with fields to be written to JSON files
 * which are processed by the frontend for the visualization of the containing data of this container (e.g. classes/methods visited by participant)
 */
public class ClassContainer
{
    private String fullyQualifiedClassName;
    private String packageName;
    private List<String> methodNameList = new ArrayList<>();
    private List<MethodContainer> methodContainerList = new ArrayList<>();
    private Long openedFrom;
    private Long closedAt;
    private String simpleClassName;

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public void setFullyQualifiedClassName(String fullyQualifiedClassName)
    {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    public String getFullyQualifiedClassName()
    {
        return fullyQualifiedClassName;
    }

    public List<String> getMethodNameList()
    {
        return methodNameList;
    }

    public void setMethodNameList(List<String> methodNameList)
    {
        this.methodNameList = methodNameList;
    }

    public List<MethodContainer> getMethodContainerList()
    {
        return methodContainerList;
    }

    public void setMethodContainerList(List<MethodContainer> methodContainerList)
    {
        this.methodContainerList = methodContainerList;
    }

    public Long getOpenedFrom()
    {
        return openedFrom;
    }

    public void setOpenedFrom(Long openedFrom)
    {
        this.openedFrom = openedFrom;
    }

    public Long getClosedAt()
    {
        return closedAt;
    }

    public void setClosedAt(Long closedAt)
    {
        this.closedAt = closedAt;
    }

    public void setSimpleClassName(String simpleClassName)
    {
        this.simpleClassName = simpleClassName;
    }

    public String getSimpleClassName()
    {
        return simpleClassName;
    }
}
