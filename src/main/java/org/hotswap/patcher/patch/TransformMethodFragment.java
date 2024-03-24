package org.hotswap.patcher.patch;

public class TransformMethodFragment {
    public enum TransformType {
        INSERT_BEFORE,
        INSERT_AFTER,
        SET_BODY
    }

    private final TransformType transformType;
    private final String src;

    public TransformMethodFragment(TransformType transformType, String src) {
        this.transformType = transformType;
        this.src = src;
    }

    public TransformType getTransformType() {
        return transformType;
    }

    public String getSrc() {
        return src;
    }

    @Override
    public String toString() {
        return "MethodPatchFragment{" +
                "transformType=" + transformType +
                ", code='" + src + '\'' +
                '}';
    }
}
