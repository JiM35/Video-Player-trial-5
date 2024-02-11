package com.example.videoplayer_trial5;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeTouchListener implements View.OnTouchListener {

//    Create an object for gesture detector
    private final GestureDetector gestureDetector;

//    Create Constructor for this class
    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    public final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
//            When user will double tap on the screen we will call the method - onDoubleTouch and onSingleTouch
            onDoubleTouch();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            onSingleTouch();
            return super.onSingleTapConfirmed(e);
        }
    }

//    Create two methods for double and single tap - we call these two methods above
    public void onDoubleTouch() {

    }

    public void onSingleTouch() {

    }
}
