package com.example.user.airhockey;

import android.graphics.Point;

/**
 * Created by User on 3/11/2018.
 */

public class ScreenConstants {
    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    public static float getMultiplier(Point point){
        return (float)((0.2/ScreenConstants.SCREEN_HEIGHT)*point.y + 0.8);
    }
}
