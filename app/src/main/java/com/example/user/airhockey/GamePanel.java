package com.example.user.airhockey;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by User on 3/11/2018.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback,SensorEventListener {
    private MainThread thread;

    private Board board;
    private HockeyMallet player;
    private HockeyBall ball;

    private Point playerPoint;
    private Point ballPoint;

    private HockeyMallet opponent;
    private Point opponentPoint;

    int ballVelocityX;
    int ballVelocityY;

    private Sensor accelerometer;

    float [] history = new float[2];
    double oldX;
    double oldY;

    int myScore;
    int hisScore;

    private boolean gameOver=false;
    private long gameOverTime;
    private Rect r = new Rect();

    WifiThread socketThread;

    public GamePanel(Context context, SensorManager SM, WifiThread socketThread){
        super(context);
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(),this);

        board= new Board(Color.MAGENTA);
        player = new HockeyMallet(new RectF(100,100,400,400), Color.RED,Color.BLACK);
        opponent= new HockeyMallet(new RectF(100,100,400,400), Color.RED,Color.BLACK);
        ball = new HockeyBall(new RectF(100,100,300,300), Color.WHITE);

        myScore=0;
        hisScore=0;

        playerPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,3*ScreenConstants.SCREEN_HEIGHT/4);
        opponentPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,ScreenConstants.SCREEN_HEIGHT/4);
        ballPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,ScreenConstants.SCREEN_HEIGHT/2);

        oldX=ScreenConstants.SCREEN_WIDTH/2;
        oldY=3*ScreenConstants.SCREEN_HEIGHT/4;

        player.update(playerPoint);
        opponent.update(opponentPoint);
        ball.update(ballPoint);

        ballVelocityX=0;
        ballVelocityY=0;

        System.out.println(SM);

        accelerometer=SM.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        SM.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME);

        this.socketThread=socketThread;
    }

    public void update(){
        if(!gameOver) {
            try{
                String opponentUpdate = socketThread.receiveMessage();
                Log.d("MESSAGE",opponentUpdate);
                String[] coordinates=opponentUpdate.split(" ");
                int opponentY=ScreenConstants.SCREEN_HEIGHT-Integer.parseInt(coordinates[1]);
                int opponentX=ScreenConstants.SCREEN_WIDTH-Integer.parseInt(coordinates[0]);
                opponentPoint.set(opponentX,opponentY);
            }catch (Exception e){

            }
            socketThread.setMessage(playerPoint.x+" "+playerPoint.y);
            ball.update(ballPoint);
            opponent.update(opponentPoint);
            player.update(playerPoint);
            ballIntersectUpdate();
            velocityUpdate();
            goalIntersectUpdate();
        }
        else{
            if(System.currentTimeMillis()-gameOverTime>5000){
                reset();
                gameOver=false;
            }
        }
    }

    public void reset(){
        playerPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,3*ScreenConstants.SCREEN_HEIGHT/4);
        ballPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,ScreenConstants.SCREEN_HEIGHT/2);

        oldX=ScreenConstants.SCREEN_WIDTH/2;
        oldY=3*ScreenConstants.SCREEN_HEIGHT/4;

        player.update(playerPoint);
        ball.update(ballPoint);

        ballVelocityX=0;
        ballVelocityY=0;
    }

    public void goalIntersectUpdate(){
        if(board.goalTouch(ball)){
            if(board.goalTouchBoard(ball)==Board.MYGOAL)hisScore++;
            else myScore++;
            gameOver=true;
            gameOverTime=System.currentTimeMillis();
        }
    }

    public void ballIntersectUpdate(){
        String IntersectLocation=ball.intersect(player);
        if(IntersectLocation!=null){
            int randomAdderX= (int)((Math.random()*601)-300);
            int randomAdderY= (int)((Math.random()*601)-300);

            if(IntersectLocation.equals("left")){
                ballVelocityX=800;
                ballVelocityY=randomAdderY;
            }
            else if(IntersectLocation.equals("right")){
                ballVelocityX=-800;
                ballVelocityY=randomAdderY;
            }
            else if(IntersectLocation.equals("top")){
                ballVelocityY=800;
                ballVelocityX=randomAdderX;
            }
            else if(IntersectLocation.equals("bottom")){
                ballVelocityY=-800;
                ballVelocityX=randomAdderX;
            }
            else if(IntersectLocation.equals("right-top")){
                ballVelocityX=-800+randomAdderX;
                ballVelocityY=800+randomAdderY;
            }
            else if(IntersectLocation.equals("right-bottom")){
                ballVelocityX=-800+randomAdderX;
                ballVelocityY=-800+randomAdderY;
            }
            else if(IntersectLocation.equals("left-top")){
                ballVelocityX=800+randomAdderX;
                ballVelocityY=800+randomAdderY;
            }
            else if(IntersectLocation.equals("left-bottom")){
                ballVelocityX=800+randomAdderX;
                ballVelocityY=-800+randomAdderY;
            }
        }

        System.out.println(ball.intersect(player));
    }

    public void velocityUpdate(){
        String hitLocation=board.contains(ball);
        if(hitLocation!=null) {
            int randomAdderXPos= (int)((Math.random()*201)+100);
            int randomAdderXNeg= (int)((Math.random()*-201)-100);
            if (hitLocation.equals("left")) {
                ballVelocityX *= -1;
                if(ballVelocityX<100) {
                    ballVelocityX += randomAdderXPos;
                }
            } else if (hitLocation.equals("left-top")) {
                ballVelocityX *= -1;
                ballVelocityY *= -1;
            } else if (hitLocation.equals("left-bottom")) {
                ballVelocityX *= -1;
                ballVelocityY *= -1;
            } else if (hitLocation.equals("bottom")) {
                ballVelocityY *= -1;
            } else if (hitLocation.equals("right-bottom")) {
                ballVelocityX *= -1;
                ballVelocityY *= -1;
            } else if (hitLocation.equals("right-top")) {
                ballVelocityX *= -1;
                ballVelocityY *= -1;
            } else if (hitLocation.equals("right")) {
                ballVelocityX *= -1;
                if(ballVelocityX>-100) {
                    ballVelocityX += randomAdderXNeg;
                }
            } else if (hitLocation.equals("top")) {
                ballVelocityY *= -1;
            }
        }

        ballPoint.y=(int)(ballPoint.y + ballVelocityY/30);
        ballPoint.x=(int)(ballPoint.x + ballVelocityX/30);
        if(ballVelocityX<0)ballVelocityX+=2;
        if(ballVelocityY<0 )ballVelocityY+=2;
        if(ballVelocityX>0)ballVelocityX-=2;
        if(ballVelocityY>0 )ballVelocityY-=2;
    }


    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);

        board.draw(canvas);
        ball.draw(canvas);
        opponent.draw(canvas);
        player.draw(canvas);

        if(gameOver){
            Paint paint = new Paint();
            paint.setTextSize(100);
            paint.setColor(Color.GREEN);
            drawCenterText(canvas,paint,myScore+" -- "+hisScore);
        }

    }

    private void drawCenterText(Canvas canvas, Paint paint, String text) {
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float xChange = /*history[0] - */-1 * event.values[0];
        float yChange = /*history[1] - */1 * event.values[1];

        String hitLocation=board.contains(player);
        if(hitLocation==null) {
            updatePoint(xChange,yChange);
        }
        else {
            if (hitLocation.equals("top")) {
                if(yChange<0){
                    oldX=playerPoint.x;
                    playerPoint.set((int) (oldX + xChange * 8), (int) (oldY + yChange * 8));
                }
                else{
                    updatePoint(xChange,yChange);
                }
            }
            else if (hitLocation.equals("bottom")) {
                if(yChange>0){
                    oldX=playerPoint.x;
                    playerPoint.set((int) (oldX + xChange * 8), (int) (oldY + yChange * 8));
                }
                else{
                    updatePoint(xChange,yChange);
                }
            }
            else if (hitLocation.equals("right")) {
                if(xChange>0){
                    oldY=playerPoint.y;
                    playerPoint.set((int) (oldX + xChange * 8), (int) (oldY + yChange * 8));
                }
                else{
                    updatePoint(xChange,yChange);
                }
            }

            else if (hitLocation.equals("left")) {
                if(xChange<0){
                    oldY=playerPoint.y;
                    playerPoint.set((int) (oldX + xChange * 8), (int) (oldY + yChange * 8));
                }
                else{
                    updatePoint(xChange,yChange);
                }
            }
            else if(hitLocation.equals("left-top")){
                if(xChange<0 || yChange<0){
                    playerPoint.set((int)(oldX+xChange*8),(int)(oldY+yChange*8));
                }
                else{
                    updatePoint(xChange,yChange);
                }
            }
            else if(hitLocation.equals("left-bottom")){
                if(xChange<0 || yChange>0){
                    playerPoint.set((int)(oldX+xChange*8),(int)(oldY+yChange*8));
                }
                else{
                    updatePoint(xChange,yChange);
                }

            }
            else if(hitLocation.equals("right-top")){
                if(xChange>0 || yChange<0){
                    playerPoint.set((int)(oldX+xChange*8),(int)(oldY+yChange*8));
                }
                else{
                    updatePoint(xChange,yChange);
                }

            }
            else if(hitLocation.equals("right-bottom")){
                if(xChange>0 || yChange>0){
                    playerPoint.set((int)(oldX+xChange*8),(int)(oldY+yChange*8));
                }
                else{
                    updatePoint(xChange,yChange);
                }
            }

        }

    }

    private void updatePoint(float xChange, float yChange){
        oldX = playerPoint.x;
        oldY = playerPoint.y;
        playerPoint.set((int)(oldX+xChange*8),(int)(oldY+yChange*8));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread = new MainThread(getHolder(),this);
        thread.setRunning(true);

        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        while(true){
            try{
                thread.setRunning(false);
                thread.join();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


}
