/*
 * Copyright (C) 2026 Nozyx
 *
 * This file is part of StriderInstaller.
 *
 * StriderInstaller is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

public class MinecraftServerDownloader {

    private static final String VERSION_MANIFEST =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public MinecraftServerDownloader() {
        httpClient =
                HttpClient.newHttpClient();

        mapper =
                new ObjectMapper();
    }

    public Path download(
            String minecraftVersion,
            Path serverFolder
    ) throws Exception {

        JsonNode version =
                findVersion(
                        minecraftVersion
                );

        JsonNode server =
                version
                        .path("downloads")
                        .path("server");

        if (server.isMissingNode()) {
            throw new IOException(
                    "Minecraft version "
                            + minecraftVersion
                            + " does not provide a server download."
            );
        }

        String url =
                server
                        .path("url").asString();

        String expectedSha1 =
                server
                        .path("sha1").asString();

        if (url.isBlank()) {
            throw new IOException(
                    "Minecraft server download URL is missing."
            );
        }

        if (expectedSha1.isBlank()) {
            throw new IOException(
                    "Minecraft server SHA-1 is missing."
            );
        }

        Path serverJar =
                serverFolder.resolve(
                        "server.jar"
                );

        downloadFile(
                url,
                serverJar
        );

        String actualSha1 =
                sha1(serverJar);

        if (!expectedSha1.equalsIgnoreCase(actualSha1)) {
            Files.deleteIfExists(
                    serverJar
            );

            throw new IOException(
                    "Downloaded Minecraft server is corrupted."
                            + "\nExpected SHA-1: "
                            + expectedSha1
                            + "\nActual SHA-1: "
                            + actualSha1
            );
        }

        return serverJar;
    }

    private JsonNode findVersion(
            String minecraftVersion
    ) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder(
                                URI.create(
                                        VERSION_MANIFEST
                                )
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Failed to download Mojang version manifest: HTTP "
                            + response.statusCode()
            );
        }

        JsonNode manifest =
                mapper.readTree(
                        response.body()
                );

        for (JsonNode version :
                manifest.path("versions")) {

            if (minecraftVersion.equals(
                    version.path("id").asString()
            )) {

                return loadVersionMetadata(
                        version.path("url").asString()
                );
            }
        }

        throw new IOException(
                "Minecraft version not found: "
                        + minecraftVersion
        );
    }

    private JsonNode loadVersionMetadata(
            String url
    ) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder(
                                URI.create(url)
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Failed to download Minecraft version metadata: HTTP "
                            + response.statusCode()
            );
        }

        return mapper.readTree(
                response.body()
        );
    }

    private void downloadFile(
            String url,
            Path destination
    ) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder(
                                URI.create(url)
                        )
                        .GET()
                        .build();

        HttpResponse<InputStream> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofInputStream()
                );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Failed to download Minecraft server: HTTP "
                            + response.statusCode()
            );
        }

        try (InputStream input =
                     response.body()) {

            Files.copy(
                    input,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private String sha1(
            Path file
    ) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-1");

        try (InputStream input =
                     Files.newInputStream(file)) {

            byte[] buffer =
                    new byte[8192];

            int read;

            while ((read = input.read(buffer)) != -1) {
                digest.update(
                        buffer,
                        0,
                        read
                );
            }
        }

        StringBuilder result =
                new StringBuilder();

        for (byte value :
                digest.digest()) {

            result.append(
                    String.format(
                            "%02x",
                            value
                    )
            );
        }

        return result.toString();
    }
}
