package org.hotswap.patcher.patch;

import java.util.List;

public class MethodPatch extends MethodPatchBase {
    private final String methodName;
    public MethodPatch(String methodName, boolean allMethods, List<MethodParam> params) {
        super(allMethods, params);
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

}
