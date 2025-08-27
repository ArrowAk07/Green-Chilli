package com.restaurant.restaurantpremium;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DeleteFoodItem extends JFrame {
    private JComboBox<String> itemComboBox;
    private JTextArea itemDetailsArea;
    
    // Color scheme matching AddFoodItem
    private final Color primaryColor = new Color(40, 42, 53); // Dark navy
    private final Color secondaryColor = new Color(255, 215, 0); // Gold
    private final Color accentColor = new Color(230, 230, 230); // Light gray
    private final Color textColor = new Color(60, 60, 60); // Dark gray
    
    private List<FoodItem> foodItems = new ArrayList<>();

    public DeleteFoodItem() {
        setTitle("Delete Menu Item - Premium Restaurant");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setApplicationIcon();
        
        loadMenuItems();
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
    
    private void loadMenuItems() {
        foodItems.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, description, price FROM food_items ORDER BY name")) {
            
            while (rs.next()) {
                FoodItem item = new FoodItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    null, null, 0, null, false
                );
                foodItems.add(item);
            }
        } catch (SQLException e) {
            showError("Error loading menu items: " + e.getMessage());
        }
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header with gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, primaryColor, 
                    getWidth(), 0, new Color(60, 62, 73));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("DELETE MENU ITEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Form panel with subtle pattern background
        JPanel formPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(245, 245, 245));
                for (int y = 0; y < getHeight(); y += 20) {
                    for (int x = 0; x < getWidth(); x += 20) {
                        g2d.fillOval(x, y, 2, 2);
                    }
                }
            }
        };
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(new Color(250, 250, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Item selection combo box
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(createFormLabel("Select Item:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        itemComboBox = new JComboBox<>();
        itemComboBox.setRenderer(new ItemListRenderer());
        itemComboBox.setFont(new Font("Open Sans", Font.PLAIN, 14));
        itemComboBox.setBackground(Color.WHITE);
        itemComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        itemComboBox.addActionListener(e -> updateItemDetails());
        formPanel.add(itemComboBox, gbc);
        
        // Item details area
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        
        itemDetailsArea = new JTextArea();
        itemDetailsArea.setEditable(false);
        itemDetailsArea.setFont(new Font("Open Sans", Font.PLAIN, 14));
        itemDetailsArea.setLineWrap(true);
        itemDetailsArea.setWrapStyleWord(true);
        itemDetailsArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Item Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        itemDetailsArea.setBackground(Color.WHITE);
        
        formPanel.add(new JScrollPane(itemDetailsArea), gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(250, 250, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton cancelButton = createStyledButton("Cancel", false);
        JButton deleteButton = createStyledButton("Delete Item", true);
        
        cancelButton.addActionListener(e -> dispose());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(deleteButton);
        
        // Populate combo box
        populateItemComboBox();
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void populateItemComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (FoodItem item : foodItems) {
            model.addElement(item.getName());
        }
        itemComboBox.setModel(model);
        
        if (foodItems.size() > 0) {
            updateItemDetails();
        }
    }
    
    private void updateItemDetails() {
        int selectedIndex = itemComboBox.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < foodItems.size()) {
            FoodItem selectedItem = foodItems.get(selectedIndex);
            String details = String.format(
                "Name: %s\n\nDescription: %s\n\nPrice: ₹%.2f",
                selectedItem.getName(),
                selectedItem.getDescription(),
                selectedItem.getPrice()
            );
            itemDetailsArea.setText(details);
        }
    }
    
    private void deleteSelectedItem() {
        int selectedIndex = itemComboBox.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= foodItems.size()) {
            showError("Please select an item to delete");
            return;
        }
        
        FoodItem selectedItem = foodItems.get(selectedIndex);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "<html><body><p style='width: 200px;'>Are you sure you want to permanently delete:<br><b>" + 
            selectedItem.getName() + "</b>?</p></body></html>",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM food_items WHERE id = ?";
                
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setInt(1, selectedItem.getId());
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        showSuccess("Item deleted successfully!");
                        foodItems.remove(selectedIndex);
                        populateItemComboBox();
                    } else {
                        showError("Item could not be deleted");
                    }
                }
            } catch (SQLException e) {
                showError("Error deleting item: " + e.getMessage());
            }
        }
    }
    
    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Montserrat", Font.BOLD, 14));
        label.setForeground(textColor);
        return label;
    }
    
    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Montserrat", Font.BOLD, 14));
        button.setForeground(isPrimary ? primaryColor : textColor);
        button.setBackground(isPrimary ? secondaryColor : Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isPrimary ? primaryColor : accentColor, 1),
            BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(isPrimary ? primaryColor : accentColor);
                button.setForeground(isPrimary ? Color.WHITE : textColor);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(isPrimary ? secondaryColor : Color.WHITE);
                button.setForeground(isPrimary ? primaryColor : textColor);
            }
        });
        
        return button;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, 
            "<html><body><p style='width: 200px;'>" + message + "</p></body></html>", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, 
            "<html><body><p style='width: 200px;'>" + message + "</p></body></html>", 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Custom renderer for combo box items
    private class ItemListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            setFont(new Font("Open Sans", Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            if (isSelected) {
                setBackground(primaryColor);
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(textColor);
            }
            
            return this;
        }
    }
    
    // Simple FoodItem class for holding data
    private static class FoodItem {
        private int id;
        private String name;
        private String description;
        private double price;
        private String category;
        private String imagePath;
        private double rating;
        private String offer;
        private boolean isSpecial;
        
        public FoodItem(int id, String name, String description, double price, 
                       String category, String imagePath, double rating, 
                       String offer, boolean isSpecial) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.category = category;
            this.imagePath = imagePath;
            this.rating = rating;
            this.offer = offer;
            this.isSpecial = isSpecial;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public String getCategory() { return category; }
        public String getImagePath() { return imagePath; }
        public double getRating() { return rating; }
        public String getOffer() { return offer; }
        public boolean isSpecial() { return isSpecial; }
    }
}