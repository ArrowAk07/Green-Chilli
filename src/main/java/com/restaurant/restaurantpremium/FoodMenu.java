package com.restaurant.restaurantpremium;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodMenu extends JFrame {
    private List<FoodItem> foodItems = new ArrayList<>();

    public FoodMenu() {
        setTitle("Premium Restaurant - Menu");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        loadFoodItems();
        initUI();
    }

    private void loadFoodItems() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT f.*, COALESCE(AVG(r.rating), 0.0) AS avg_rating, " +
                       "COALESCE(f.original_price, f.price) AS original_price, " +
                       "COALESCE(f.discount_percentage, 0) AS discount_percentage " +
                       "FROM food_items f LEFT JOIN reviews r ON f.id = r.food_id " +
                       "GROUP BY f.id";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
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

                try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT comment FROM reviews WHERE food_id = ?")) {
                    pstmt.setInt(1, item.getId());
                    ResultSet reviewRs = pstmt.executeQuery();

                    while (reviewRs.next()) {
                        item.addReview(reviewRs.getString("comment"));
                    }
                }

                foodItems.add(item);
                // Debug logging to verify item data
                System.out.println("Loaded item: " + item.getName() + 
                                   ", Price: " + item.getPrice() + 
                                   ", Original Price: " + item.getOriginalPrice() + 
                                   ", Discount Percentage: " + item.getDiscountPercentage());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading menu: " + e.getMessage());
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 30, 30));
        headerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("OUR PREMIUM MENU");
        titleLabel.setFont(new Font("Playfair Display", Font.BOLD, 32));
        titleLabel.setForeground(new Color(230, 230, 230));
        headerPanel.add(titleLabel);

        // Menu Items Grid
        JPanel menuPanel = new JPanel(new GridLayout(0, 2, 25, 25));
        menuPanel.setBorder(new EmptyBorder(30, 30, 40, 30));
        menuPanel.setBackground(new Color(248, 248, 248));

        for (FoodItem item : foodItems) {
            menuPanel.add(createFoodCard(item));
        }

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createFoodCard(FoodItem item) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220)), 
            new EmptyBorder(15, 15, 15, 15)
        ));

        // Image Section
        ImageIcon foodIcon = loadFoodImage(item.getImagePath(), item.getName());
        JLabel imageLabel = new JLabel(foodIcon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Details Section
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);

        // Food Name
        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Playfair Display", Font.BOLD, 22));
        nameLabel.setForeground(new Color(40, 40, 40));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Description
        JLabel descLabel = new JLabel("<html><div style='width:280px;color:#555555;font-family:Open Sans;font-size:12pt;line-height:1.4'>" 
            + item.getDescription() + "</div></html>");
        descLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Price and Offer
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pricePanel.setBackground(Color.WHITE);
        pricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        double originalPrice = item.getOriginalPrice();
        double discountedPrice = item.getPrice();
        double discountPercentage = item.getDiscountPercentage();
        boolean hasDiscount = discountPercentage > 0;

        // Debug logging to check discount logic
        if (hasDiscount) {
            System.out.println("Item with discount: " + item.getName() + 
                               ", Original Price: " + originalPrice + 
                               ", Discounted Price: " + discountedPrice + 
                               ", Discount Percentage: " + discountPercentage);
        } else {
            System.out.println("Item without discount: " + item.getName() + 
                               ", Price: " + discountedPrice);
        }

        // Original Price
        JLabel originalPriceLabel = new JLabel(String.format("₹%.2f", originalPrice));
        originalPriceLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        if (hasDiscount) {
            originalPriceLabel.setText("<html><s>" + String.format("₹%.2f", originalPrice) + "</s></html>");
            originalPriceLabel.setForeground(new Color(150, 150, 150));
        } else {
            originalPriceLabel.setForeground(new Color(0, 100, 0));
        }
        pricePanel.add(originalPriceLabel);

        // Discounted Price
        if (hasDiscount) {
            JLabel discountedPriceLabel = new JLabel("  " + String.format("₹%.2f", discountedPrice));
            discountedPriceLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
            discountedPriceLabel.setForeground(new Color(0, 100, 0));
            pricePanel.add(discountedPriceLabel);

            JLabel offerLabel = new JLabel("  " + (int)discountPercentage + "% OFF");
            offerLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
            offerLabel.setForeground(new Color(200, 0, 0));
            offerLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
            pricePanel.add(offerLabel);
        }

        // Rating Stars
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        ratingPanel.setBackground(Color.WHITE);
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingPanel.setBorder(new EmptyBorder(10, 0, 15, 0));

        JLabel ratingValue = new JLabel(String.format("%.1f", item.getAvgRating()));
        ratingValue.setFont(new Font("Montserrat", Font.BOLD, 14));
        ratingValue.setForeground(new Color(70, 70, 70));
        ratingPanel.add(ratingValue);

        int roundedRating = (int) Math.round(item.getAvgRating());
        for (int i = 0; i < 5; i++) {
            JLabel star = new JLabel(i < roundedRating ? "★" : "☆");
            star.setFont(new Font("Arial Unicode MS", Font.PLAIN, 18));
            star.setForeground(i < roundedRating ? new Color(255, 180, 0) : new Color(200, 200, 200));
            ratingPanel.add(star);
        }

        // Reviews Section
        JPanel reviewsPanel = new JPanel();
        reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));
        reviewsPanel.setBackground(Color.WHITE);
        reviewsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (!item.getReviews().isEmpty()) {
            JLabel reviewsTitle = new JLabel("Customer Reviews:");
            reviewsTitle.setFont(new Font("Open Sans", Font.BOLD, 12));
            reviewsTitle.setForeground(new Color(100, 100, 100));
            reviewsTitle.setBorder(new EmptyBorder(0, 0, 5, 0));
            reviewsPanel.add(reviewsTitle);

            int maxReviews = Math.min(2, item.getReviews().size());
            for (int i = 0; i < maxReviews; i++) {
                JLabel reviewLabel = new JLabel("<html><div style='width:280px;font-family:Open Sans;font-size:11px;color:#666666;font-style:italic;margin-top:3px'>" 
                    + "• " + item.getReviews().get(i) + "</div></html>");
                reviewLabel.setBorder(new EmptyBorder(2, 10, 2, 0));
                reviewsPanel.add(reviewLabel);
            }

            if (item.getReviews().size() > 2) {
                JButton seeAllBtn = new JButton("View All Reviews (" + item.getReviews().size() + ")");
                seeAllBtn.setFont(new Font("Open Sans", Font.PLAIN, 11));
                seeAllBtn.setBackground(new Color(60, 60, 60));
                seeAllBtn.setForeground(Color.WHITE);
                seeAllBtn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(100, 100, 100)), 
                    new EmptyBorder(3, 15, 3, 15)
                ));
                seeAllBtn.setFocusPainted(false);
                seeAllBtn.addActionListener(e -> showAllReviews(item));
                seeAllBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                reviewsPanel.add(Box.createVerticalStrut(8));
                reviewsPanel.add(seeAllBtn);
            }
        }

        // Assemble Components
        detailsPanel.add(nameLabel);
        detailsPanel.add(descLabel);
        detailsPanel.add(pricePanel);
        detailsPanel.add(ratingPanel);
        detailsPanel.add(reviewsPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(imageLabel, BorderLayout.NORTH);
        contentPanel.add(detailsPanel, BorderLayout.CENTER);
        card.add(contentPanel, BorderLayout.CENTER);

        // Special Badge
        if (item.isSpecial()) {
            JLabel specialLabel = new JLabel("CHEF'S SPECIAL");
            specialLabel.setFont(new Font("Montserrat", Font.BOLD, 12));
            specialLabel.setForeground(Color.WHITE);
            specialLabel.setBackground(new Color(180, 40, 40));
            specialLabel.setOpaque(true);
            specialLabel.setHorizontalAlignment(SwingConstants.CENTER);
            specialLabel.setBorder(new EmptyBorder(3, 10, 3, 10));
            card.add(specialLabel, BorderLayout.NORTH);
        }

        return card;
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
        
        Image scaledImage = originalIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private ImageIcon createPlaceholderIcon(String foodName) {
        int width = 300;
        int height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);
        
        g.setColor(new Color(120, 120, 120));
        g.setFont(new Font("Open Sans", Font.BOLD, 16));
        
        String[] lines = {"Image Not Available", foodName};
        
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

    private void showAllReviews(FoodItem item) {
        if (item == null || item.getReviews() == null || item.getReviews().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No reviews available for this item");
            return;
        }

        JDialog reviewDialog = new JDialog(this, "All Reviews for " + item.getName(), true);
        reviewDialog.setSize(450, 500);
        reviewDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(250, 250, 250));
        
        JTextArea reviewsArea = new JTextArea();
        reviewsArea.setEditable(false);
        reviewsArea.setFont(new Font("Open Sans", Font.PLAIN, 13));
        reviewsArea.setLineWrap(true);
        reviewsArea.setWrapStyleWord(true);
        reviewsArea.setBackground(new Color(250, 250, 250));
        
        StringBuilder sb = new StringBuilder();
        sb.append("All Reviews for ").append(item.getName()).append("\n\n");
        sb.append("Average Rating: ").append(String.format("%.1f", item.getAvgRating())).append("/5.0\n\n");
        
        for (String review : item.getReviews()) {
            if (review != null && !review.isEmpty()) {
                sb.append("★ ").append(review).append("\n\n");
            }
        }
        reviewsArea.setText(sb.toString());
        
        JScrollPane scrollPane = new JScrollPane(reviewsArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        reviewDialog.add(panel);
        reviewDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new FoodMenu().setVisible(true);
        });
    }
}