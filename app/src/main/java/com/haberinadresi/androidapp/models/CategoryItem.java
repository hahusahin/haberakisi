package com.haberinadresi.androidapp.models;


public class CategoryItem {

    private String categoryName;
    private int categoryImageId;
    private boolean isSelected;

    public CategoryItem(String categoryName, int categoryImageId, boolean isSelected) {
        this.categoryName = categoryName;
        this.categoryImageId = categoryImageId;
        this.isSelected = isSelected;
    }

    public CategoryItem(){
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getCategoryImageId() {
        return categoryImageId;
    }

    public boolean getIsSelected() { return isSelected; }

    public void setCategoryImageId(int resourceId){
        this.categoryImageId = resourceId;
    }

    public void setSelected(boolean isSelected){
        this.isSelected = isSelected;
    }

}
