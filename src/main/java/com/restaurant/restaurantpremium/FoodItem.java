package com.restaurant.restaurantpremium;

import java.util.ArrayList;
import java.util.List;

public class FoodItem {
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imagePath;
    private double avgRating;  // Corrected field name
    private boolean isSpecial;
    private double originalPrice;
    private double discountPercentage;
    private List<String> reviews = new ArrayList<>();

    // Constructor
    public FoodItem(int id, String name, String description, double price, String category,
                    String imagePath, double avgRating, boolean isSpecial,
                    double originalPrice, double discountPercentage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
        this.avgRating = avgRating;
        this.isSpecial = isSpecial;
        this.originalPrice = originalPrice;
        this.discountPercentage = discountPercentage;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }
    public double getAvgRating() { return avgRating; }  // Fixed method name
    public boolean isSpecial() { return isSpecial; }
    public List<String> getReviews() { return reviews; }
    public double getOriginalPrice() { return originalPrice; }  // Fixed implementation
    public double getDiscountPercentage() { return discountPercentage; }  // Fixed implementation

    // Add a review
    public void addReview(String review) {
        reviews.add(review);
    }
}
