package org.hotswap.patcher.patch;

import java.util.List;

public class ConstructorPatch extends MethodPatchBase {
    public ConstructorPatch(boolean allMethods, List<MethodParam> params) {
        super(allMethods, params);
    }
}
