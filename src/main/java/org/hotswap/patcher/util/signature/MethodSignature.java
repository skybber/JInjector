package org.hotswap.patcher.util.signature;

import org.hotswap.patcher.patch.MethodPatch;

/**
 * The type Method signature.
 */
public class MethodSignature {
    public static String getMethodSignature(MethodPatch methodPatch) {
        StringBuilder result = new StringBuilder();
        result.append(methodPatch.getMethodName());
        result.append("(");
        if (methodPatch.isAllMethods()) {
            result.append("*");
        } else {
            if (!methodPatch.getParamClasses().isEmpty()) {
                for (String paramClass: methodPatch.getParamClasses()) {
                    result.append(paramClass);
                    result.append(",");
                }
                result.setLength(result.length()-1);
            }
        }
        result.append(")");
        return result.toString();
    }
}
