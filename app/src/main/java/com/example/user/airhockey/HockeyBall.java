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

public class HockeyBall implements GameObject {
    private RectF ball;
    private int ballColor;

    float originalSize;

    public HockeyBall(RectF ball, int ballColor){
        this.ball=ball;
        this.ballColor=ballColor;
        originalSize=ball.height();
    }

    public RectF getBall(){
        return ball;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint= new Paint();
        paint.setColor(ballColor);
        canvas.drawOval(ball,paint);
    }

    @Override
    public void update() {

    }

    public void update(Point point) {
        float mult=ScreenConstants.getMultiplier(point);
        ball.set(point.x-originalSize*mult/2, point.y - originalSize*mult/2,point.x+originalSize*mult/2, point.y + originalSize*mult/2 );
    }

    public String intersect(HockeyMallet mallet){
        return intersect(mallet.getMallet(),ball);
    }


    private String intersect(RectF oval, RectF shape) {
        Region clip = new Region(0, 0, ScreenConstants.SCREEN_WIDTH, ScreenConstants.SCREEN_HEIGHT);

        Path circle = new Path();
        circle.addOval(shape, Path.Direction.CCW);

        Path path = new Path();
        path.addOval(oval, Path.Direction.CCW);

        Region region1 = new Region();
        region1.setPath(path, clip);

        //return (!region1.quickReject(region2) && region1.op(region2, Region.Op.INTERSECT));
        //return  !region1.quickContains((int)player.getMallet().left,(int)player.getMallet().top,(int)player.getMallet().right,(int)player.getMallet().bottom);
        int xleft = (int) (shape.centerX() - shape.width() / 2);
        int yleft = (int) (shape.centerY());

        int xtop = (int) (shape.centerX());
        int ytop = (int) (shape.centerY() - shape.height() / 2);

        int xright = (int) (shape.centerX() + shape.width() / 2);
        int yright = (int) (shape.centerY());

        int xbottom = (int) (shape.centerX());
        int ybottom = (int) (shape.centerY() + shape.height() / 2);

        if (region1.contains((xleft+xtop)/2, (yleft+ytop)/2) /*&& region1.contains(xtop, ytop)*/) {
            return "left-top";
        } else if (region1.contains((xleft+xbottom)/2, (yleft+ybottom)/2) /*&& region1.contains(xbottom, ybottom)*/) {
            return "left-bottom";
        } else if (region1.contains((xright+xtop)/2, (yright+ytop)/2)/* && region1.contains(xtop, ytop)*/) {
            return "right-top";
        } else if (region1.contains((xright+xbottom)/2, (yright+ybottom)/2) /*&& region1.contains(xbottom, ybottom)*/) {
            return "right-bottom";
        } else if (region1.contains(xright, yright)) {
            return "right";
        } else if (region1.contains(xleft, yleft)) {
            return "left";
        } else if (region1.contains(xbottom, ybottom)) {
            return "bottom";
        } else if (region1.contains(xtop, ytop)) {
            return "top";
        } else return null;
    }
}
