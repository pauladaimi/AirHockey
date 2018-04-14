package com.example.user.airhockey;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

/**
 * Created by User on 3/11/2018.
 */

public class HockeyMallet implements GameObject{
    private RectF mallet;
    private RectF handle;
    private int malletColor;
    private int handleColor;

    private float originalSize;
    private float originalHandleSize;

    public HockeyMallet(RectF mallet, int malletColor,int handleColor){
        this.mallet=mallet;
        originalSize=mallet.height();
        handle = new RectF(mallet.left/2,mallet.top/2,mallet.right/2,mallet.bottom/2);
        originalHandleSize=handle.height();
        this.handleColor=handleColor;
        this.malletColor=malletColor;
    }

    public RectF getMallet(){
        return mallet;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();

        paint.setColor(malletColor);
        canvas.drawOval(mallet,paint);

        paint.setColor(handleColor);
        canvas.drawOval(handle,paint);
    }

    @Override
    public void update() {

    }

    public void update(Point point){
        float mult=ScreenConstants.getMultiplier(point);
        mallet.set(point.x-originalSize*mult/2, point.y - originalSize*mult/2,point.x+originalSize*mult/2, point.y + originalSize*mult/2 );
        handle.set(point.x-originalHandleSize*mult/2, point.y - originalHandleSize*mult/2,point.x+originalHandleSize*mult/2, point.y + originalHandleSize*mult/2 );
    }
}
