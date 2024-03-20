/*
 * Copyright 2013-2024 the HotswapAgent authors.
 *
 * This file is part of HotswapAgent.
 *
 * HotswapPatcher is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 2 of the License, or (at your
 * option) any later version.
 *
 * HotswapAgent is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with HotswapAgent. If not, see http://www.gnu.org/licenses/.
 */
package org.hotswap.patcher;

import org.hotswap.patcher.javassist.*;
import org.hotswap.patcher.logging.AgentLogger;
import org.hotswap.patcher.patch.ClassPatch;
import org.hotswap.patcher.patch.ConstructorPatch;
import org.hotswap.patcher.patch.MethodPatch;
import org.hotswap.patcher.patch.MethodPatchFragment;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Patcher transformer.
 */
public class PatcherTransformer implements ClassFileTransformer {

    private static AgentLogger LOGGER = AgentLogger.getLogger(PatcherTransformer.class);

    private Map<String, ClassPatch> patches = new HashMap<>();

    public void addClassPatch(ClassPatch classPatch) {
        String resClassName = classPatch.getClassName().replaceAll("\\.", "/");
        patches.put(resClassName, classPatch);
    }

    @Override
    public byte[] transform(final ClassLoader classLoader, String className, Class<?> redefiningClass,
                            final ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {

        byte[] result = bytes;

        ClassPatch classPatch = patches.get(className);
        if (classPatch != null) {
            LOGGER.info("Transforming class= '{}'.", className);
            ClassPool classPool = new ClassPool();
            CtClass ctClass = null;
            try {
                classPool.appendSystemPath();
                classPool.appendClassPath(new LoaderClassPath(classLoader));
                ctClass = classPool.makeClass(new ByteArrayInputStream(bytes));
            } catch (Exception e) {
                LOGGER.error("Unable create CtClass for '" + className + "'.", e);
            }
            if (ctClass != null) {
                try {
                    for (List<ConstructorPatch> constructorPatchList : classPatch.getConstructorPatches().values()) {
                        for (ConstructorPatch constructorPatch : constructorPatchList) {
                            if (constructorPatch.getPatchFragments().isEmpty()) {
                                continue;
                            }
                            if (constructorPatch.isAllMethods()) {
                                applyAllConstructorPatch(classPool, ctClass, constructorPatch);
                            } else {
                                applyConstructorPatch(classPool, ctClass, constructorPatch);
                            }
                        }
                    }
                    for (List<MethodPatch> methodPatchList : classPatch.getMethodPatches().values()) {
                        for (MethodPatch methodPatch : methodPatchList) {
                            if (methodPatch.getPatchFragments().isEmpty()) {
                                continue;
                            }
                            if (methodPatch.isAllMethods()) {
                                applyAllMethodPatch(classPool, ctClass, methodPatch);
                            } else {
                                applyMethodPatch(classPool, ctClass, methodPatch);
                            }
                        }
                    }
                    result = ctClass.toBytecode();
                    ctClass.detach();
                } catch (Exception e) {
                    LOGGER.error("Patching class '" + className + "' failed.", e);
                }
            }
        }

        return result;
    }

    private void applyAllConstructorPatch(ClassPool classPool, CtClass ctClass, ConstructorPatch constructorPatch) {
        // TODO:
    }

    private void applyAllMethodPatch(ClassPool classPool, CtClass ctClass, MethodPatch methodPatch) {
        // TODO:
    }

    private void applyConstructorPatch(ClassPool classPool, CtClass ctClass, ConstructorPatch constructorPatch) throws NotFoundException, CannotCompileException {
        CtClass[] params = classNamesToCtClasses(classPool, constructorPatch.getParamClasses());
        CtConstructor ctConstructor = ctClass.getDeclaredConstructor(params);
        for (MethodPatchFragment patchFragment: constructorPatch.getPatchFragments()) {
            switch (patchFragment.getTransformType()) {
                case INSERT_BEFORE: {
                    ctConstructor.insertBefore(patchFragment.getCode());
                }
                break;
                case INSERT_AFTER: {
                    ctConstructor.insertAfter(patchFragment.getCode());
                }
                break;
                case SET_BODY: {
                    ctConstructor.setBody(patchFragment.getCode());
                }
                break;
            }
        }
    }

    private void applyMethodPatch(ClassPool classPool, CtClass ctClass, MethodPatch methodPatch) throws NotFoundException, CannotCompileException {
        CtClass[] params = classNamesToCtClasses(classPool, methodPatch.getParamClasses());
        CtMethod ctMethod = ctClass.getDeclaredMethod(methodPatch.getMethodName(), params);
        for (MethodPatchFragment patchFragment: methodPatch.getPatchFragments()) {
            switch (patchFragment.getTransformType()) {
                case INSERT_BEFORE: {
                    ctMethod.insertBefore(patchFragment.getCode());
                }
                break;
                case INSERT_AFTER: {
                    ctMethod.insertAfter(patchFragment.getCode());
                }
                break;
                case SET_BODY: {
                    ctMethod.setBody(patchFragment.getCode());
                }
                break;
            }
        }
    }

    private CtClass[] classNamesToCtClasses(ClassPool classPool, List<String> classNames) throws NotFoundException {
        CtClass[] result = new CtClass[classNames.size()];
        for (int i = 0; i < classNames.size(); i++) {
            result[i] = classPool.get(classNames.get(i));
        }
        return result;
    }
}
