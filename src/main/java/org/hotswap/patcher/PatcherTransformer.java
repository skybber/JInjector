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
import org.hotswap.patcher.patch.*;

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

    private Map<String, Transform> transforms = new HashMap<>();

    public void addTransform(Transform classPatch) {
        String resClassName = classPatch.getClassName().replaceAll("\\.", "/");
        transforms.put(resClassName, classPatch);
    }

    @Override
    public byte[] transform(final ClassLoader classLoader, String className, Class<?> redefiningClass,
                            final ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {

        byte[] result = bytes;

        Transform classTransform = transforms.get(className);
        if (classTransform != null) {
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
                    for (NewField newField: classTransform.getNewFields()) {
                        ctClass.addField(CtField.make(newField.getSrc(), ctClass));
                    }
                    for (NewMethod newConstructor: classTransform.getNewConstructors()) {
                       ctClass.addConstructor(CtNewConstructor.make(newConstructor.getSrc(), ctClass));
                    }
                    for (NewMethod newMethod: classTransform.getNewMethods()) {
                        ctClass.addMethod(CtNewMethod.make(newMethod.getSrc(), ctClass));
                    }
                    for (TransformField transfField: classTransform.getTransformFields().values()) {
                        applyFieldTransformer(classPool, ctClass, transfField);
                    }
                    for (TransformConstructor transfConstructor : classTransform.getTransformConstructors().values()) {
                        applyConstructorTransformer(classPool, ctClass, transfConstructor);
                    }
                    for (List<TransformMethod> tranfsMethods : classTransform.getTransformMethods().values()) {
                        for (TransformMethod transfMethod : tranfsMethods) {
                            applyMethodTransformer(classPool, ctClass, transfMethod);
                        }
                    }
                    result = ctClass.toBytecode();
                    ctClass.detach();
                    LOGGER.info("Class '{}' transformed.", className);
                } catch (Exception e) {
                    LOGGER.error("Transforming class '" + className + "' failed.", e);
                }
            }
        }

        return result;
    }


    private void applyFieldTransformer(ClassPool classPool, CtClass ctClass, TransformField transfField) throws NotFoundException {
        CtField ctField = ctClass.getDeclaredField(transfField.getFieldName());
        switch (transfField.getFieldTransformType()) {
            case RENAME: {
                ctField.setName(transfField.getRenameTo());
                break;
            }
            case REMOVE: {
                ctClass.removeField(ctField);
                break;
            }
        }
    }

    private void applyConstructorTransformer(ClassPool classPool, CtClass ctClass, TransformConstructor tranfsConstructor) throws NotFoundException, CannotCompileException {
        if (tranfsConstructor.isAllMethods()) {
            CtConstructor[] declaredConstructors = ctClass.getDeclaredConstructors();
            for (CtConstructor ctConstructor: declaredConstructors) {
                doApplyConstructorTransformer(ctConstructor, tranfsConstructor);
            }
        } else {
            CtClass[] params = classNamesToCtClasses(classPool, tranfsConstructor.getParamClasses());
            CtConstructor ctConstructor = ctClass.getDeclaredConstructor(params);
            doApplyConstructorTransformer(ctConstructor, tranfsConstructor);
        }
    }

    private void doApplyConstructorTransformer(CtConstructor ctConstructor, TransformConstructor tranfsConstructor) throws CannotCompileException {
        for (TransformMethodFragment patchFragment : tranfsConstructor.getPatchFragments()) {
            switch (patchFragment.getTransformType()) {
                case INSERT_BEFORE: {
                    ctConstructor.insertBefore(patchFragment.getSrc());
                }
                break;
                case INSERT_AFTER: {
                    ctConstructor.insertAfter(patchFragment.getSrc());
                }
                break;
                case SET_BODY: {
                    ctConstructor.setBody(patchFragment.getSrc());
                }
                break;
            }
        }
    }

    private void applyMethodTransformer(ClassPool classPool, CtClass ctClass, TransformMethod transfMethod) throws NotFoundException, CannotCompileException {
        if (transfMethod.isAllMethods()) {
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods(transfMethod.getMethodName());
            for (CtMethod ctMethod: declaredMethods) {
                doApplyMethodTransformer(ctMethod, transfMethod);
            }
        } else {
            CtClass[] params = classNamesToCtClasses(classPool, transfMethod.getParamClasses());
            CtMethod ctMethod = ctClass.getDeclaredMethod(transfMethod.getMethodName(), params);
            doApplyMethodTransformer(ctMethod, transfMethod);
        }
    }

    private void doApplyMethodTransformer(CtMethod ctMethod, TransformMethod transfMethod) throws CannotCompileException {
        for (TransformMethodFragment patchFragment : transfMethod.getPatchFragments()) {
            switch (patchFragment.getTransformType()) {
                case INSERT_BEFORE: {
                    ctMethod.insertBefore(patchFragment.getSrc());
                }
                break;
                case INSERT_AFTER: {
                    ctMethod.insertAfter(patchFragment.getSrc());
                }
                break;
                case SET_BODY: {
                    ctMethod.setBody(patchFragment.getSrc());
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
