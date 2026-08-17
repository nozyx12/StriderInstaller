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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ServerInstallProgress {

    private final JDialog dialog;

    private final JLabel statusLabel;
    private final JLabel dotsLabel;
    private final JProgressBar progressBar;

    private final Timer dotsTimer;

    private int dots = 0;

    public ServerInstallProgress(JFrame parent) {
        dialog =
                new JDialog(
                        parent,
                        "Installing server",
                        true
                );

        dialog.setUndecorated(true);

        dialog.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE
        );

        dialog.setSize(
                450,
                100
        );

        dialog.setResizable(false);

        dialog.setShape(
                new RoundRectangle2D.Double(
                        0,
                        0,
                        dialog.getWidth(),
                        dialog.getHeight(),
                        20,
                        20
                )
        );

        dialog.setLocationRelativeTo(
                parent
        );

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
                        25,
                        20,
                        25
                )
        );

        statusLabel =
                new JLabel(
                        "Preparing installation..."
                );

        statusLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        14
                )
        );

        dotsLabel =
                new JLabel();

        dotsLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        JPanel statusPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                0,
                                0
                        )
                );

        statusPanel.setOpaque(false);

        statusPanel.add(statusLabel);
        statusPanel.add(dotsLabel);

        statusPanel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        progressBar =
                new JProgressBar();

        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);

        progressBar.setStringPainted(true);

        progressBar.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(statusPanel);

        panel.add(
                Box.createVerticalStrut(15)
        );

        panel.add(progressBar);

        dialog.setContentPane(panel);

        dotsTimer =
                new Timer(
                        400,
                        e -> {
                            dots = (dots + 1) % 4;

                            dotsLabel.setText(
                                    ".".repeat(dots)
                            );
                        }
                );

        dotsTimer.start();
    }

    public void setStep(
            int step,
            int totalSteps,
            String message
    ) {
        int progress =
                (int) (
                        ((double) step / totalSteps)
                                * 100
                );

        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);

            progressBar.setValue(
                    progress
            );

            progressBar.setString(
                    step + " / " + totalSteps
            );

            dots = 0;
            dotsLabel.setText("");
        });
    }

    public void setCompleted() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(
                    "Installation completed."
            );

            dotsLabel.setText("");

            progressBar.setValue(100);

            progressBar.setString(
                    "Complete"
            );

            dotsTimer.stop();
        });
    }

    public void showDialog() {
        dialog.setVisible(true);
    }

    public void close() {
        dotsTimer.stop();

        SwingUtilities.invokeLater(
                dialog::dispose
        );
    }
}
