package com.restaurant.restaurantpremium;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class Home extends JFrame {
    private boolean isAdmin;

    public Home(boolean isAdmin) {
        this.isAdmin = isAdmin;
        setTitle("Green Chilli - Premium Dining");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 1200, 800, 30, 30));

        initUI();
    }

    private void initUI() {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1200, 800));

        // Background image
        try {
            ImageIcon bgIcon = new ImageIcon(getClass().getResource("/images/background.jpg"));
            JLabel backgroundLabel = new JLabel(bgIcon);
            backgroundLabel.setBounds(0, 0, 1200, 800);
            layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);
        } catch (Exception e) {
            layeredPane.setBackground(new Color(240, 240, 240));
        }

        // Close button
        JButton closeButton = createCloseButton();
        closeButton.setBounds(1140, 20, 35, 35);
        layeredPane.add(closeButton, JLayeredPane.PALETTE_LAYER);

        // Navigation buttons
        JPanel buttonPanel = createNavigationButtons();
        buttonPanel.setBounds(50, 700, 1100, 80);
        layeredPane.add(buttonPanel, JLayeredPane.PALETTE_LAYER);

        // User role indicator
        JLabel roleLabel = new JLabel(isAdmin ? "ADMIN MODE" : "CUSTOMER MODE");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roleLabel.setForeground(isAdmin ? new Color(200, 0, 0) : new Color(0, 100, 0));
        roleLabel.setBounds(20, 20, 150, 30);
        layeredPane.add(roleLabel, JLayeredPane.PALETTE_LAYER);

        setContentPane(layeredPane);
    }

    private JPanel createNavigationButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        // Common buttons for all users
        addButton(buttonPanel, "MENU", e -> new FoodMenu().setVisible(true));
        addButton(buttonPanel, "OFFERS", e -> new SpecialOffers().setVisible(true));
        addButton(buttonPanel, "BILLING", e -> new BillingSystem().setVisible(true));

        // Customer-only button
        if (!isAdmin) {
            addButton(buttonPanel, "REVIEWS", e -> new CustomerReviews().setVisible(true));
        }

        // Admin-only buttons
        if (isAdmin) {
            addButton(buttonPanel, "ADD ITEM", e -> new AddFoodItem().setVisible(true));
            addButton(buttonPanel, "DELETE ITEM", e -> new DeleteFoodItem().setVisible(true));
        }

        // Logout button (red)
        JButton logoutButton = new JButton("LOGOUT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color baseColor = new Color(200, 0, 0);
                g2.setColor(getModel().isRollover() ? baseColor.darker() : baseColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        logoutButton.setPreferredSize(new Dimension(150, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> {
            new RestaurantPremium().setVisible(true);
            dispose();
        });
        buttonPanel.add(logoutButton);

        return buttonPanel;
    }

    private void addButton(JPanel panel, String text, ActionListener action) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color baseColor = new Color(0, 100, 0);
                g2.setColor(getModel().isRollover() ? baseColor.darker() : baseColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(150, 60));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        panel.add(button);
    }

    private JButton createCloseButton() {
        JButton closeButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(new Color(200, 0, 0));
                } else {
                    g2.setColor(new Color(220, 20, 60));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.setStroke(new BasicStroke(2));
                g2.setColor(Color.WHITE);
                int margin = 14;
                g2.drawLine(margin, margin, getWidth() - margin, getHeight() - margin);
                g2.drawLine(margin, getHeight() - margin, getWidth() - margin, margin);

                g2.dispose();
            }
        };

        closeButton.setPreferredSize(new Dimension(35, 35));
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> System.exit(0));

        return closeButton;
    }
}