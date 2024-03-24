package org.hotswap.patcher.patch;

import java.util.*;

/**
 * The type Class patch.
 */
public class Transform {
    private final String className;
    private final boolean onStart;
    private final Map<String, TransformField> transformFields = new LinkedHashMap<>();
    private final Map<String, TransformConstructor> transformConstructors = new LinkedHashMap<>();
    private final Map<String, List<TransformMethod>> transformMethods = new LinkedHashMap<>();
    private final List<NewField> newFields = new ArrayList<>();
    private final List<NewMethod> newConstructors = new ArrayList<>();
    private final List<NewMethod> newMethods = new ArrayList<>();

    public Transform(String className, boolean onStart) {
       this.className = className;
        this.onStart = onStart;
    }

    public String getClassName() {
        return className;
    }

    public boolean isOnStart() {
        return onStart;
    }

    public Map<String, TransformField> getTransformFields() {
        return transformFields;
    }

    public Map<String, TransformConstructor> getTransformConstructors() {
        return transformConstructors;
    }

    public Map<String , List<TransformMethod>> getTransformMethods() {
        return transformMethods;
    }

    public void addMethodPatch(TransformMethod methodPatch) {
        List<TransformMethod> methodPatchList = transformMethods.computeIfAbsent(methodPatch.getMethodName(), k->new ArrayList<>());
        methodPatchList.add(methodPatch);
    }

    public List<NewMethod> getNewConstructors() {
        return newConstructors;
    }

    public List<NewMethod> getNewMethods() {
        return newMethods;
    }

    public List<NewField> getNewFields() {
        return newFields;
    }

    public void addTransformField(TransformField transformField) {
        transformFields.put(transformField.getFieldName(), transformField);
    }

    public void addTransformConstructor(TransformConstructor transformConstructor) {
        String constructorSignature = transformConstructor.getMethodSignature();
        transformConstructors.put(constructorSignature, transformConstructor);
    }

    public void addTransformMethod(TransformMethod transformMethod) {
        String methodSignature = transformMethod.getMethodSignature();
        List<TransformMethod> transformMethodList = transformMethods.computeIfAbsent(methodSignature, sign -> new ArrayList<>());
        transformMethodList.add(transformMethod);
    }

    public void addNewField(NewField newField) {
        newFields.add(newField);
    }

    public void addNewConstructor(NewMethod newConstructor) {
        newConstructors.add(newConstructor);
    }

    public void addNewMethod(NewMethod newMethod) {
        newMethods.add(newMethod);
    }

}
