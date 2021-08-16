package com.sample.productdetailssample;

public class Product {
    private String id;
    private String title;
    private String description;
    private String image_url;
    private String price;

    public Product() {
    }

    public Product(String id, String title, String description, String image_url, String price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image_url = image_url;
        this.price = price;
    }

    public Product(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
