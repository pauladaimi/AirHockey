package com.example.user.airhockey;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * Created by User on 3/14/2018.
 */
//This is the Hockey Ball
public class HockeyBall implements GameObject {
    private RectF ball;
    private int ballColor;

    float originalSize;

    //Creates a certain ball with a certain color
    public HockeyBall(RectF ball, int ballColor){
        this.ball=ball;
        this.ballColor=ballColor;
        originalSize=ball.height();
    }

    public RectF getBall(){
        return ball;
    }

    //draws ball onto the canvas
    @Override
    public void draw(Canvas canvas) {
        Paint paint= new Paint();
        paint.setColor(ballColor);
        canvas.drawOval(ball,paint);
    }

    //updates ball position according to a certain point
    public void update(Point point) {
        float mult=ScreenConstants.getMultiplier(point);
        ball.set(point.x-originalSize*mult/2, point.y - originalSize*mult/2,point.x+originalSize*mult/2, point.y + originalSize*mult/2 );
    }

    //return wether the ball intersected with a certain mallet
    public Boolean intersects(HockeyMallet mallet){
        float malletRadius = mallet.getMallet().width()/2;
        float malletX = mallet.getMallet().centerX();
        float malletY=mallet.getMallet().centerY();

        double centerDist=Math.sqrt(Math.pow(ball.centerX()-malletX,2)+Math.pow(ball.centerY()-malletY,2));

        return(centerDist<=malletRadius+(ball.width()/2));
    }

}
