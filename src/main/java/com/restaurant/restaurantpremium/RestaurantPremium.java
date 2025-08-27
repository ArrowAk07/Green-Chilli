package com.restaurant.restaurantpremium;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class RestaurantPremium extends JFrame {
    private JRadioButton adminRadio, customerRadio;
    private JPasswordField passwordField;
    private JLabel passwordLabel;
    private JButton loginButton;

    public RestaurantPremium() {
        setTitle("Green Chilli - Login");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        // Removed the stray '*' character at the end of the following line.
        setShape(new RoundRectangle2D.Double(0, 0, 1200, 800, 30, 30));

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1200, 800));

        // Background image with fallback
        URL bgURL = getClass().getResource("/images/background.jpg");
        if (bgURL != null) {
            ImageIcon bgIcon = new ImageIcon(bgURL);
            JLabel backgroundLabel = new JLabel(bgIcon);
            backgroundLabel.setBounds(0, 0, 1200, 800);
            layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);
        } else {
            System.err.println("Background image not found, using solid color");
            JPanel solidPanel = new JPanel();
            solidPanel.setBackground(new Color(240, 240, 240));
            solidPanel.setBounds(0, 0, 1200, 800);
            layeredPane.add(solidPanel, JLayeredPane.DEFAULT_LAYER);
        }

        // Close button
        JButton closeButton = createCloseButton();
        closeButton.setBounds(1140, 20, 35, 35);
        layeredPane.add(closeButton, JLayeredPane.PALETTE_LAYER);

        // Login panel
        JPanel loginPanel = createLoginPanel();
        loginPanel.setBounds(350, 200, 500, 400);
        layeredPane.add(loginPanel, JLayeredPane.PALETTE_LAYER);

        setContentPane(layeredPane);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title
        JLabel titleLabel = new JLabel("WELCOME TO GREEN CHILLI", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 100, 0));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Select your role to continue", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);

        // User type selection
        JPanel radioPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        radioPanel.setOpaque(false);

        ButtonGroup userGroup = new ButtonGroup();
        adminRadio = new JRadioButton("Admin");
        customerRadio = new JRadioButton("Customer");
        userGroup.add(adminRadio);
        userGroup.add(customerRadio);
        customerRadio.setSelected(true);

        // Add listener to show/hide password field
        adminRadio.addActionListener(e -> togglePasswordField(true));
        customerRadio.addActionListener(e -> togglePasswordField(false));

        styleRadioButton(adminRadio);
        styleRadioButton(customerRadio);

        radioPanel.add(adminRadio);
        radioPanel.add(customerRadio);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 15, 5, 15);
        panel.add(radioPanel, gbc);

        // Password field (initially hidden for customer)
        passwordLabel = new JLabel("Admin Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setVisible(false);
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setVisible(false);
        styleTextField(passwordField);
        gbc.gridy = 4;
        panel.add(passwordField, gbc);

        // Login button
        loginButton = new JButton("CONTINUE");
        styleLoginButton(loginButton);
        loginButton.addActionListener(e -> handleLogin());
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 15, 5, 15);
        panel.add(loginButton, gbc);

        return panel;
    }

    private void togglePasswordField(boolean show) {
        passwordLabel.setVisible(show);
        passwordField.setVisible(show);
        revalidate();
        repaint();
    }

    private void styleRadioButton(JRadioButton radio) {
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        radio.setOpaque(false);
        radio.setFocusPainted(false);
        radio.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Safely load the radio button icons
        URL unselectedURL = getClass().getResource("/images/radio_unselected.png");
        URL selectedURL = getClass().getResource("/images/radio_selected.png");

        if (unselectedURL != null && selectedURL != null) {
            radio.setIcon(new ImageIcon(unselectedURL));
            radio.setSelectedIcon(new ImageIcon(selectedURL));
        } else {
            radio.setIcon(UIManager.getIcon("RadioButton.unselectedIcon"));
            radio.setSelectedIcon(UIManager.getIcon("RadioButton.selectedIcon"));
        }
    }

    private void styleTextField(JPasswordField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(Color.WHITE);
    }

    private void styleLoginButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 100, 0));
        button.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 120, 0));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 100, 0));
            }
        });
    }

    private void handleLogin() {
        boolean isAdmin = adminRadio.isSelected();

        if (isAdmin) {
            char[] password = passwordField.getPassword();
            if (password.length == 0) {
                JOptionPane.showMessageDialog(this, "Please enter admin password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Simple hardcoded password check (replace with secure logic in production)
            if (!String.valueOf(password).equals("admin123")) {
                JOptionPane.showMessageDialog(this, "Invalid admin password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Home home = new Home(isAdmin);
        home.setVisible(true);
        dispose();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                RestaurantPremium login = new RestaurantPremium();
                login.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
