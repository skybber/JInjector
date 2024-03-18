package org.hotswap.patcher.patch;

import java.util.*;

/**
 * The type Class patch.
 */
public class ClassPatch {
    private String className;
    private final Map<String, List<ConstructorPatch>> constructorPatches = new LinkedHashMap<>();
    private final Map<String, List<MethodPatch>> methodPatches = new LinkedHashMap<>();

    public ClassPatch(String className) {
       this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, List<ConstructorPatch>> getConstructorPatches() {
        return constructorPatches;
    }

    public Map<String , List<MethodPatch>> getMethodPatches() {
        return methodPatches;
    }

    public void addMethodPatch(MethodPatch methodPatch) {
        List<MethodPatch> methodPatchList = methodPatches.computeIfAbsent(methodPatch.getMethodName(), k->new ArrayList<>());
        methodPatchList.add(methodPatch);
    }

    @Override
    public String toString() {
        return "ClassPatch{" +
                "className='" + className + '\'' +
                ", constructorPatches=" + constructorPatches +
                ", methodPatches=" + methodPatches +
                '}';
    }
}
