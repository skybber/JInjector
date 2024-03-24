package org.hotswap.patcher.patch;

/**
 * The type New method patch.
 */
public class TransformNewMethod {
    private String addMethodPatch;

    public String getAddMethodPatch() {
        return addMethodPatch;
    }

    public void setAddMethodPatch(String addMethodPatch) {
        this.addMethodPatch = addMethodPatch;
    }

    @Override
    public String toString() {
        return "NewMethodPatch{" +
                "addMethodPatch='" + addMethodPatch + '\'' +
                '}';
    }
}
