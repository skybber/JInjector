package org.hotswap.patcher.patch;

public class MethodPatchFragment {
    public enum TransformType {
        INSERT_BEFORE,
        INSERT_AFTER
    }

    private final TransformType transformType;
    private final String code;

    public MethodPatchFragment(TransformType transformType, String code) {
        this.transformType = transformType;
        this.code = code;
    }

    public TransformType getTransformType() {
        return transformType;
    }

    public String getCode() {
        return code;
    }
}
