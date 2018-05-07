package com.example.user.airhockey;

import android.graphics.Canvas;
import android.graphics.Point;

/**
 * Created by User on 3/11/2018.
 */

//Interface shared by all objects that can be drawn
public interface GameObject {
    public void draw(Canvas canvas);
    public void update(Point point);
}
