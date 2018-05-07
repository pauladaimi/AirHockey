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

//Class that holds all game functionalities
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback,SensorEventListener {
    private MainThread thread;
    boolean host;

    int yCenter;

    private Board board;
    private HockeyMallet player;
    private HockeyBall ball;

    private Point playerPoint;
    private Point ballPoint;

    private HockeyMallet opponent;
    private Point opponentPoint;

    int myDebounce=0;
    int hisDebounce=0;
    int ballVelocityX;
    int ballVelocityY;

    int myMalletVelocityX=0;
    int myMalletVelocityY=0;

    int hisMalletVelocityX=0;
    int hisMalletVelocityY=0;

    private Sensor accelerometer;

    float [] history = new float[2];
    double oldX;
    double oldY;

    double myHandleOldX;
    double myHandleOldY;

    int myScore;
    int hisScore;

    private boolean gameOver=false;
    private long gameOverTime;
    private Rect r = new Rect();

    WifiThread socketThread;

    //Creates a new Panel with a board, player, opponent, and sets the accelerometer
    public GamePanel(Context context, SensorManager SM, WifiThread socketThread, boolean host){
        super(context);
        getHolder().addCallback(this);
        this.host=host;
        thread = new MainThread(getHolder(),this);

        board= new Board(Color.MAGENTA);
        player = new HockeyMallet(new RectF(100,100,(ScreenConstants.SCREEN_WIDTH+ScreenConstants.SCREEN_WIDTH)/9,(ScreenConstants.SCREEN_WIDTH+ScreenConstants.SCREEN_WIDTH)/9), Color.RED,Color.BLACK);
        opponent= new HockeyMallet(new RectF(100,100,(ScreenConstants.SCREEN_WIDTH+ScreenConstants.SCREEN_WIDTH)/9,(ScreenConstants.SCREEN_WIDTH+ScreenConstants.SCREEN_WIDTH)/9), Color.RED,Color.BLACK);
        ball = new HockeyBall(new RectF(100,100,(ScreenConstants.SCREEN_WIDTH+ScreenConstants.SCREEN_WIDTH)/10,(ScreenConstants.SCREEN_WIDTH+ScreenConstants.SCREEN_WIDTH)/10), Color.WHITE);

        myScore=0;
        hisScore=0;

        yCenter=(int)board.getYCenter();

        playerPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,3*ScreenConstants.SCREEN_HEIGHT/4);
        opponentPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,ScreenConstants.SCREEN_HEIGHT/4);
        ballPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,yCenter);


        oldX=ScreenConstants.SCREEN_WIDTH/2;
        oldY=3*ScreenConstants.SCREEN_HEIGHT/4;

        player.update(playerPoint);
        opponent.update(opponentPoint);
        ball.update(ballPoint);

        ballVelocityX=0;
        ballVelocityY=0;

        myMalletVelocityX=0;
        myMalletVelocityY=0;

        System.out.println(SM);

        accelerometer=SM.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        SM.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME);

        this.socketThread=socketThread;
    }


    public void update(){
        if(!gameOver) {

            //Gets the message from the socket, to update opponent position, and ball position, if not host
            try{
                String opponentUpdate = socketThread.receiveMessage();
                Log.d("MESSAGE",opponentUpdate);
                String[] coordinates=opponentUpdate.split(" ");
                int oppXoppBoard=(int)(Double.parseDouble(coordinates[0])*ScreenConstants.SCREEN_WIDTH);
                int oppYoppBoard=(int)(Double.parseDouble(coordinates[1])*ScreenConstants.SCREEN_HEIGHT);
                int opponentInitY=ScreenConstants.SCREEN_HEIGHT - oppYoppBoard;

                int initRange=ScreenConstants.SCREEN_HEIGHT-(int)board.getYCenter();
                int ActRange=yCenter;
                int opponentY=(int)(((double)opponentInitY/initRange)*ActRange);

                double scale1=((double)yCenter-opponentY)/(double)yCenter;
                double scale2=0.4*scale1;
                double yTEST3=1-scale2;

                int oppXmyBoard=ScreenConstants.SCREEN_WIDTH-oppXoppBoard;
                int opponentX=(int)((scale2/2)*ScreenConstants.SCREEN_WIDTH+yTEST3*oppXmyBoard);



                opponentPoint.set(opponentX,opponentY);
                hisMalletVelocityX=Integer.parseInt(coordinates[4]);
                hisMalletVelocityY=Integer.parseInt(coordinates[5]);

                //if not host, update ball position to host's ball position
                if(!host){
                    int actballposX;
                    int actballposY;

                    int oppBallPosX=(int)(Double.parseDouble(coordinates[2])*ScreenConstants.SCREEN_WIDTH);
                    int oppBallPosY=(int)(Double.parseDouble(coordinates[3])*ScreenConstants.SCREEN_HEIGHT);

                    if (board.ballMyBoard(oppBallPosX,oppBallPosY)){
                        oppBallPosY=ScreenConstants.SCREEN_HEIGHT - oppBallPosY;
                        actballposY=(int)(((double)oppBallPosY/initRange)*ActRange);
                    }
                    else{

                        actballposY=(int)(((double)oppBallPosY/ActRange)*initRange);
                        actballposY=ScreenConstants.SCREEN_HEIGHT-actballposY;
                    }
                    double scale11=((double)yCenter-actballposY)/(double)yCenter;
                    double scale22=0.4*scale11;
                    double scale33=1-scale22;

                    int ballXmyBoard=ScreenConstants.SCREEN_WIDTH-oppBallPosX;
                    actballposX=(int)((scale22/2)*ScreenConstants.SCREEN_WIDTH+scale33*ballXmyBoard);

                    ballPoint.set(actballposX,actballposY);
                }

            }catch (Exception e){

            }
            //sets the message to be sent having, mallet and ball coordinates, and mallet speed.
            socketThread.setMessage(((double)playerPoint.x/ScreenConstants.SCREEN_WIDTH)+" "+((double)playerPoint.y/ScreenConstants.SCREEN_HEIGHT)+" "+ ((double)ballPoint.x/ScreenConstants.SCREEN_WIDTH) +" "+ ((double)ballPoint.y/ScreenConstants.SCREEN_HEIGHT)+" "+myMalletVelocityX+" "+myMalletVelocityY);
            ball.update(ballPoint);
            opponent.update(opponentPoint);

            player.update(playerPoint);
            getMyHandleSpeed();
            myHandleOldX=player.getMallet().centerX();
            myHandleOldY=player.getMallet().centerY();

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

    //computes the speed of the handle
    public void getMyHandleSpeed(){
        myMalletVelocityX = (int)((player.getMallet().centerX()-myHandleOldX)*30);
        myMalletVelocityY = (int)((player.getMallet().centerY()-myHandleOldY)*30);
    }

    //resets whenever there's a goal
    public void reset(){
        playerPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,3*ScreenConstants.SCREEN_HEIGHT/4);
        ballPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,yCenter);

        oldX=ScreenConstants.SCREEN_WIDTH/2;
        oldY=3*ScreenConstants.SCREEN_HEIGHT/4;

        player.update(playerPoint);
        ball.update(ballPoint);

        ballVelocityX=0;
        ballVelocityY=0;
    }

    //checks for intercepts with the goal
    public void goalIntersectUpdate(){
        if(board.goalTouch(ball)){
            if(board.goalTouchBoard(ball)==Board.MYGOAL)hisScore++;
            else myScore++;
            gameOver=true;
            gameOverTime=System.currentTimeMillis();
        }
    }

    //checks id the ball intersected any of the mallets
    public void ballIntersectUpdate(){
        if(ball.intersects(player)){
            if(myDebounce==0) {
                myDebounce++;
                ballVelocityX = -ballVelocityX + myMalletVelocityX;
                ballVelocityY = -ballVelocityY + myMalletVelocityY;
            }
        }

        if(myDebounce>0) {
            myDebounce++;
        }
        if(myDebounce==10){
            myDebounce=0;
        }

        if(ball.intersects(opponent)){
            if(hisDebounce==0) {
                hisDebounce++;
                ballVelocityX=-ballVelocityX+hisMalletVelocityX;
                ballVelocityY=-ballVelocityY+hisMalletVelocityY;
            }
        }
        if(hisDebounce>0){
            hisDebounce++;
        }
        if(hisDebounce==10){
            hisDebounce=0;
        }
    }

    //updates ball velocity if it hit with any of the walls
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
        if (host) {
            ballPoint.y = (int) (ballPoint.y + ballVelocityY / 30);
            ballPoint.x = (int) (ballPoint.x + ballVelocityX / 30);
            if (ballVelocityX < 0) ballVelocityX += 2;
            if (ballVelocityY < 0) ballVelocityY += 2;
            if (ballVelocityX > 0) ballVelocityX -= 2;
            if (ballVelocityY > 0) ballVelocityY -= 2;
        }
    }


    //draws all components on the canvas
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

    //Method that draws text on the center of the screen
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

    //Method that updates the position of the mallet using the accelerometer
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

    //updates the player point to the new point
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
