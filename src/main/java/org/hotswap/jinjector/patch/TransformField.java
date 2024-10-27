package org.hotswap.jinjector.patch;

public class TransformField {
    public enum FieldTransformType {
        RENAME,
        REMOVE
    }
    private final String fieldName;
    private final FieldTransformType fieldTransformType;
    private final String renameTo;

    public TransformField(String fieldName, FieldTransformType fieldTransformType, String renameTo) {
        this.fieldName = fieldName;
        this.fieldTransformType = fieldTransformType;
        this.renameTo = renameTo;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FieldTransformType getFieldTransformType() {
        return fieldTransformType;
    }

    public String getRenameTo() {
        return renameTo;
    }
}
