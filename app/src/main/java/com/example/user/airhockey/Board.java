package com.example.user.airhockey;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

/**
 * Created by User on 3/11/2018.
 */

public class Board implements GameObject {
    Path myBoard;
    Path opponentBoard;
    Path fullBoard;

    RectF myGoal;
    static int MYGOAL=1;
    RectF hisGoal;
    static int HISGOAL=2;

    RectF decoration;

    int color;

    float xTopRight;
    float xTopLeft;

    float xInterceptLeft;
    float xInterceptRight;

    float xCenter;
    float yCenter;

    public Board(int color){
        this.color=color;
        xTopLeft=(float)(0.2*ScreenConstants.SCREEN_WIDTH);
        xTopRight=ScreenConstants.SCREEN_WIDTH-((float)(0.2*ScreenConstants.SCREEN_WIDTH));
        getCenter();

        fullBoard=drawTrapezoid(0,ScreenConstants.SCREEN_HEIGHT,ScreenConstants.SCREEN_WIDTH,ScreenConstants.SCREEN_HEIGHT,
                xTopRight,0,xTopLeft,0);

        myBoard=drawTrapezoid(0,ScreenConstants.SCREEN_HEIGHT,ScreenConstants.SCREEN_WIDTH,ScreenConstants.SCREEN_HEIGHT,
                xInterceptRight,yCenter,xInterceptLeft,yCenter);

        opponentBoard=drawTrapezoid(xInterceptLeft,yCenter,xInterceptRight,yCenter,
                xTopRight,0,xTopLeft,0);

        myGoal= new RectF((ScreenConstants.SCREEN_WIDTH/2)-(ScreenConstants.SCREEN_WIDTH/4), ScreenConstants.SCREEN_HEIGHT-20, (ScreenConstants.SCREEN_WIDTH/2)+(ScreenConstants.SCREEN_WIDTH/4), ScreenConstants.SCREEN_HEIGHT+20);
        double multiplier=ScreenConstants.getMultiplier(new Point(0,0));
        hisGoal=new RectF((ScreenConstants.SCREEN_WIDTH/2)-((int)(multiplier*ScreenConstants.SCREEN_WIDTH/4)), -20, (ScreenConstants.SCREEN_WIDTH/2)+((int)(multiplier*ScreenConstants.SCREEN_WIDTH/4)), +20);

        decoration=new RectF(xCenter-ScreenConstants.SCREEN_WIDTH/8,yCenter-ScreenConstants.SCREEN_HEIGHT/9,xCenter +ScreenConstants.SCREEN_WIDTH/6,yCenter+ScreenConstants.SCREEN_HEIGHT/10);
    }

    public Path drawTrapezoid(float x1,float y1,float x2,float y2,float x3,float y3, float x4, float y4){
        Path trapezoid = new Path();
        trapezoid.reset();

        trapezoid.moveTo(x1,y1);
        trapezoid.lineTo(x2,y2);
        trapezoid.lineTo(x3,y3);
        trapezoid.lineTo(x4,y4);
        trapezoid.lineTo(x1,y1);

        return trapezoid;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawPath(myBoard,paint);
        canvas.drawPath(opponentBoard,paint);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(15);
        canvas.drawLine(xInterceptLeft,yCenter,xInterceptRight,yCenter,paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawOval(decoration,paint);

        paint.setColor(Color.BLACK);
        canvas.drawRect(myGoal,paint);
        canvas.drawRect(hisGoal,paint);

    }

    private void getCenter(){
        double slope1=ScreenConstants.SCREEN_HEIGHT/-xTopRight;
        double slope2=ScreenConstants.SCREEN_HEIGHT/(ScreenConstants.SCREEN_WIDTH-xTopLeft);

        double B1=(double)ScreenConstants.SCREEN_HEIGHT;
        double B2=(double)ScreenConstants.SCREEN_HEIGHT-(slope2 * ScreenConstants.SCREEN_WIDTH);

        xCenter=(float)((B2-B1)/(slope1-slope2));
        yCenter=(float)((xCenter*slope1)+ScreenConstants.SCREEN_HEIGHT);

        System.out.println("xCenter: "+xCenter);
        System.out.println("XLength: "+ScreenConstants.SCREEN_WIDTH);

        getIntercepts();
    }

    private void getIntercepts(){
        double length1=xTopRight-xTopLeft;
        double length2=ScreenConstants.SCREEN_WIDTH;

        double midLength=(2*length1*length2)/(length1+length2);

        xInterceptLeft=(float)(xCenter-midLength/2);
        xInterceptRight=(float)(xCenter+midLength/2);

    }

    public boolean goalTouch(HockeyBall ball){
        return goalTouchBoard(ball)!=0;
    }

    public int goalTouchBoard(HockeyBall ball){
        Region clip = new Region(0, 0, ScreenConstants.SCREEN_WIDTH, ScreenConstants.SCREEN_HEIGHT);

        Path goal = new Path();
        goal.addRect(myGoal,Path.Direction.CCW);

        Path oppGoal = new Path();
        oppGoal.addRect(hisGoal, Path.Direction.CCW);

        Path hockeyBall = new Path();
        hockeyBall.addOval(ball.getBall(), Path.Direction.CCW);

        Region region1=new Region();
        region1.setPath(goal,clip);

        Region region2=new Region();
        region2.setPath(hockeyBall,clip);

        Region region3 = new Region();
        region3.setPath(oppGoal,clip);
        if(region1.op(region2, Region.Op.INTERSECT)){
            return MYGOAL;
        }
        else if(region3.op(region2, Region.Op.INTERSECT)){
            return HISGOAL;
        }

        return 0;

    }

    public String contains(HockeyBall ball){
        return contains(fullBoard,ball.getBall());
    }

    public String contains(HockeyMallet player){
        return contains(myBoard,player.getMallet());
    }

    private String contains(Path path,RectF shape) {
        Region clip = new Region(0, 0, ScreenConstants.SCREEN_WIDTH, ScreenConstants.SCREEN_HEIGHT);

        Path circle = new Path();
        circle.addRect(shape, Path.Direction.CCW);

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

        if (!region1.contains(xleft, yleft) && !region1.contains(xtop,ytop)) {
            return "left-top";
        } else if (!region1.contains(xleft, yleft) && !region1.contains(xbottom,ybottom)) {
            return "left-bottom";
        } else if (!region1.contains(xright, yright) && !region1.contains(xtop,ytop)) {
            return "right-top";
        } else if (!region1.contains(xright, yright) && !region1.contains(xbottom,ybottom)) {
            return "right-bottom";
        } else if(!region1.contains(xright,yright)){
            return "right";
        }else if(!region1.contains(xleft,yleft)){
            return "left";
        } else if (!region1.contains(xbottom,ybottom)){
            return "bottom";
        }else if (!region1.contains(xtop,ytop)){
            return "top";
        }
        else return null;
        //return (region1.contains(xleft,yleft)&& region1.contains(xtop,ytop) && region1.contains(xright,yright) && region1.contains(xbottom,ybottom));

    }

    @Override
    public void update() {

    }
}
