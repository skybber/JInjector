package org.hotswap.patcher.patch;

import java.util.ArrayList;
import java.util.List;

public class MethodPatchBase {

    public static class MethodParam {
        private final String className;
        private final boolean isArray;

        public MethodParam(String className, boolean isArray) {

            this.className = className;
            this.isArray = isArray;
        }

        public String getClassName() {
            return className;
        }

        public boolean isArray() {
            return isArray;
        }
    }

    private final boolean allMethods;
    private final List<MethodPatch.MethodParam> params;
    private final List<MethodPatchFragment> patchFragments = new ArrayList<>();

    public MethodPatchBase(boolean allMethods, List<MethodParam> params) {
        this.allMethods = allMethods;
        this.params = params;
    }

    public boolean isAllMethods() {
        return allMethods;
    }

    public List<MethodParam> getParams() {
        return params;
    }

    public List<MethodPatchFragment> getPatchFragments() {
        return patchFragments;
    }

    public void addPatchFragment(MethodPatchFragment patchFragment) {
        patchFragments.add(patchFragment);
    }
}
