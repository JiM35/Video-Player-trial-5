package com.example.videoplayer_trial5;

public class IconModel {
//    Variable will be for showing icon
    private int imageView;
//    For showing title of that icon (👆👆👆👆👆)
    private String iconTitle;

    public IconModel(int imageView, String iconTitle) {
        this.imageView = imageView;
        this.iconTitle = iconTitle;
    }

    public int getImageView() {
        return imageView;
    }

    public void setImageView(int imageView) {
        this.imageView = imageView;
    }

    public String getIconTitle() {
        return iconTitle;
    }

    public void setIconTitle(String iconTitle) {
        this.iconTitle = iconTitle;
    }
}
