package com.restaurant.restaurantpremium;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import java.util.Random;

public class AddFoodItem extends JFrame {
    private JTextField nameField, priceField, categoryField, imageField;
    private JTextArea descArea;
    private JCheckBox specialCheck;

    // Color scheme
    private final Color primaryColor = new Color(40, 42, 53);
    private final Color secondaryColor = new Color(255, 215, 0);
    private final Color accentColor = new Color(230, 230, 230);
    private final Color textColor = new Color(60, 60, 60);

    public AddFoodItem() {
        setTitle("Add New Food Item - Premium Restaurant");
        setSize(650, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setApplicationIcon();
        
        initUI();
    }

    private void setApplicationIcon() {
        try {
            URL iconUrl = getClass().getResource("/images/restaurant_logo.png");
            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.err.println("Error loading application icon: " + e.getMessage());
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JLabel titleLabel = new JLabel("ADD NEW MENU ITEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(new Color(250, 250, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = createStyledTextField();
        descArea = createStyledTextArea();
        priceField = createStyledTextField();
        categoryField = createStyledTextField();
        imageField = createStyledTextField();
        specialCheck = createStyledCheckbox();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(createFormLabel("Item Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(createFormLabel("Description:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(descArea), gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(createFormLabel("Price:"), gbc);
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(createFormLabel("Category:"), gbc);
        gbc.gridx = 1;
        formPanel.add(categoryField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(createFormLabel("Image Path:"), gbc);
        gbc.gridx = 1;
        formPanel.add(imageField, gbc);

        gbc.gridx = 1; gbc.gridy++;
        formPanel.add(specialCheck, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(250, 250, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton cancelButton = createStyledButton("Cancel", false);
        JButton saveButton = createStyledButton("Save Item", true);

        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> saveFoodItem());

        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(saveButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Montserrat", Font.BOLD, 14));
        label.setForeground(textColor);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Open Sans", Font.PLAIN, 14));
        return field;
    }

    private JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(new Font("Open Sans", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JCheckBox createStyledCheckbox() {
        JCheckBox checkBox = new JCheckBox("Special Item?");
        checkBox.setFont(new Font("Open Sans", Font.PLAIN, 14));
        return checkBox;
    }

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Montserrat", Font.BOLD, 14));
        return button;
    }

    private void saveFoodItem() {
        try {
            String name = nameField.getText();
            String description = descArea.getText();
            double originalPrice = Double.parseDouble(priceField.getText());
            String category = categoryField.getText();
            String imagePath = imageField.getText();
            boolean isSpecial = specialCheck.isSelected();

            if (name.isEmpty() || description.isEmpty()) {
                showError("Name and description are required");
                return;
            }

            // Generate a random discount between 5% and 25%
            double discountPercentage = 5 + new Random().nextDouble() * 20; 

            // Calculate discounted price
            double discountedPrice = originalPrice - (originalPrice * discountPercentage / 100);

            String sql = "INSERT INTO food_items (name, description, original_price, price, discount_percentage, category, image_path, is_special) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, name);
                pstmt.setString(2, description);
                pstmt.setDouble(3, originalPrice);
                pstmt.setDouble(4, discountedPrice);
                pstmt.setDouble(5, discountPercentage);
                pstmt.setString(6, category);
                pstmt.setString(7, imagePath);
                pstmt.setBoolean(8, isSpecial);

                pstmt.executeUpdate();

                showSuccess("Food item added successfully with a " + String.format("%.2f", discountPercentage) + "% discount!");
                dispose();
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid price");
        } catch (SQLException e) {
            showError("Error saving to database: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
