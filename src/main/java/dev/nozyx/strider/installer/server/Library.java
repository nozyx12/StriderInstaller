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

package dev.nozyx.strider.installer.server;

public record Library(
        String path,
        String url,
        String sha1
) {
}
