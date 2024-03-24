package org.hotswap.patcher.patch;

import java.util.ArrayList;
import java.util.List;

public abstract class TransformMethodBase {

    private final boolean allMethods;
    private final List<String> paramClasses;
    private final List<TransformMethodFragment> patchFragments = new ArrayList<>();

    public TransformMethodBase(boolean allMethods, List<String> paramClasses) {
        this.allMethods = allMethods;
        this.paramClasses = paramClasses;
    }

    public abstract String getMethodName();

    public boolean isAllMethods() {
        return allMethods;
    }

    public List<String> getParamClasses() {
        return paramClasses;
    }

    public List<TransformMethodFragment> getPatchFragments() {
        return patchFragments;
    }

    public void addPatchFragment(TransformMethodFragment patchFragment) {
        patchFragments.add(patchFragment);
    }

    public String getMethodSignature() {
        StringBuilder result = new StringBuilder();
        result.append(getMethodName());
        result.append("(");
        if (allMethods) {
            result.append("*");
        } else {
            if (!paramClasses.isEmpty()) {
                for (String paramClass: paramClasses) {
                    result.append(paramClass);
                    result.append(",");
                }
                result.setLength(result.length()-1);
            }
        }
        result.append(")");
        return result.toString();
    }
}
