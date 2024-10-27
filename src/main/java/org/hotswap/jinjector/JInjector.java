/*
 * Copyright 2013-2024 the HotswapAgent authors.
 *
 * This file is part of HotswapAgent.
 *
 * JInjector is free software: you can redistribute it and/or modify it
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
package org.hotswap.jinjector;

import com.sun.tools.attach.VirtualMachine;
import org.hotswap.jinjector.logging.AgentLogger;
import org.hotswap.jinjector.parser.PatchParser;
import org.hotswap.jinjector.patch.Patch;
import org.hotswap.jinjector.patch.Transform;
import org.hotswap.jinjector.util.Version;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;

public class JInjector {
    private static AgentLogger LOGGER = AgentLogger.getLogger(JInjector.class);
    private static List<Patch> patches = new ArrayList<>();

    public static void agentmain(String args, Instrumentation inst) {
        premain(args, inst);
    }

    public static void premain(String args, Instrumentation instrumentation) {
        LOGGER.info("Loading JInjector {} - runtime javassist class patching.", Version.version());
        parseArgs(args);

        JInjectorTransformer transformer = new JInjectorTransformer();
        for (Patch patch: patches) {
            for (Transform transform: patch.getTransforms()) {
                transformer.addTransform(transform);
            }
        }
        instrumentation.addTransformer(transformer, true);

        List<Class<?>> retransClasses = new ArrayList<>();
        for (Patch patch: patches) {
            for (Transform transform: patch.getTransforms()) {
                try {
                    Class<?> clazz = Class.forName(transform.getClassName());
                    retransClasses.add(clazz);
                } catch (ClassNotFoundException ignore) {
                }
            }
        }

        if (!retransClasses.isEmpty()) {
            try {
                LOGGER.info("Retransforming {}", retransClasses);
                instrumentation.retransformClasses(retransClasses.toArray(new Class[retransClasses.size()]));
            } catch (UnmodifiableClassException e) {
                LOGGER.error("Retransformation failed.", e);
            }
        }

        LOGGER.debug("JInjector initialized.");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java -jar jinjector.jar <-p patch.hswp>+ <targetPid>");
            return;
        }
        String targetPid = args[args.length-1]; // The PID of the target JVM process
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < args.length-1; i++) {
            params.append(args[i] + " ");
        }
        VirtualMachine vm = VirtualMachine.attach(targetPid);
        String agentPath = JInjector.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        vm.loadAgent(agentPath, params.toString().trim());
        vm.detach();
        System.out.println("JInjector loaded successfully.");
    }

    public static void parseArgs(String args) {
        if (args == null)
            return;

        args = args.trim();

        if (args.startsWith("-")) {
            String[] argsArr = args.split("\\s+");
            for (int i=0; i < argsArr.length-1; i+=2) {
                String param = argsArr[i];
                String paramValue = argsArr[i+1];
                if ("-p".equals(param)) {
                    PatchParser parser = new PatchParser();
                    Patch patch = parser.parseFile(paramValue);
                    if (patch != null) {
                        patches.add(patch);
                    }
                }
            }
        } else {
            for (String arg : args.split(",")) {
                String[] val = arg.split("=");
                if (val.length != 2) {
                    LOGGER.warning("Invalid javaagent command line argument '{}'. Argument is ignored.", arg);
                }

                String option = val[0];
                String optionValue = val[1];

                if ("patch".equals(option)) {
                    PatchParser parser = new PatchParser();
                    Patch patch = parser.parseFile(optionValue);
                    if (patch != null) {
                        patches.add(patch);
                    }
                } else {
                    LOGGER.warning("Invalid javaagent option '{}'. Argument '{}' is ignored.", option, arg);
                }
            }
        }
    }
}
