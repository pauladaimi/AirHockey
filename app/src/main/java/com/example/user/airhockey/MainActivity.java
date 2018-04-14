package com.example.user.airhockey;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    public ListView mListView;
    private Button searchBtn;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private ArrayAdapter<String> wifiP2pArrayAdapter;

    int position;

    Intent gameStart;

    Context thisContext;

    static final String TAG="AirHockey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisContext=this;

        Point playerPoint = new Point(ScreenConstants.SCREEN_WIDTH/2,3*ScreenConstants.SCREEN_HEIGHT/4);
        String point = playerPoint.x +" " + playerPoint.y;

        String[] coordinates=point.split(" ");

        Log.e("PLAYERPOINT",coordinates[0]+" "+coordinates[1]);

        gameStart = new Intent(this,GameActivity.class);

        mIntentFilter = new IntentFilter();

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mListView=(ListView)findViewById(R.id.listView);
        searchBtn=(Button)findViewById(R.id.search_button);

        wifiP2pArrayAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mListView.setAdapter(wifiP2pArrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG,""+i);
                position=i;
                mReceiver.connect(i);
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(view);
            }
        });

        mManager=(WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel= mManager.initialize(this,getMainLooper(),null);
        mReceiver= new WifiDirectBroadcastReceiver(mManager,mChannel,this);

    }

    public void play(InetAddress hostAddress, Boolean host){
        gameStart.putExtra("HostAddress", hostAddress.getHostAddress());
        gameStart.putExtra("IsHost",host);
        gameStart.putExtra("Connected",true);

        startActivity(gameStart);
    }

    public void search(View view){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Searching");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG,"Error: Code:"+i);
            }
        });
    }

    public void displayPeers(WifiP2pDeviceList peerList){
        wifiP2pArrayAdapter.clear();

        for(WifiP2pDevice peer: peerList.getDeviceList()){
            wifiP2pArrayAdapter.add(peer.deviceName+"\n"+peer.deviceAddress);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}
