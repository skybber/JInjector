package org.hotswap.patcher.patch;

import java.util.List;

/**
 * The type Method patch.
 */
public class MethodPatch extends MethodPatchBase {
    private final String methodName;
    public MethodPatch(String methodName, boolean allMethods, List<String> paramClasses) {
        super(allMethods, paramClasses);
        this.methodName = methodName;
    }

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
