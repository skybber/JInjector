package org.hotswap.patcher.patch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPatch {
    private String className;
    private final Map<String, MethodPatch> constructorPatches = new HashMap<>();
    private final Map<String, List<MethodPatch>> methodPatches = new HashMap<>();

    public ClassPatch(String className) {
       this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, MethodPatch> getConstructorPatches() {
        return constructorPatches;
    }

    public void addMethodPatch(MethodPatch methodPatch) {
        List<MethodPatch> methodPatchList = methodPatches.computeIfAbsent(methodPatch.getMethodName(), k->new ArrayList<>());
        methodPatchList.add(methodPatch);
    }
}
