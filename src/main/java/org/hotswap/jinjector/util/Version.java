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
package org.hotswap.jinjector.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Information about jinjector version.
 */
public class Version {

    public static String version() {
        try {
            Properties prop = new Properties();
            InputStream in = Version.class.getResourceAsStream("/version.properties");
            prop.load(in);
            in.close();

            return prop.getProperty("version") == null ? "unkown" : prop.getProperty("version");
        } catch (IOException e) {
            return "unknown";
        }
    }
}
