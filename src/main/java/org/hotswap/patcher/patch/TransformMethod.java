package org.hotswap.patcher.patch;

import java.util.List;

public class TransformMethod extends TransformMethodBase {
    private final String methodName;
    public TransformMethod(String methodName, boolean allMethods, List<String> paramClasses) {
        super(allMethods, paramClasses);
        this.methodName = methodName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return "MethodPatch{" +
                "methodName='" + methodName + '\'' +
                ", allMethods=" + isAllMethods() +
                ", paramClasses=" + getParamClasses() +
                ", patchFragments=" + getPatchFragments() +
                '}';
    }
}
