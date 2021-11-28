package com.telemedicine.telecare.model.food;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class DailyFood implements Serializable {

    public static String DATE = "date";

    @SerializedName("foods")
    @Expose
    private List<FoodAmount> foods;
    @SerializedName("timing")
    @Expose
    private String timing;
    @ServerTimestamp
    private Date date;
    @SerializedName("totalCalories")
    @Expose
    private Double totalCalories;
    @SerializedName("totalFat")
    @Expose
    private Double totalFat;
    @SerializedName("totalCarboHydrate")
    @Expose
    private Double totalCarboHydrate;
    @SerializedName("totalSugars")
    @Expose
    private Double totalSugars;
    @SerializedName("totalProtein")
    @Expose
    private Double totalProtein;
    @SerializedName("totalVitaminA")
    @Expose
    private Double totalVitaminA;
    @SerializedName("totalVitaminC")
    @Expose
    private Double totalVitaminC;
    @SerializedName("totalCalcium")
    @Expose
    private Double totalCalcium;
    @SerializedName("totalSaturatedFat")
    @Expose
    private Double totalSaturatedFat;
    @SerializedName("totalCholesterol")
    @Expose
    private Double totalCholesterol;

    public DailyFood() {}

    public List<FoodAmount> getFoods() {
        return foods;
    }

    public void setFoods(List<FoodAmount> foods) {
        this.foods = foods;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(Double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public Double getTotalFat() {
        return totalFat;
    }

    public void setTotalFat(Double totalFat) {
        this.totalFat = totalFat;
    }

    public Double getTotalCarboHydrate() {
        return totalCarboHydrate;
    }

    public void setTotalCarboHydrate(Double totalCarboHydrate) {
        this.totalCarboHydrate = totalCarboHydrate;
    }

    public Double getTotalSugars() {
        return totalSugars;
    }

    public void setTotalSugars(Double totalSugars) {
        this.totalSugars = totalSugars;
    }

    public Double getTotalProtein() {
        return totalProtein;
    }

    public void setTotalProtein(Double totalProtein) {
        this.totalProtein = totalProtein;
    }

    public Double getTotalVitaminA() {
        return totalVitaminA;
    }

    public void setTotalVitaminA(Double totalVitaminA) {
        this.totalVitaminA = totalVitaminA;
    }

    public Double getTotalVitaminC() {
        return totalVitaminC;
    }

    public void setTotalVitaminC(Double totalVitaminC) {
        this.totalVitaminC = totalVitaminC;
    }

    public Double getTotalCalcium() {
        return totalCalcium;
    }

    public void setTotalCalcium(Double totalCalcium) {
        this.totalCalcium = totalCalcium;
    }

    public Double getTotalSaturatedFat() {
        return totalSaturatedFat;
    }

    public void setTotalSaturatedFat(Double totalSaturatedFat) {
        this.totalSaturatedFat = totalSaturatedFat;
    }

    public Double getTotalCholesterol() {
        return totalCholesterol;
    }

    public void setTotalCholesterol(Double totalCholesterol) {
        this.totalCholesterol = totalCholesterol;
    }
}
