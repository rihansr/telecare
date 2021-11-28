package com.telemedicine.telecare.model.food;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Food implements Serializable {

    public static String NAME = "name";

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("photo")
    @Expose
    private String photo;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("quantity")
    @Expose
    private Double quantity;
    @SerializedName("quantityUnit")
    @Expose
    private String quantityUnit;
    @SerializedName("calories")
    @Expose
    private Double calories;
    @SerializedName("fat")
    @Expose
    private Double fat;
    @SerializedName("carboHydrate")
    @Expose
    private Double carboHydrate;
    @SerializedName("sugars")
    @Expose
    private Double sugars;
    @SerializedName("protein")
    @Expose
    private Double protein;
    @SerializedName("vitaminA")
    @Expose
    private Double vitaminA;
    @SerializedName("vitaminC")
    @Expose
    private Double vitaminC;
    @SerializedName("calcium")
    @Expose
    private Double calcium;
    @SerializedName("saturatedFat")
    @Expose
    private Double saturatedFat;
    @SerializedName("cholesterol")
    @Expose
    private Double cholesterol;
    @SerializedName("additionalInfo")
    @Expose
    private String additionalInfo;

    public Food() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getCarboHydrate() {
        return carboHydrate;
    }

    public void setCarboHydrate(Double carboHydrate) {
        this.carboHydrate = carboHydrate;
    }

    public Double getSugars() {
        return sugars;
    }

    public void setSugars(Double sugars) {
        this.sugars = sugars;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getVitaminA() {
        return vitaminA;
    }

    public void setVitaminA(Double vitaminA) {
        this.vitaminA = vitaminA;
    }

    public Double getVitaminC() {
        return vitaminC;
    }

    public void setVitaminC(Double vitaminC) {
        this.vitaminC = vitaminC;
    }

    public Double getCalcium() {
        return calcium;
    }

    public void setCalcium(Double calcium) {
        this.calcium = calcium;
    }

    public Double getSaturatedFat() {
        return saturatedFat;
    }

    public void setSaturatedFat(Double saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public Double getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(Double cholesterol) {
        this.cholesterol = cholesterol;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return name == null ? "" : name;
    }
}
