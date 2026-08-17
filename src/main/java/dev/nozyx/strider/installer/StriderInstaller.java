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

import com.formdev.flatlaf.FlatLightLaf;
import dev.nozyx.strider.installer.client.ClientTab;
import dev.nozyx.strider.installer.server.ServerTab;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public class StriderInstaller {

    private static final Point[] mouseClickPoint =
            new Point[1];

    public static void main(String[] args) {
        FlatLightLaf.setup();

        try {
            UIManager.setLookAndFeel(
                    new FlatLightLaf()
            );
        } catch (UnsupportedLookAndFeelException ignored) {
        }

        JFrame frame =
                createFrame();

        JPanel header =
                createHeader();

        JTabbedPane tabs =
                new JTabbedPane();

        ClientTab clientTab =
                new ClientTab(frame);

        ServerTab serverTab =
                new ServerTab(frame);

        tabs.addTab(
                "Client",
                clientTab.getPanel()
        );

        tabs.addTab(
                "Server",
                serverTab.getPanel()
        );

        frame.add(
                header,
                BorderLayout.NORTH
        );

        frame.add(
                tabs,
                BorderLayout.CENTER
        );

        frame.setVisible(true);
    }

    private static JFrame createFrame() {
        JFrame frame =
                new JFrame(
                        "StriderInstaller v"
                                + InstallerConstants.VERSION
                );

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setSize(400, 500);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        URL iconURL =
                StriderInstaller.class
                        .getResource("/icon.png");

        if (iconURL != null) {
            frame.setIconImage(
                    new ImageIcon(iconURL)
                            .getImage()
            );
        }

        frame.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(
                            MouseEvent e
                    ) {
                        mouseClickPoint[0] =
                                e.getPoint();
                    }
                }
        );

        frame.addMouseMotionListener(
                new MouseAdapter() {
                    @Override
                    public void mouseDragged(
                            MouseEvent e
                    ) {
                        Point current =
                                e.getLocationOnScreen();

                        frame.setLocation(
                                current.x
                                        - mouseClickPoint[0].x,
                                current.y
                                        - mouseClickPoint[0].y
                        );
                    }
                }
        );

        frame.setLayout(
                new BorderLayout()
        );

        return frame;
    }

    private static JPanel createHeader() {
        JPanel panel =
                new JPanel();

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
                        0,
                        20
                )
        );

        JLabel logo =
                new JLabel();

        logo.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        try {
            BufferedImage image =
                    ImageIO.read(
                            StriderInstaller.class
                                    .getResource(
                                            "/logo.png"
                                    )
                    );

            logo.setIcon(
                    new ImageIcon(
                            image.getScaledInstance(
                                    250,
                                    110,
                                    Image.SCALE_SMOOTH
                            )
                    )
            );

        } catch (IOException e) {
            logo.setText("Logo not found");
        }

        JLabel version =
                new JLabel(
                        "v"
                                + InstallerConstants.VERSION
                );

        version.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        version.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        version.setForeground(
                Color.DARK_GRAY
        );

        panel.add(logo);
        panel.add(
                Box.createVerticalStrut(5)
        );
        panel.add(version);
        panel.add(
                Box.createVerticalStrut(10)
        );

        return panel;
    }

    public static String encodeImage(
            String resourcePath
    ) throws IOException {

        try (InputStream is =
                     StriderInstaller.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     resourcePath
                             )) {

            if (is == null) {
                throw new IOException(
                        "Resource not found: "
                                + resourcePath
                );
            }

            return "data:image/png;base64,"
                    + Base64.getEncoder()
                    .encodeToString(
                            is.readAllBytes()
                    );
        }
    }
}
