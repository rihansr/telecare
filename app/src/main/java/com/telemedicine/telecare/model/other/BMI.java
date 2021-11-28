package com.telemedicine.telecare.model.other;

import java.io.Serializable;

public class BMI implements Serializable {

    private Double          value;
    private String          category;
    private Double          lbm;
    private Double          lfm;
    private ActivityLevel   activityLevel;
    private Limit           calorie;
    private Limit           protein;
    private Limit           carbohydrate;
    private Limit           fat;

    public BMI() {}

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getLbm() {
        return lbm;
    }

    public void setLbm(Double lbm) {
        this.lbm = lbm;
    }

    public Double getLfm() {
        return lfm;
    }

    public void setLfm(Double lfm) {
        this.lfm = lfm;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public Limit getCalorie() {
        return calorie;
    }

    public void setCalorie(Limit calorie) {
        this.calorie = calorie;
    }

    public Limit getProtein() {
        return protein;
    }

    public void setProtein(Limit protein) {
        this.protein = protein;
    }

    public Limit getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(Limit carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public Limit getFat() {
        return fat;
    }

    public void setFat(Limit fat) {
        this.fat = fat;
    }
}
