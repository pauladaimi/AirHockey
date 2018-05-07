package com.example.user.airhockey;

import android.graphics.Point;

/**
 * Created by User on 3/11/2018.
 */

//Class that holds constants that will be used throughout the project
public class ScreenConstants {
    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

//returns the multiplier in which a Mallet or ball will be resized according to its Y position
    public static float getMultiplier(Point point){
        return (float)((0.4/ScreenConstants.SCREEN_HEIGHT)*point.y + 0.6);
    }
}
