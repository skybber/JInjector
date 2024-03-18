package org.hotswap.patcher.patch;

import java.util.List;

/**
 * The type Constructor patch.
 */
public class ConstructorPatch extends MethodPatchBase {
    public ConstructorPatch(boolean allMethods, List<String> paramClasses) {
        super(allMethods, paramClasses);
    }
}
