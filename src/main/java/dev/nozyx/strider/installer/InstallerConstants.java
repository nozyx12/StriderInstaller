/*
 * Copyright (C) 2026 Nozyx
 *
 * This file is part of StriderInstaller.
 *
 * StriderInstaller is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * StriderInstaller is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with StriderInstaller. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.nozyx.strider.installer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class InstallerConstants {

    private InstallerConstants() {}

    public static final String VERSION = "1.1.0";

    public static final Map<String, String[]> SUPPORTED_VERSIONS;

    static {
        Map<String, String[]> versions = new LinkedHashMap<>();

        versions.put("1.1.1", new String[]{
                "26.2",
                "26.1.2",
                "26.1.1",
                "26.1"
        });

        versions.put("1.1.0", new String[]{
                "26.2",
                "26.1.2",
                "26.1.1",
                "26.1"
        });

        versions.put("1.0.0", new String[]{
                "26.2",
                "26.1.2",
                "26.1.1",
                "26.1"
        });

        SUPPORTED_VERSIONS = Collections.unmodifiableMap(versions);
    }

    public static final String[] SUPPORTED_LOADER_VERSIONS =
            SUPPORTED_VERSIONS.keySet().toArray(String[]::new);
}
