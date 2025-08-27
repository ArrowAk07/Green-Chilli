package com.restaurant.restaurantpremium;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class BillingSystem extends JFrame {
    private final Color GREEN = new Color(0, 128, 0);
    private final Color RED = new Color(255, 0, 0);
    private final Color BLUE = new Color(0, 0, 255);
    private final Color GRAY = new Color(128, 128, 128);
    private final Color PRICE_COLOR = new Color(200, 0, 0);
    
    private List<FoodItem> foodItems = new ArrayList<>();
    private DefaultListModel<String> cartModel = new DefaultListModel<>();
    private JList<String> cartList;
    private JLabel subtotalLabel, taxLabel, totalLabel;
    private double subtotal = 0.0;
    private final double TAX_RATE = 0.05;
    private NumberFormat rupeeFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private Map<String, ImageIcon> imageCache = new HashMap<>();

    public BillingSystem() {
        setTitle("Green Chilli - Billing");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(250, 245, 240));
        
        try {
            setIconImage(new ImageIcon(getClass().getResource("/com/restaurant/restaurantpremium/resources/logo.png")).getImage());
        } catch (Exception e) {
            System.out.println("Logo not found, using default icon");
        }
        
        loadMenuItems();
        initUI();
    }

    private void loadMenuItems() {
        foodItems.clear();
        try (Connection conn = DBConnection.getConnection()) {
            // Load food items
            String foodItemsSql = "SELECT *, " +
                                "CASE WHEN discount_percentage > 0 THEN original_price ELSE price END AS display_price, " +
                                "discount_percentage " +
                                "FROM food_items ORDER BY name";
            try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                 ResultSet rs = stmt.executeQuery(foodItemsSql)) {
                
                while (rs.next()) {
                    FoodItem item = new FoodItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("image_path"),
                        rs.getDouble("avg_rating"),
                        rs.getBoolean("is_special"),
                        rs.getDouble("original_price"),
                        rs.getDouble("discount_percentage")
                    );
                    foodItems.add(item);
                }
            }

            // Load reviews for each food item
            for (FoodItem item : foodItems) {
                String reviewsSql = "SELECT comment FROM reviews WHERE food_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(reviewsSql)) {
                    pstmt.setInt(1, item.getId());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            item.addReview(rs.getString("comment"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading menu: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(250, 245, 240);
                Color color2 = new Color(220, 210, 200);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setOpaque(true);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("GREEN CHILLI CHECKOUT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(70, 50, 40));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Menu Panel
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150, 120, 90)), 
            "OUR MENU",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16),
            new Color(100, 70, 50))
        );
        menuPanel.setOpaque(false);
        
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        
        for (FoodItem item : foodItems) {
            itemsPanel.add(createMenuItemPanel(item));
            itemsPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane menuScroll = new JScrollPane(itemsPanel);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuPanel.add(menuScroll, BorderLayout.CENTER);

        // Cart Panel
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150, 120, 90)), 
            "YOUR ORDER",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16),
            new Color(100, 70, 50))
        );
        cartPanel.setBackground(Color.WHITE);

        cartList = new JList<>(cartModel);
        cartList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cartList.setFixedCellHeight(40);
        cartList.setCellRenderer(new CartItemRenderer());
        
        JScrollPane cartScroll = new JScrollPane(cartList);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartPanel.add(cartScroll, BorderLayout.CENTER);

        // Cart Controls
        JPanel cartControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        cartControls.setOpaque(false);
        
        JButton removeButton = createStyledButton("Remove Selected", RED);
        removeButton.addActionListener(e -> removeSelectedItem());
        cartControls.add(removeButton);

        JButton clearButton = createStyledButton("Clear All", GRAY);
        clearButton.addActionListener(e -> clearCart());
        cartControls.add(clearButton);

        cartPanel.add(cartControls, BorderLayout.SOUTH);

        // Totals Panel
        JPanel totalsPanel = new JPanel(new GridBagLayout());
        totalsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        totalsPanel.setBackground(new Color(255, 255, 255, 180));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        subtotalLabel = new JLabel();
        subtotalLabel.setLayout(new BorderLayout());
        subtotalLabel.add(new JLabel("Subtotal:"), BorderLayout.WEST);
        JLabel subtotalValue = new JLabel("₹0.00", SwingConstants.RIGHT);
        subtotalValue.setForeground(PRICE_COLOR);
        subtotalLabel.add(subtotalValue, BorderLayout.EAST);
        totalsPanel.add(subtotalLabel, gbc);
        
        gbc.gridy++;
        taxLabel = new JLabel();
        taxLabel.setLayout(new BorderLayout());
        taxLabel.add(new JLabel("GST (5%):"), BorderLayout.WEST);
        JLabel taxValue = new JLabel("₹0.00", SwingConstants.RIGHT);
        taxValue.setForeground(PRICE_COLOR);
        taxLabel.add(taxValue, BorderLayout.EAST);
        totalsPanel.add(taxLabel, gbc);
        
        gbc.gridy++;
        totalsPanel.add(new JSeparator(), gbc);
        
        gbc.gridy++;
        totalLabel = new JLabel();
        totalLabel.setLayout(new BorderLayout());
        JLabel totalText = new JLabel("Total Amount:");
        totalText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalText.setForeground(new Color(80, 60, 40));
        totalLabel.add(totalText, BorderLayout.WEST);
        JLabel totalValue = new JLabel("₹0.00", SwingConstants.RIGHT);
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalValue.setForeground(PRICE_COLOR);
        totalLabel.add(totalValue, BorderLayout.EAST);
        totalsPanel.add(totalLabel, gbc);

        // Checkout Button
        JButton checkoutButton = createStyledButton("PROCEED TO PAYMENT", GREEN);
        checkoutButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        checkoutButton.setPreferredSize(new Dimension(300, 50));
        checkoutButton.addActionListener(e -> checkout());

        // Main Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPanel, cartPanel);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        centerPanel.add(totalsPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(checkoutButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(
                    Math.max(0, bgColor.getRed() - 30),
                    Math.max(0, bgColor.getGreen() - 30),
                    Math.max(0, bgColor.getBlue() - 30)
                ));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private JPanel createMenuItemPanel(FoodItem item) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 190, 180)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10))
        );
        panel.setBackground(new Color(255, 255, 255, 220));
        panel.setMaximumSize(new Dimension(600, 80));

        // Left side - Image
        try {
            ImageIcon icon = imageCache.getOrDefault(
                item.getImagePath() + "_thumb", 
                imageCache.get(item.getImagePath())
            );
            
            if (icon == null) {
                icon = loadFoodImage(item.getImagePath(), item.getName());
                Image scaledImage = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImage);
                imageCache.put(item.getImagePath() + "_thumb", icon);
            }
            
            JLabel imageLabel = new JLabel(icon);
            panel.add(imageLabel, BorderLayout.WEST);
        } catch (Exception e) {
            panel.add(new JLabel("No Image"), BorderLayout.WEST);
        }

        // Center - Item info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(new Color(70, 50, 40));
        
        JLabel descLabel = new JLabel("<html><i>" + item.getDescription() + "</i></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 100, 100));
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(descLabel);
        panel.add(infoPanel, BorderLayout.CENTER);

        // Right side - Price and Add button
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        
        // Price panel
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pricePanel.setOpaque(false);
        
        if (item.getDiscountPercentage() > 0) {
            JLabel originalPriceLabel = new JLabel(rupeeFormat.format(item.getOriginalPrice())) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.GRAY);
                    int y = getHeight() / 2;
                    g2.drawLine(0, y, getWidth(), y);
                }
            };
            originalPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            originalPriceLabel.setForeground(Color.GRAY);
            originalPriceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            
            JLabel discountedPrice = new JLabel(rupeeFormat.format(item.getPrice()));
            discountedPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
            discountedPrice.setForeground(PRICE_COLOR);
            
            pricePanel.add(originalPriceLabel);
            pricePanel.add(discountedPrice);
        } else {
            JLabel priceLabel = new JLabel(rupeeFormat.format(item.getPrice()));
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            priceLabel.setForeground(PRICE_COLOR);
            pricePanel.add(priceLabel);
        }
        
        rightPanel.add(pricePanel, BorderLayout.CENTER);
        
        // Add button
        JButton addButton = createStyledButton("ADD +", GREEN);
        addButton.setPreferredSize(new Dimension(80, 30));
        addButton.addActionListener(e -> addToCart(item));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    class CartItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                    boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            panel.setBackground(isSelected ? new Color(220, 230, 240) : Color.WHITE);
            
            String[] parts = value.toString().split(" - ₹");
            JLabel nameLabel = new JLabel(parts[0]);
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            JLabel priceLabel = new JLabel("₹" + parts[1]);
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            priceLabel.setForeground(PRICE_COLOR);
            
            panel.add(nameLabel, BorderLayout.WEST);
            panel.add(priceLabel, BorderLayout.EAST);
            
            return panel;
        }
    }

    private void addToCart(FoodItem item) {
        cartModel.addElement(String.format("%s - ₹%.2f", item.getName(), item.getPrice()));
        subtotal += item.getPrice();
        updateTotals();
    }

    private void removeSelectedItem() {
        int selectedIndex = cartList.getSelectedIndex();
        if (selectedIndex != -1) {
            String item = cartModel.get(selectedIndex);
            double price = Double.parseDouble(item.split("₹")[1]);
            subtotal -= price; // Fixed: Subtract the price, not PRICE_COLOR
            cartModel.remove(selectedIndex);
            updateTotals();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select an item to remove", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearCart() {
        if (cartModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Cart is already empty", 
                "Empty Cart", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear your cart?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            cartModel.clear();
            subtotal = 0.0;
            updateTotals();
        }
    }

    private void updateTotals() {
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;
        
        ((JLabel)((BorderLayout)subtotalLabel.getLayout()).getLayoutComponent(BorderLayout.EAST)).setText("₹" + String.format("%.2f", subtotal));
        ((JLabel)((BorderLayout)taxLabel.getLayout()).getLayoutComponent(BorderLayout.EAST)).setText("₹" + String.format("%.2f", tax));
        ((JLabel)((BorderLayout)totalLabel.getLayout()).getLayoutComponent(BorderLayout.EAST)).setText("₹" + String.format("%.2f", total));
    }

    private void checkout() {
        if (cartModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Your cart is empty!", 
                "Checkout", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String customerName = JOptionPane.showInputDialog(this, 
            "Please enter your name for the receipt:", 
            "Customer Name", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = "Guest";
        }

        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        JDialog checkoutDialog = new JDialog(this, "Confirm Payment", true);
        checkoutDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        checkoutDialog.getContentPane().setBackground(Color.WHITE);

        JPanel receiptPanel = new JPanel(new BorderLayout());
        receiptPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        receiptPanel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(34, 139, 34));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel restaurantLabel = new JLabel("GREEN CHILLI", JLabel.CENTER);
        restaurantLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        restaurantLabel.setForeground(Color.WHITE);
        headerPanel.add(restaurantLabel, BorderLayout.NORTH);
        
        JLabel taglineLabel = new JLabel("A Bait of Flavours", JLabel.CENTER);
        taglineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        taglineLabel.setForeground(Color.WHITE);
        headerPanel.add(taglineLabel, BorderLayout.CENTER);
        
        JLabel contactLabel = new JLabel("Kalyani, West Bengal, India | +91 6201840347", JLabel.CENTER);
        contactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contactLabel.setForeground(Color.WHITE);
        headerPanel.add(contactLabel, BorderLayout.SOUTH);
        
        receiptPanel.add(headerPanel, BorderLayout.NORTH);
        
        JTextArea itemsArea = new JTextArea();
        itemsArea.setEditable(false);
        itemsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        itemsArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        StringBuilder receiptText = new StringBuilder();
        receiptText.append("==================== GREEN CHILLI ====================\n");
        receiptText.append("================= A Bait of Flavours ==================\n\n");
        receiptText.append("--------------------------------------------------\n");
        receiptText.append(String.format("%-40s\n", "RECEIPT"));
        receiptText.append("--------------------------------------------------\n");
        receiptText.append(String.format("%-30s %s\n", "Customer Name:", customerName));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
        java.util.Date currentDate = new java.util.Date();
        receiptText.append(String.format("%-30s %s\n", "Date:", dateFormat.format(currentDate)));
        receiptText.append(String.format("%-30s %s\n", "Time:", timeFormat.format(currentDate)));
        
        receiptText.append("--------------------------------------------------\n");
        receiptText.append(String.format("%-30s %10s\n", "ITEM", "PRICE"));
        receiptText.append("--------------------------------------------------\n");
        
        for (int i = 0; i < cartModel.size(); i++) {
            String item = cartModel.get(i);
            String[] parts = item.split(" - ₹");
            receiptText.append(String.format("%-30s ₹%10s\n", parts[0], parts[1]));
        }
        
        receiptText.append("--------------------------------------------------\n");
        receiptText.append(String.format("%-30s ₹%10.2f\n", "SUBTOTAL:", subtotal));
        receiptText.append(String.format("%-30s ₹%10.2f\n", "GST (5%):", tax));
        receiptText.append("--------------------------------------------------\n");
        receiptText.append(String.format("%-30s ₹%10.2f\n", "TOTAL:", total));
        receiptText.append("--------------------------------------------------\n\n");
        receiptText.append("Payment Method: Cash/Card\n");
        receiptText.append("Thank you for dining with us at Green Chilli!\n");
        
        itemsArea.setText(receiptText.toString());
        receiptPanel.add(new JScrollPane(itemsArea), BorderLayout.CENTER);
        
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(240, 240, 240));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton printButton = createStyledButton("Print Receipt", BLUE);
        printButton.addActionListener(e -> {
            try {
                itemsArea.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(checkoutDialog,
                    "Error printing receipt: " + ex.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton confirmButton = createStyledButton("Confirm Payment", GREEN);
        confirmButton.addActionListener(e -> {
            checkoutDialog.dispose();
            storeReceiptInDatabase(subtotal, tax, total);
            processPayment(total);
        });
        
        JButton cancelButton = createStyledButton("Cancel", RED);
        cancelButton.addActionListener(e -> checkoutDialog.dispose());
        
        footerPanel.add(printButton);
        footerPanel.add(Box.createHorizontalStrut(20));
        footerPanel.add(confirmButton);
        footerPanel.add(Box.createHorizontalStrut(20));
        footerPanel.add(cancelButton);
        
        receiptPanel.add(footerPanel, BorderLayout.SOUTH);
        
        checkoutDialog.add(receiptPanel);
        checkoutDialog.pack();
        checkoutDialog.setSize(550, 600);
        checkoutDialog.setLocationRelativeTo(this);
        checkoutDialog.setVisible(true);
    }

    private void storeReceiptInDatabase(double subtotal, double tax, double total) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                String orderSql = "INSERT INTO orders (order_date, subtotal, tax, total) VALUES (?, ?, ?, ?)";
                PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
                orderStmt.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
                orderStmt.setDouble(2, subtotal);
                orderStmt.setDouble(3, tax);
                orderStmt.setDouble(4, total);
                
                int affectedRows = orderStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }
                
                int orderId;
                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained.");
                    }
                }
                
                String itemSql = "INSERT INTO order_items (order_id, food_item_id, item_name, item_price) " +
                               "VALUES (?, ?, ?, ?)";
                PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                
                for (int i = 0; i < cartModel.size(); i++) {
                    String item = cartModel.get(i);
                    String[] parts = item.split(" - ₹");
                    String itemName = parts[0];
                    double itemPrice = Double.parseDouble(parts[1]);
                    
                    FoodItem foodItem = foodItems.stream()
                        .filter(f -> f.getName().equals(itemName))
                        .findFirst()
                        .orElse(null);
                    
                    if (foodItem != null) {
                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, foodItem.getId());
                        itemStmt.setString(3, foodItem.getName());
                        itemStmt.setDouble(4, foodItem.getPrice());
                        itemStmt.addBatch();
                    }
                }
                
                itemStmt.executeBatch();
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error saving receipt to database: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processPayment(double total) {
        JDialog paymentDialog = new JDialog(this, "Payment Complete", true);
        paymentDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        paymentDialog.getContentPane().setBackground(Color.WHITE);
        
        JPanel paymentPanel = new JPanel(new BorderLayout());
        paymentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        try {
            ImageIcon successIcon = new ImageIcon(getClass().getResource("/com/restaurant/restaurantpremium/resources/success.png"));
            JLabel iconLabel = new JLabel(successIcon);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            paymentPanel.add(iconLabel, BorderLayout.NORTH);
        } catch (Exception e) {
            System.out.println("Success icon not found");
        }
        
        JLabel messageLabel = new JLabel("<html><center>Payment Successful!<br>₹" + 
            String.format("%.2f", total) + " has been charged.</center></html>", 
            SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        paymentPanel.add(messageLabel, BorderLayout.CENTER);
        
        JButton closeButton = createStyledButton("Finish", GREEN);
        closeButton.addActionListener(e -> {
            cartModel.clear();
            subtotal = 0.0;
            updateTotals();
            paymentDialog.dispose();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        paymentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        paymentDialog.add(paymentPanel);
        paymentDialog.pack();
        paymentDialog.setSize(400, 300);
        paymentDialog.setLocationRelativeTo(this);
        paymentDialog.setVisible(true);
    }

    private ImageIcon loadFoodImage(String imageName, String foodName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            return createPlaceholderIcon(foodName);
        }
        
        String cleanName = imageName.trim();
        if (cleanName.contains("/")) {
            cleanName = cleanName.substring(cleanName.lastIndexOf("/") + 1);
        }
        if (cleanName.contains("\\")) {
            cleanName = cleanName.substring(cleanName.lastIndexOf("\\") + 1);
        }
        
        URL imageUrl = getClass().getClassLoader().getResource("images/" + cleanName);
        if (imageUrl == null) {
            return createPlaceholderIcon(foodName);
        }
        
        ImageIcon originalIcon = new ImageIcon(imageUrl);
        if (originalIcon.getIconWidth() <= 0) {
            return createPlaceholderIcon(foodName);
        }
        
        return originalIcon;
    }

    private ImageIcon createPlaceholderIcon(String foodName) {
        int width = 70;
        int height = 70;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        
        String[] lines = {
            "Image Not",
            "Found"
        };
        
        FontMetrics fm = g.getFontMetrics();
        int y = (height - (fm.getHeight() * lines.length)) / 2 + fm.getAscent();
        
        for (String line : lines) {
            int x = (width - fm.stringWidth(line)) / 2;
            g.drawString(line, x, y);
            y += fm.getHeight();
        }
        
        g.dispose();
        return new ImageIcon(image);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
                
                UIManager.put("OptionPane.background", Color.WHITE);
                UIManager.put("Panel.background", Color.WHITE);
                UIManager.put("Viewport.background", Color.WHITE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            BillingSystem billingSystem = new BillingSystem();
            billingSystem.setVisible(true);
        });
    }
}