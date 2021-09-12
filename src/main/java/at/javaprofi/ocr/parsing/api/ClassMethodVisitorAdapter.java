package at.javaprofi.ocr.parsing.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class ClassMethodVisitorAdapter extends GenericVisitorAdapter<Map<String, List<String>>, Object>
{
    @Override
    public Map<String, List<String>> visit(ClassOrInterfaceDeclaration n, Object arg)
    {
        final Map<String, List<String>> classMethodMap = new HashMap<>();
        classMethodMap.putIfAbsent(n.getFullyQualifiedName().orElse(null), n.getMethods()
            .stream()
            .map(methodDeclaration -> methodDeclaration.getDeclarationAsString(true, true, true))
            .collect(
                Collectors.toList()));

        return classMethodMap;
    }
}
