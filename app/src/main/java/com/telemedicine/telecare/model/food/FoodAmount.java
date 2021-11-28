package com.telemedicine.telecare.model.food;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FoodAmount implements Serializable {

    public static String FOOD_ID = "foodId";

    @SerializedName("foodId")
    @Expose
    private String foodId;
    @SerializedName("foodName")
    @Expose
    private String foodName;
    @SerializedName("food")
    @Expose
    private Food food;
    @SerializedName("quantity")
    @Expose
    private Double quantity;
    @SerializedName("quantityUnit")
    @Expose
    private String quantityUnit;

    public FoodAmount() {}

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }
}
