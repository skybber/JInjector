package org.hotswap.jinjector.patch;

import java.util.List;

public class TransformConstructor extends TransformMethodBase {
    public TransformConstructor(boolean allMethods, List<String> paramClasses) {
        super(allMethods, paramClasses);
    }

    @Override
    public String getMethodName() {
        return "<init>";
    }
}
