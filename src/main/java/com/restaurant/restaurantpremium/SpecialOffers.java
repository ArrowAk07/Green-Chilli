package com.restaurant.restaurantpremium;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SpecialOffers extends JFrame {
    
    // Modern color palette
    private static final Color HEADER_BG = new Color(70, 130, 180);  // Cool steel blue
    private static final Color PRIMARY_GREEN = new Color(40, 167, 69);
    private static final Color HOVER_GREEN = new Color(33, 136, 56);
    private static final Color PRESSED_GREEN = new Color(25, 105, 44);
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color MAIN_BG = new Color(245, 248, 250);  // Light blue-gray
    
    public SpecialOffers() {
        setTitle("Premium Restaurant - Special Offers");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(MAIN_BG);
        
        // Header with cool new color
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        
        JLabel titleLabel = new JLabel("TODAY'S SPECIAL OFFERS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        
        // Add decorative elements
        JLabel iconLabel = new JLabel("✨");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLabel.setForeground(new Color(255, 215, 0)); // Gold
        
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(iconLabel, BorderLayout.EAST);
        headerPanel.add(Box.createHorizontalStrut(20), BorderLayout.WEST);
        
        // Offers panel
        JPanel offersPanel = new JPanel();
        offersPanel.setLayout(new BoxLayout(offersPanel, BoxLayout.Y_AXIS));
        offersPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));
        offersPanel.setBackground(MAIN_BG);
        
        // Add offers
        addOffer(offersPanel, "Family Feast", "25% off family meals", "4+ people", "All week");
        addOffer(offersPanel, "Happy Hour", "B1G1 drinks", "Includes alcohol", "4-7PM daily");
        addOffer(offersPanel, "Weekend Brunch", "15% off brunch", "Free coffee", "Weekends");
        addOffer(offersPanel, "Early Bird", "20% off before 6PM", "Excludes holidays", "Mon-Fri");
        addOffer(offersPanel, "Student Deal", "10% discount", "With ID", "Always");
        addOffer(offersPanel, "Birthday", "Free dessert", "$30+ purchase", "Show ID");
        addOffer(offersPanel, "Lunch Combo", "$15 meal deal", "3 choices", "Weekdays");
        addOffer(offersPanel, "Senior Discount", "15% off", "60+ only", "ID required");
        addOffer(offersPanel, "Chef's Pick", "20% off special", "Daily rotation", "Ask server");
        addOffer(offersPanel, "Loyalty", "10% members", "Card required", "No expiry");
        
        JScrollPane scrollPane = new JScrollPane(offersPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    private void addOffer(JPanel panel, String title, String desc, String details, String validity) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        // Add subtle shadow
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Title with accent color
        JLabel titleLabel = new JLabel("🍽️ " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(HEADER_BG.darker());  // Darker version of header color
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Description
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLabel.setForeground(new Color(70, 70, 70));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Details with bullet points
        JLabel detailsLabel = new JLabel("• " + details);
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailsLabel.setForeground(new Color(100, 100, 100));
        detailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Validity with icon
        JLabel validityLabel = new JLabel("⏱️ " + validity);
        validityLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        validityLabel.setForeground(new Color(120, 120, 120));
        validityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Green action button
        JButton claimBtn = createGreenButton();
        
        // Layout components with proper spacing
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(descLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(detailsLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(validityLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(claimBtn);
        
        panel.add(card);
        panel.add(Box.createVerticalStrut(15));
    }
    
    private JButton createGreenButton() {
        JButton button = new JButton("CLAIM OFFER") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(PRESSED_GREEN);
                } else if (getModel().isRollover()) {
                    g2.setColor(HOVER_GREEN);
                } else {
                    g2.setColor(PRIMARY_GREEN);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 35, 12, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFocusPainted(false);
        
        button.addActionListener(e -> {
            button.setText("✓ CLAIMED");
            button.setForeground(SUCCESS_GREEN);
            button.setEnabled(false);
            showSuccessMessage(button.getParent());
        });
        
        return button;
    }
    
    private void showSuccessMessage(Container parent) {
        JLabel message = new JLabel("Offer added to your order!", JLabel.CENTER);
        message.setFont(new Font("Segoe UI", Font.BOLD, 14));
        message.setForeground(SUCCESS_GREEN);
        message.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JOptionPane.showMessageDialog(this, message, "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SpecialOffers offers = new SpecialOffers();
            offers.setVisible(true);
            offers.setLocationRelativeTo(null);
        });
    }
}