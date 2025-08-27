package com.restaurant.restaurantpremium;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerReviews extends JFrame {
    // Components
    private JComboBox<String> foodCombo;
    private JRadioButton[] ratingButtons;
    private ButtonGroup ratingGroup;
    private JTextField nameField;
    private JTextArea commentArea;
    private JButton submitButton;
    private JPanel reviewsPanel;
    
    // Color scheme
    private final Color creamBackground = new Color(255, 253, 245);
    private final Color creamPanel = new Color(255, 251, 235);
    private final Color maroonHeader = new Color(128, 0, 32); // Light maroon
    private final Color darkText = new Color(60, 60, 60); // Replaces primaryColor
    private final Color successColor = new Color(67, 160, 71);
    private final Color accentColor = new Color(230, 230, 230);
    private final Color rightPanelColor = new Color(245, 240, 230); // Cool cream

    public CustomerReviews() {
        try {
            initializeUI();
            initComponents();
        } catch (SQLException e) {
            showError("Failed to initialize reviews: " + e.getMessage());
            dispose();
        }
    }

    private void initializeUI() {
        setTitle("Premium Restaurant - Customer Reviews");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents() throws SQLException {
        // Set default font for the application
        setUIFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(600);
        mainSplitPane.setDividerSize(3);
        mainSplitPane.setBackground(creamBackground);
        add(mainSplitPane);

        // Left panel - Form (centered content)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        leftPanel.setBackground(creamPanel);
        mainSplitPane.setLeftComponent(leftPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.weightx = 1;
        gbc.weighty = 0;

        JLabel formTitle = new JLabel("ADD YOUR REVIEW");
        formTitle.setFont(new Font("Playfair Display", Font.BOLD, 28));
        formTitle.setForeground(maroonHeader);
        leftPanel.add(formTitle, gbc);

        // Name field
        leftPanel.add(createLabel("Your Name:"), gbc);
        nameField = createTextField();
        leftPanel.add(nameField, gbc);

        // Food selection
        leftPanel.add(createLabel("Food Item:"), gbc);
        foodCombo = new JComboBox<>();
        foodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        foodCombo.setBorder(createFieldBorder());
        foodCombo.setPreferredSize(new Dimension(400, 40));
        loadFoodItems();
        leftPanel.add(foodCombo, gbc);

        // Rating selection - Horizontal radio buttons
        leftPanel.add(createLabel("Rating:"), gbc);
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ratingPanel.setBackground(creamPanel);
        
        ratingGroup = new ButtonGroup();
        ratingButtons = new JRadioButton[5];
        
        for (int i = 0; i < 5; i++) {
            ratingButtons[i] = new JRadioButton(String.valueOf(i+1));
            ratingButtons[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
            ratingButtons[i].setBackground(creamPanel);
            ratingGroup.add(ratingButtons[i]);
            ratingPanel.add(ratingButtons[i]);
        }
        ratingButtons[4].setSelected(true); // Default to 5 stars
        
        leftPanel.add(ratingPanel, gbc);

        // Comment area
        leftPanel.add(createLabel("Your Review:"), gbc);
        commentArea = new JTextArea(5, 20);
        commentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(createFieldBorder());
        commentScroll.setPreferredSize(new Dimension(400, 120));
        leftPanel.add(commentScroll, gbc);

        // Submit button - Proper green color
        submitButton = new JButton("SUBMIT REVIEW");
        submitButton.setFont(new Font("Montserrat", Font.BOLD, 16));
        submitButton.setBackground(successColor);
        submitButton.setForeground(Color.WHITE);
        submitButton.setBorderPainted(false);
        submitButton.setFocusPainted(false);
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> submitReview());
        
        // Button hover effect
        submitButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                submitButton.setBackground(new Color(56, 142, 60));
            }
            public void mouseExited(MouseEvent e) {
                submitButton.setBackground(successColor);
            }
        });

        // Add some vertical space before the button
        gbc.insets = new Insets(30, 0, 10, 0);
        leftPanel.add(submitButton, gbc);

        // Add glue to push everything up
        gbc.weighty = 1;
        leftPanel.add(Box.createVerticalGlue(), gbc);

        // Right panel - Reviews with cool cream color
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(rightPanelColor);
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel reviewsTitle = new JLabel("CUSTOMER REVIEWS");
        reviewsTitle.setFont(new Font("Playfair Display", Font.BOLD, 28));
        reviewsTitle.setForeground(maroonHeader);
        reviewsTitle.setBorder(new EmptyBorder(0, 0, 25, 0));
        rightPanel.add(reviewsTitle, BorderLayout.NORTH);

        reviewsPanel = new JPanel();
        reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));
        reviewsPanel.setBackground(rightPanelColor);

        JScrollPane scrollPane = new JScrollPane(reviewsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        mainSplitPane.setRightComponent(rightPanel);

        loadReviews();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Montserrat", Font.BOLD, 14));
        label.setForeground(darkText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(createFieldBorder());
        field.setPreferredSize(new Dimension(400, 40));
        return field;
    }

    private Border createFieldBorder() {
        return new CompoundBorder(
            new LineBorder(accentColor, 1),
            new EmptyBorder(10, 10, 10, 10)
        );
    }

    private void loadFoodItems() {
        foodCombo.removeAllItems();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM food_items ORDER BY name")) {

            while (rs.next()) {
                foodCombo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            showError("Error loading food items: " + e.getMessage());
        }
    }

    private void loadReviews() throws SQLException {
        reviewsPanel.removeAll();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT r.*, f.name as food_name FROM reviews r " +
                 "JOIN food_items f ON r.food_id = f.id " +
                 "ORDER BY r.review_date DESC LIMIT 50")) {

            while (rs.next()) {
                addReviewCard(
                    rs.getString("customer_name"),
                    rs.getString("food_name"),
                    rs.getInt("rating"),
                    rs.getString("comment"),
                    rs.getTimestamp("review_date")
                );
            }
        }
        reviewsPanel.revalidate();
        reviewsPanel.repaint();
    }

    private void addReviewCard(String customerName, String foodName, 
                             int rating, String comment, Timestamp date) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, accentColor),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Header with name and date
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(customerName);
        nameLabel.setFont(new Font("Playfair Display", Font.BOLD, 16));
        nameLabel.setForeground(darkText); // Using darkText instead of primaryColor

        String dateStr = new SimpleDateFormat("MMM dd, yyyy").format(new Date(date.getTime()));
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(new Font("Montserrat", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(150, 150, 150));

        headerPanel.add(nameLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        card.add(headerPanel);

        // Food name
        JLabel foodLabel = new JLabel(foodName);
        foodLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
        foodLabel.setForeground(darkText);
        foodLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        card.add(foodLabel);

        // Rating stars
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ratingPanel.setBackground(Color.WHITE);

        for (int i = 0; i < 5; i++) {
            JLabel star = new JLabel(i < rating ? "★" : "☆");
            star.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
            star.setForeground(i < rating ? new Color(255, 180, 0) : new Color(220, 220, 220));
            ratingPanel.add(star);
        }
        card.add(ratingPanel);

        // Comment
        JTextArea commentArea = new JTextArea(comment);
        commentArea.setFont(new Font("Open Sans", Font.PLAIN, 13));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setEditable(false);
        commentArea.setBackground(Color.WHITE);
        commentArea.setForeground(darkText);
        commentArea.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        card.add(commentArea);
        reviewsPanel.add(card);
        reviewsPanel.add(Box.createVerticalStrut(10));
    }

    private void submitReview() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Please enter your name");
            nameField.requestFocus();
            return;
        }

        if (commentArea.getText().trim().isEmpty()) {
            showError("Please enter your review");
            commentArea.requestFocus();
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String selectedFood = (String) foodCombo.getSelectedItem();
            int foodId = getFoodId(selectedFood, conn);

            if (foodId == -1) {
                showError("Selected food item not found");
                return;
            }

            // Get selected rating from radio buttons
            int selectedRating = 5; // default
            for (int i = 0; i < ratingButtons.length; i++) {
                if (ratingButtons[i].isSelected()) {
                    selectedRating = i + 1;
                    break;
                }
            }

            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO reviews (food_id, customer_name, rating, comment) " +
                "VALUES (?, ?, ?, ?)");

            pstmt.setInt(1, foodId);
            pstmt.setString(2, nameField.getText().trim());
            pstmt.setInt(3, selectedRating);
            pstmt.setString(4, commentArea.getText().trim());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                nameField.setText("");
                commentArea.setText("");
                ratingButtons[4].setSelected(true); // Reset to 5 stars
                loadReviews();
                showSuccess("Thank you for your review!");
            }
        } catch (SQLException ex) {
            showError("Error submitting review: " + ex.getMessage());
        }
    }

    private int getFoodId(String foodName, Connection conn) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
            "SELECT id FROM food_items WHERE name = ?")) {
            pstmt.setString(1, foodName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to set default font for all components
    private static void setUIFont(Font font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, font);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new CustomerReviews().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Failed to initialize reviews: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}