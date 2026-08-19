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

package dev.nozyx.strider.installer.client;

import dev.nozyx.strider.installer.InstallerConstants;
import dev.nozyx.strider.installer.StriderInstaller;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

public class ClientTab {

    private final JFrame frame;

    private final JPanel panel;

    private final JComboBox<String> striderVersionBox;
    private final JComboBox<String> mcVersionCombo;
    private final JCheckBox disableUICheckBox;
    private final JTextField pathField;
    private final JButton installButton;

    public ClientTab(JFrame frame) {
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
                        getDefaultMinecraftPath()
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

        JLabel launcherLabel =
                new JLabel("Launcher folder:");

        launcherLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        16
                )
        );

        launcherLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(launcherLabel);
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

        String launcherFolder =
                pathField.getText();

        boolean disableUI =
                disableUICheckBox.isSelected();

        try {
            boolean installed =
                    install(
                            loaderVersion,
                            mcVersion,
                            launcherFolder,
                            disableUI
                    );

            if (!installed) {
                return;
            }

            JOptionPane.showMessageDialog(
                    frame,
                    "Installation completed!\n\n"
                            + "StriderLoader "
                            + loaderVersion
                            + " has been installed for Minecraft Client "
                            + mcVersion
                            + ".",
                    "Installation done",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            e.printStackTrace(System.err);

            JOptionPane.showMessageDialog(
                    frame,
                    "Error while installing StriderLoader:\n"
                            + e.getLocalizedMessage(),
                    "Install error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean install(
            String loaderVersion,
            String mcVersion,
            String launcherFolder,
            boolean disableUI
    ) throws Exception {
        File launcherProfiles =
                new File(
                        launcherFolder,
                        "launcher_profiles.json"
                );

        if (!launcherProfiles.exists()) {
            throw new IOException(
                    "launcher_profiles.json not found in: "
                            + launcherFolder
            );
        }

        File minecraftVersionDir =
                new File(
                        launcherFolder,
                        "versions"
                                + File.separator
                                + mcVersion
                );

        File minecraftVersionJson =
                new File(
                        minecraftVersionDir,
                        mcVersion + ".json"
                );

        if (!minecraftVersionJson.isFile()) {
            throw new IOException(
                    "Minecraft version '"
                            + mcVersion
                            + "' was not found in the selected launcher folder."
            );
        }

        File striderDir =
                new File(
                        launcherFolder,
                        "versions"
                                + File.separator
                                + "striderloader-"
                                + loaderVersion
                                + "-"
                                + mcVersion
                );

        if (striderDir.exists()) {
            int result =
                    JOptionPane.showConfirmDialog(
                            frame,
                            "StriderLoader "
                                    + loaderVersion
                                    + " for Minecraft Client "
                                    + mcVersion
                                    + " is already installed.\n"
                                    + "Reinstall it?",
                            "Reinstall",
                            JOptionPane.YES_NO_OPTION
                    );

            if (result != JOptionPane.YES_OPTION) {
                return false;
            }

            deleteDirectory(
                    striderDir.toPath()
            );
        }

        if (!striderDir.mkdirs()) {
            throw new IOException(
                    "Could not create: "
                            + striderDir
            );
        }

        createVersionJson(
                loaderVersion,
                mcVersion,
                striderDir,
                disableUI
        );

        updateLauncherProfiles(
                launcherProfiles,
                loaderVersion,
                mcVersion
        );

        return true;
    }

    private void createVersionJson(
            String loaderVersion,
            String mcVersion,
            File striderDir,
            boolean disableUI
    ) throws IOException {

        try (InputStream is =
                     StriderInstaller.class
                             .getResourceAsStream(
                                     "/client/"
                                             + loaderVersion
                                             + ".json"
                             )) {

            if (is == null) {
                throw new IOException(
                        "Client JSON not found for loader "
                                + loaderVersion
                );
            }

            String json =
                    new String(
                            is.readAllBytes(),
                            StandardCharsets.UTF_8
                    )
                            .replace(
                                    "<loaderVersion>",
                                    loaderVersion
                            )
                            .replace(
                                    "<mcVersion>",
                                    mcVersion
                            )
                            .replace(
                                    "<uiEnabled>",
                                    Boolean.toString(!disableUI)
                            );

            Files.writeString(
                    new File(
                            striderDir,
                            "striderloader-"
                                    + loaderVersion
                                    + "-"
                                    + mcVersion
                                    + ".json"
                    ).toPath(),
                    json,
                    StandardCharsets.UTF_8
            );
        }
    }

    private void updateLauncherProfiles(
            File launcherProfiles,
            String loaderVersion,
            String mcVersion
    ) throws IOException {

        ObjectMapper mapper =
                new ObjectMapper();

        ObjectNode root =
                (ObjectNode) mapper.readTree(
                        Files.readString(
                                launcherProfiles.toPath(),
                                StandardCharsets.UTF_8
                        )
                );

        ObjectNode profiles;

        if (root.has("profiles")
                && root.get("profiles").isObject()) {

            profiles =
                    (ObjectNode) root.get("profiles");

        } else {
            profiles =
                    mapper.createObjectNode();

            root.set(
                    "profiles",
                    profiles
            );
        }

        String profileKey =
                "striderloader-"
                        + loaderVersion
                        + "-"
                        + mcVersion;

        String now =
                DateTimeFormatter.ISO_INSTANT.format(
                        Instant.now()
                );

        ObjectNode profile =
                mapper.createObjectNode();

        profile.put("created", now);
        profile.put(
                "icon",
                StriderInstaller.encodeImage("icon.png")
        );
        profile.put("lastUsed", now);
        profile.put(
                "lastVersionId",
                profileKey
        );
        profile.put(
                "name",
                "StriderLoader "
                        + loaderVersion
                        + " - Minecraft "
                        + mcVersion
        );
        profile.put("type", "custom");

        profiles.set(
                profileKey,
                profile
        );

        Files.writeString(
                launcherProfiles.toPath(),
                mapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(root),
                StandardCharsets.UTF_8
        );
    }

    private void deleteDirectory(
            Path directory
    ) throws IOException {

        try (Stream<Path> stream =
                     Files.walk(directory)) {

            stream
                    .filter(path -> !path.equals(directory))
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        Files.delete(directory);
    }

    private static String getDefaultMinecraftPath() {
        String os =
                System.getProperty("os.name")
                        .toLowerCase();

        String path;

        if (os.contains("win")) {
            path =
                    System.getenv("APPDATA")
                            + "\\.minecraft";

        } else if (os.contains("mac")) {
            path =
                    System.getProperty("user.home")
                            + "/Library/Application Support/minecraft";

        } else {
            path =
                    System.getProperty("user.home")
                            + "/.minecraft";
        }

        return new File(path).isDirectory()
                ? path
                : "-- Default folder not found --";
    }
}
