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

import org.hotswap.patcher.logging.AgentLogger;
import org.hotswap.patcher.parser.PatchParser;
import org.hotswap.patcher.patch.ClassPatch;
import org.hotswap.patcher.util.Version;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Hotswap patcher.
 */
public class HotswapPatcher {
    private static AgentLogger LOGGER = AgentLogger.getLogger(HotswapPatcher.class);

    private static List<String> patches = new ArrayList<>();

    public static void agentmain(String args, Instrumentation inst) {
        premain(args, inst);
    }

    public static void premain(String args, Instrumentation instrumentation) {
        LOGGER.info("Loading Hotswap Patcher {{}} - runtime javassist class patching.", Version.version());
        parseArgs(args);

        PatcherTransformer transformer = new PatcherTransformer();
        PatchParser parser = new PatchParser();
        List<ClassPatch> allPatches = new ArrayList<>();
        for (String patch: patches) {
            List<ClassPatch> classPatches = parser.parseFile(patch);
            if (classPatches != null) {
                for (ClassPatch classPatch: classPatches) {
                    LOGGER.info("Read classPatch {}", classPatch.toString());
                    transformer.addClassPatch(classPatch);
                    allPatches.add(classPatch);
                }

            }
        }
        instrumentation.addTransformer(transformer, true);

        List<Class<?>> retransformClasses = new ArrayList<>();
        for (ClassPatch classPatch: allPatches) {
            try {
                Class<?> clazz = Class.forName(classPatch.getClassName());
                retransformClasses.add(clazz);
            } catch (ClassNotFoundException ignore) {
            }
        }
        if (!retransformClasses.isEmpty()) {
            try {
                LOGGER.error("Retransforming {}", retransformClasses);
                instrumentation.retransformClasses(retransformClasses.toArray(new Class[retransformClasses.size()]));
            } catch (UnmodifiableClassException e) {
                LOGGER.error("Retransformation failed.", e);
            }
        }

        LOGGER.debug("Hotswap patcher initialized.");
    }

    public static void parseArgs(String args) {
        if (args == null)
            return;

        for (String arg : args.split(",")) {
            String[] val = arg.split("=");
            if (val.length != 2) {
                LOGGER.warning("Invalid javaagent command line argument '{}'. Argument is ignored.", arg);
            }

            String option = val[0];
            String optionValue = val[1];

            if ("patch".equals(option)) {
                patches.add(optionValue);
            } else {
                LOGGER.warning("Invalid javaagent option '{}'. Argument '{}' is ignored.", option, arg);
            }
        }
    }
}
