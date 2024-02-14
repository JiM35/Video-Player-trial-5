package com.example.videoplayer_trial5;

import android.annotation.SuppressLint;

public abstract class Utility {
//    Create a method that will convert the  milliseconds to hours, minutes and seconds
//    In VideoFilesAdapter.java we have the method timeConversion - that will convert the millis in hours, minutes and seconds. Cut the method and paste it here
//    We have to change the milliseconds into hours, minutes and seconds according to the duration of video. For this we have to create a method - timeConversion

//    We can access the timeConversion method all over the app
    @SuppressLint("DefaultLocale")
    public static String timeConversion(Long millie) {
        if (millie != null) {
            long seconds = (millie / 1000);  // It will convert milliseconds to 1 second
            long sec = seconds % 60;
            long min = (seconds / 60) % 60;  // Converts all the seconds to minutes
            long hrs = (seconds / (60 * 60)) % 24;  // Converts to hours
            if (hrs > 0) {  // If hours is greater than 0, means the video has the duration more than 1 hr, then we will return it to
                return String.format("%02d:%02d:%02d", hrs, min, sec);
            } else {
                return String.format("%02d:%02d", min, sec);  // Means user does not have duration more than 1 hr
            }
        } else {
            return null;
        }
    }
}
