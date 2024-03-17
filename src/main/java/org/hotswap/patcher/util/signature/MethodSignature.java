package org.hotswap.patcher.util.signature;

import org.hotswap.patcher.patch.MethodPatch;

public class MethodSignature {
    public static String getMethodSignature(MethodPatch methodPatch) {
        StringBuilder result = new StringBuilder();
        result.append(methodPatch.getMethodName());
        result.append("(");
        if (methodPatch.isAllMethods()) {
            result.append("*");
        } else {
            if (methodPatch.getParams() != null && !methodPatch.getParams().isEmpty()) {
                for (MethodPatch.MethodParam methodParam: methodPatch.getParams()) {
                    result.append(methodParam.getClassName());
                    if (methodParam.isArray()) {
                        result.append("[]");
                    }
                }
                result.setLength(result.length()-1);
            }
        }
        result.append(")");
        return result.toString();
    }
}
