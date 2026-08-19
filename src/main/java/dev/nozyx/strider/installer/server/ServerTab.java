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

import dev.nozyx.strider.installer.InstallerConstants;
import dev.nozyx.strider.installer.StriderInstaller;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ServerTab {

    private static final int TOTAL_STEPS = 6;

    private final MinecraftServerDownloader serverDownloader;

    private final JFrame frame;

    private final JPanel panel;

    private final JComboBox<String> striderVersionBox;
    private final JComboBox<String> mcVersionCombo;
    private final JCheckBox disableUICheckBox;
    private final JTextField pathField;
    private final JButton installButton;

    public ServerTab(JFrame frame) {
        serverDownloader =
                new MinecraftServerDownloader();

        this.frame = frame;

        panel = new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        striderVersionBox =
                new JComboBox<>(
                        InstallerConstants.SUPPORTED_LOADER_VERSIONS
                );

        mcVersionCombo =
                new JComboBox<>();

        disableUICheckBox =
                new JCheckBox("Disable UI");

        pathField =
                new JTextField(
                        "-- Select server folder --"
                );

        installButton =
                new JButton("Install");

        buildPanel();

        updateMinecraftVersions();
        updateInstallButton();

        striderVersionBox.addActionListener(e -> {
            updateMinecraftVersions();
            updateInstallButton();
        });

        mcVersionCombo.addActionListener(e ->
                updateInstallButton()
        );
    }

    public JPanel getPanel() {
        return panel;
    }

    private void buildPanel() {
        JLabel striderLabel =
                new JLabel("Select StriderLoader version:");

        striderLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        16
                )
        );

        striderLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(striderLabel);
        panel.add(Box.createVerticalStrut(5));

        striderVersionBox.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        16
                )
        );

        striderVersionBox.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        30
                )
        );

        striderVersionBox.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        striderVersionBox.setToolTipText(
                "Select the version of StriderLoader you want to install."
        );

        panel.add(striderVersionBox);
        panel.add(Box.createVerticalStrut(20));

        JLabel mcLabel =
                new JLabel("Select Minecraft version:");

        mcLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        16
                )
        );

        mcLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(mcLabel);
        panel.add(Box.createVerticalStrut(5));

        mcVersionCombo.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        16
                )
        );

        mcVersionCombo.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        30
                )
        );

        mcVersionCombo.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        mcVersionCombo.setToolTipText(
                "Select the Minecraft version you want to install StriderLoader for."
        );

        panel.add(mcVersionCombo);
        panel.add(Box.createVerticalStrut(5));

        disableUICheckBox.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        16
                )
        );

        disableUICheckBox.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        disableUICheckBox.setToolTipText(
                "Disable the StriderLoader startup UI."
        );

        panel.add(disableUICheckBox);
        panel.add(Box.createVerticalStrut(5));

        JLabel folderLabel =
                new JLabel("Server folder:");

        folderLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        16
                )
        );

        folderLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(folderLabel);
        panel.add(Box.createVerticalStrut(5));

        pathField.setEditable(false);

        pathField.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        JButton browseButton =
                new JButton("Browse...");

        browseButton.addActionListener(
                e -> browse()
        );

        JPanel pathPanel =
                new JPanel(
                        new BorderLayout(5, 0)
                );

        pathPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        30
                )
        );

        pathPanel.add(
                pathField,
                BorderLayout.CENTER
        );

        pathPanel.add(
                browseButton,
                BorderLayout.EAST
        );

        pathPanel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(pathPanel);
        panel.add(Box.createVerticalGlue());

        installButton.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        16
                )
        );

        installButton.addActionListener(
                e -> install()
        );

        JPanel buttons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                20,
                                10
                        )
                );

        buttons.add(installButton);

        panel.add(buttons);
    }

    private void updateMinecraftVersions() {
        String loaderVersion =
                (String) striderVersionBox.getSelectedItem();

        mcVersionCombo.removeAllItems();

        if (loaderVersion == null) {
            mcVersionCombo.setEnabled(false);
            return;
        }

        String[] versions =
                InstallerConstants.SUPPORTED_VERSIONS.get(
                        loaderVersion
                );

        if (versions == null || versions.length == 0) {
            mcVersionCombo.setEnabled(false);
            return;
        }

        mcVersionCombo.setEnabled(true);

        for (String version : versions) {
            mcVersionCombo.addItem(version);
        }

        mcVersionCombo.setSelectedIndex(0);
    }

    private void updateInstallButton() {
        String loaderVersion =
                (String) striderVersionBox.getSelectedItem();

        String mcVersion =
                (String) mcVersionCombo.getSelectedItem();

        String path =
                pathField.getText();

        boolean valid =
                loaderVersion != null
                        && mcVersion != null
                        && !mcVersion.isBlank()
                        && path != null
                        && !path.startsWith("--")
                        && new File(path).isDirectory();

        installButton.setEnabled(valid);
    }

    private void browse() {
        JFileChooser chooser =
                new JFileChooser();

        chooser.setFileSelectionMode(
                JFileChooser.DIRECTORIES_ONLY
        );

        if (chooser.showOpenDialog(frame)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected =
                chooser.getSelectedFile();

        if (!selected.isDirectory()) {
            return;
        }

        pathField.setText(
                selected.getAbsolutePath()
        );

        updateInstallButton();
    }

    private void install() {
        String loaderVersion =
                (String) striderVersionBox.getSelectedItem();

        String mcVersion =
                (String) mcVersionCombo.getSelectedItem();

        String serverFolder =
                pathField.getText();

        boolean disableUI =
                disableUICheckBox.isSelected();

        ServerInstallProgress progress =
                new ServerInstallProgress(frame);

        installButton.setEnabled(false);
        striderVersionBox.setEnabled(false);
        mcVersionCombo.setEnabled(false);
        disableUICheckBox.setEnabled(false);

        SwingWorker<Void, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Void doInBackground()
                            throws Exception {

                        boolean installed =
                                install(
                                        loaderVersion,
                                        mcVersion,
                                        serverFolder,
                                        disableUI,
                                        progress
                                );

                        if (!installed) {
                            cancel(false);
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        progress.close();

                        installButton.setEnabled(true);
                        striderVersionBox.setEnabled(true);
                        mcVersionCombo.setEnabled(true);
                        disableUICheckBox.setEnabled(true);

                        if (isCancelled()) {
                            return;
                        }

                        try {
                            get();

                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Installation completed!\n\n"
                                            + "StriderLoader "
                                            + loaderVersion
                                            + " has been installed for Minecraft Server "
                                            + mcVersion
                                            + ".",
                                    "Installation done",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                        } catch (Exception e) {
                            Throwable cause =
                                    e.getCause() != null
                                            ? e.getCause()
                                            : e;

                            cause.printStackTrace(
                                    System.err
                            );

                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Error while installing StriderLoader:\n"
                                            + cause.getLocalizedMessage(),
                                    "Install error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                };

        worker.execute();

        progress.showDialog();
    }

    private boolean install(
            String loaderVersion,
            String mcVersion,
            String serverFolder,
            boolean disableUI,
            ServerInstallProgress progress
    ) throws Exception {
        Path serverFolderPath =
                Path.of(serverFolder);

        if (Files.isDirectory(serverFolderPath)) {
            try (Stream<Path> stream =
                         Files.list(serverFolderPath)) {

                if (stream.findAny().isPresent()) {
                    int result =
                            JOptionPane.showConfirmDialog(
                                    frame,
                                    """
                                            The selected server folder is not empty.
                                            Existing files may be overwritten.
                                            Continue anyway?""",
                                    "Non-empty server folder",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE
                            );

                    if (result != JOptionPane.YES_OPTION) {
                        return false;
                    }
                }
            }
        }

        Files.createDirectories(
                serverFolderPath
        );

        JsonNode manifest =
                loadServerManifest(
                        loaderVersion
                );

        progress.setStep(
                1,
                TOTAL_STEPS,
                "Downloading Minecraft server"
        );

        Path serverJar =
                serverDownloader.download(
                        mcVersion,
                        serverFolderPath
                );


        progress.setStep(
                2,
                TOTAL_STEPS,
                "Extracting server files"
        );

        extractMinecraftBundle(
                serverJar,
                serverFolderPath
        );

        progress.setStep(
                3,
                TOTAL_STEPS,
                "Installing Minecraft libraries"
        );

        progress.setStep(
                4,
                TOTAL_STEPS,
                "Installing StriderLoader libraries"
        );

        installLibraries(
                manifest,
                serverFolderPath
        );

        progress.setStep(
                5,
                TOTAL_STEPS,
                "Creating launch scripts"
        );

        createLaunchScripts(
                mcVersion,
                serverFolderPath,
                manifest,
                disableUI
        );

        progress.setCompleted();

        return true;
    }

    private void extractMinecraftBundle(
            Path serverBundle,
            Path serverFolder
    ) throws Exception {
        Path librariesDirectory =
                serverFolder.resolve("libraries");

        Files.createDirectories(
                librariesDirectory
        );

        Path extractedServer =
                serverFolder.resolve("server.jar.tmp");

        try (JarFile jar =
                     new JarFile(serverBundle.toFile())) {

            List<BundleEntry> libraries =
                    readBundleList(
                            jar,
                            "META-INF/libraries.list",
                            "META-INF/libraries/"
                    );

            List<BundleEntry> versions =
                    readBundleList(
                            jar,
                            "META-INF/versions.list",
                            "META-INF/versions/"
                    );

            for (BundleEntry library : libraries) {

                JarEntry entry =
                        jar.getJarEntry(
                                library.jarPath()
                        );

                if (entry == null) {
                    throw new IOException(
                            "Bundled library not found: "
                                    + library.jarPath()
                    );
                }

                Path destination =
                        librariesDirectory.resolve(
                                library.path()
                        );

                extractAndVerify(
                        jar,
                        entry,
                        destination,
                        library.sha256()
                );
            }

            BundleEntry serverEntry =
                    findServerBundleEntry(
                            versions
                    );

            if (serverEntry == null) {
                throw new IOException(
                        "Could not find the Minecraft server JAR in the bundle."
                );
            }

            JarEntry entry =
                    jar.getJarEntry(
                            serverEntry.jarPath()
                    );

            if (entry == null) {
                throw new IOException(
                        "Minecraft server JAR not found: "
                                + serverEntry.jarPath()
                );
            }

            extractAndVerify(
                    jar,
                    entry,
                    extractedServer,
                    serverEntry.sha256()
            );
        }

        Files.move(
                extractedServer,
                serverBundle,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    private List<BundleEntry> readBundleList(
            JarFile jar,
            String listPath,
            String jarPrefix
    ) throws IOException {
        JarEntry listEntry =
                jar.getJarEntry(listPath);

        if (listEntry == null) {
            throw new IOException(
                    "Bundler list not found: "
                            + listPath
            );
        }

        List<BundleEntry> result =
                new ArrayList<>();

        try (InputStream input =
                     jar.getInputStream(listEntry)) {

            String content =
                    new String(
                            input.readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            for (String line :
                    content.split("\\R")) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts =
                        line.split(
                                "\\t",
                                3
                        );

                if (parts.length != 3) {
                    throw new IOException(
                            "Invalid bundler entry in "
                                    + listPath
                                    + ": "
                                    + line
                    );
                }

                result.add(
                        new BundleEntry(
                                parts[0],
                                parts[1],
                                parts[2],
                                jarPrefix + parts[2]
                        )
                );
            }
        }

        return result;
    }

    private BundleEntry findServerBundleEntry(
            List<BundleEntry> versions
    ) {
        for (BundleEntry entry :
                versions) {

            if (entry.path().endsWith(".jar")) {
                return entry;
            }
        }

        return null;
    }

    private void extractAndVerify(
            JarFile jar,
            JarEntry entry,
            Path destination,
            String expectedSha256
    ) throws Exception {
        Files.createDirectories(
                destination.getParent()
        );

        try (InputStream input =
                     jar.getInputStream(entry)) {

            Files.copy(
                    input,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        String actualSha256 =
                sha256(destination);

        if (!expectedSha256.equalsIgnoreCase(
                actualSha256
        )) {

            Files.deleteIfExists(
                    destination
            );

            throw new IOException(
                    "Checksum mismatch for "
                            + entry.getName()
                            + "\nExpected SHA-256: "
                            + expectedSha256
                            + "\nActual SHA-256: "
                            + actualSha256
            );
        }
    }

    private void installLibraries(
            JsonNode manifest,
            Path serverFolder
    ) throws Exception {

        Path librariesDirectory =
                serverFolder.resolve(
                        "libraries"
                );

        JsonNode libraries =
                manifest.path("libraries");

        if (!libraries.isArray()) {
            throw new IOException(
                    "Missing or invalid libraries in server manifest."
            );
        }

        for (JsonNode library :
                libraries) {

            String path =
                    library
                            .path("path")
                            .asString();

            if (path.isBlank()) {
                continue;
            }

            String url =
                    library
                            .path("url")
                            .asString();

            String sha1 =
                    library
                            .path("sha1")
                            .asString();

            downloadLibrary(
                    path,
                    url,
                    sha1,
                    librariesDirectory
            );
        }
    }

    private JsonNode loadServerManifest(
            String loaderVersion
    ) throws IOException {
        try (InputStream input =
                     StriderInstaller.class
                             .getResourceAsStream(
                                     "/server/"
                                             + loaderVersion
                                             + ".json"
                             )) {

            if (input == null) {
                throw new IOException(
                        "Server JSON not found for loader "
                                + loaderVersion
                );
            }

            return new ObjectMapper()
                    .readTree(input);
        }
    }

    private void downloadLibrary(
            String relativePath,
            String url,
            String expectedSha1,
            Path librariesDirectory
    ) throws Exception {
        Path destination =
                librariesDirectory.resolve(
                        relativePath
                );

        Files.createDirectories(
                destination.getParent()
        );

        HttpClient client =
                HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder(
                                URI.create(url)
                        )
                        .GET()
                        .build();

        HttpResponse<InputStream> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofInputStream()
                );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Failed to download library: "
                            + url
                            + " (HTTP "
                            + response.statusCode()
                            + ")"
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

        if (!expectedSha1.isBlank()) {
            String actualSha1 =
                    sha1(destination);

            if (!expectedSha1.equalsIgnoreCase(
                    actualSha1
            )) {
                Files.deleteIfExists(
                        destination
                );

                throw new IOException(
                        "Library checksum mismatch: "
                                + relativePath
                                + "\nExpected SHA-1: "
                                + expectedSha1
                                + "\nActual SHA-1: "
                                + actualSha1
                );
            }
        }
    }

    private void createLaunchScripts(
            String mcVersion,
            Path serverFolder,
            JsonNode manifest,
            boolean disableUI
    ) throws IOException {
        Path librariesDirectory =
                serverFolder.resolve(
                        "libraries"
                );

        List<String> libraries =
                new ArrayList<>();

        if (Files.isDirectory(
                librariesDirectory
        )) {
            try (Stream<Path> stream =
                         Files.walk(
                                 librariesDirectory
                         )) {

                stream
                        .filter(Files::isRegularFile)
                        .filter(path ->
                                path.toString()
                                        .endsWith(".jar")
                        )
                        .forEach(path -> {

                            Path relative =
                                    serverFolder.relativize(
                                            path
                                    );

                            libraries.add(
                                    relative.toString()
                            );
                        });
            }
        }

        libraries.sort(
                String::compareTo
        );

        String separator =
                File.pathSeparator;

        String classpath =
                "server.jar"
                        + separator
                        + String.join(
                        separator,
                        libraries
                );

        List<String> jvmArguments =
                new ArrayList<>();

        JsonNode arguments =
                manifest.path(
                        "jvmArguments"
                );

        if (!arguments.isArray()) {
            throw new IOException(
                    "Missing or invalid jvmArguments "
                            + "in server manifest."
            );
        }

        for (JsonNode argument :
                arguments) {
            String value =
                    argument.asString();

            if (value.isBlank()) {
                continue;
            }

            value =
                    value.replace(
                            "<mcVersion>",
                            mcVersion
                    ).replace(
                            "<uiEnabled>",
                            Boolean.toString(!disableUI)
                    );

            jvmArguments.add(
                    value
            );
        }

        String mainClass =
                manifest
                        .path("mainClass").asString();

        if (mainClass.isBlank()) {
            throw new IOException(
                    "Missing mainClass in server manifest."
            );
        }

        StringBuilder command =
                new StringBuilder(
                        "java"
                );

        for (String argument :
                jvmArguments) {

            command
                    .append(" ")
                    .append(argument);
        }

        command
                .append(" -cp \"")
                .append(classpath)
                .append("\" ")
                .append(mainClass);

        Files.writeString(
                serverFolder.resolve(
                        "run.sh"
                ),
                "#!/bin/sh\n\n"
                        + command
                        + "\n",
                StandardCharsets.UTF_8
        );

        Files.writeString(
                serverFolder.resolve(
                        "run.bat"
                ),
                "@echo off\r\n"
                        + "\r\n"
                        + command
                        + "\r\n",
                StandardCharsets.UTF_8
        );

        try {
            serverFolder
                    .resolve("run.sh")
                    .toFile()
                    .setExecutable(true);
        } catch (SecurityException ignored) {
        }
    }

    private String sha1(
            Path file
    ) throws Exception {
        return digest(
                file,
                "SHA-1"
        );
    }

    private String sha256(
            Path file
    ) throws Exception {
        return digest(
                file,
                "SHA-256"
        );
    }

    private String digest(
            Path file,
            String algorithm
    ) throws Exception {
        MessageDigest digest =
                MessageDigest.getInstance(
                        algorithm
                );

        try (InputStream input =
                     Files.newInputStream(file)) {

            byte[] buffer =
                    new byte[8192];

            int read;

            while ((read =
                    input.read(buffer)) != -1) {

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

    private record BundleEntry(
            String sha256,
            String id,
            String path,
            String jarPath
    ) {}
}
