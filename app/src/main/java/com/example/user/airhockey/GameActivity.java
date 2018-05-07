package com.example.user.airhockey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

//Sets the Activity that will start the game and connections
public class GameActivity extends Activity {
    ClientThread clientThread;
    ServerThread serverThread;

    InetAddress hostAddress;
    String stringHostAddress;

    Timer mTimer;
    TimerTask mTask;

    Intent intent;

    Boolean host;
    int port=8888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ScreenConstants.SCREEN_WIDTH=dm.widthPixels;
        ScreenConstants.SCREEN_HEIGHT=dm.heightPixels;

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        intent = getIntent();

        if(intent.getBooleanExtra("Connected",false)) {
            stringHostAddress = intent.getStringExtra("HostAddress");

            try {
                hostAddress = InetAddress.getByName(stringHostAddress);
            } catch (UnknownHostException Exc) {

            }

            host = intent.getBooleanExtra("IsHost", false);

            //if host creates a new server thread
            if (host) {
                serverThread = new ServerThread(port);
                new Thread(serverThread).start();
                setContentView(new GamePanel(this,manager,serverThread,host));
                //if client, connects to the server.
            } else {
                clientThread = new ClientThread(hostAddress, port);
                new Thread(clientThread).start();
                setContentView(new GamePanel(this,manager,clientThread,host));
            }
        }


    }

}
