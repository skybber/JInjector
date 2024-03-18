package org.hotswap.patcher.patch;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Method patch base.
 */
public class MethodPatchBase {

    private final boolean allMethods;
    private final List<String> paramClasses;
    private final List<MethodPatchFragment> patchFragments = new ArrayList<>();

    public MethodPatchBase(boolean allMethods, List<String> paramClasses) {
        this.allMethods = allMethods;
        this.paramClasses = paramClasses;
    }

    public boolean isAllMethods() {
        return allMethods;
    }

    public List<String> getParamClasses() {
        return paramClasses;
    }

    public List<MethodPatchFragment> getPatchFragments() {
        return patchFragments;
    }

    public void addPatchFragment(MethodPatchFragment patchFragment) {
        patchFragments.add(patchFragment);
    }

}
