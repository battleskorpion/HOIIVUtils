package com.hoi4utils.ui.menu;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.ui.settings.SettingsController;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class SettingsValidator {
    private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SettingsValidator.class);

    public static void checkForInvalidSettingsAndShowWarnings(Button button, Logger logger) {
        boolean hasInvalidPaths = false;
        StringBuilder warningMessage = new StringBuilder("The following settings need to be configured:\n\n");

        if (HOIIVUtils.get("valid.HOIIVFilePaths").equals("false")) {
            logger.warn("Invalid HOI IV file paths detected");
            warningMessage.append("• Hearts of Iron IV file paths\n");
            hasInvalidPaths = true;
        }

        if (HOIIVUtils.get("valid.LocalizationManager").equals("false")) {
            logger.error("Localization Manager failed to load");
            warningMessage.append("• Localization failed (bad hoi4 folder?)\n");
            hasInvalidPaths = true;
        }

        if (HOIIVUtils.get("valid.Interface").equals("false")) {
            logger.warn("Invalid GFX Interface file paths detected");
            warningMessage.append("• Interface file paths\n");
            hasInvalidPaths = true;
        }

        if (HOIIVUtils.get("valid.State").equals("false")) {
            logger.warn("Invalid State paths detected");
            warningMessage.append("• State file paths\n");
            hasInvalidPaths = true;
        }

        if (HOIIVUtils.get("valid.FocusTree").equals("false")) {
            logger.warn("Invalid Focus Tree paths detected");
            warningMessage.append("• Focus Tree file paths\n");
            hasInvalidPaths = true;
        }
        LOGGER.info("valid.Settings property set to: {}", hasInvalidPaths ? "false" : "true");
        HOIIVUtils.set("valid.Settings", hasInvalidPaths ? "false" : "true");
        if (hasInvalidPaths) {
            warningMessage.append("\nPlease go to Settings to configure these paths.");
            showWarningDialog(warningMessage.toString(), button, logger);
        }

    }
    static JDialog dialog;
    private static void showWarningDialog(String message, Button button, Logger logger) {
        dialog = new JDialog();
        dialog.setTitle("Configuration Required");
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
        panel.add(iconLabel, BorderLayout.WEST);

        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setBackground(panel.getBackground());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Dialog", Font.PLAIN, 14));
        panel.add(messageArea, BorderLayout.CENTER);

        JPanel buttonPanel = getButtonPanel(button, logger, dialog);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        dialog.setVisible(true);
    }

    private static @NotNull JPanel getButtonPanel(Button button, Logger logger, JDialog dialog) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton settingsButton = new JButton("Open Settings");

        settingsButton.addActionListener(e -> {
            Platform.runLater(() -> {
                try {
                    ((Stage) (button.getScene().getWindow())).close();
                } catch (Exception ex) {
                    logger.error("Failed to close menu window", ex);
                }
            });
            Platform.runLater(() -> new SettingsController().open());
            dialog.dispose();
        });

        buttonPanel.add(settingsButton);
        return buttonPanel;
    }
}