package com.example.user.airhockey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 4/2/2018.
 */

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    private List<WifiP2pDevice> mPeers;
    private List<WifiP2pConfig> mConfigs;

    private WifiP2pDevice mDevice;



    public WifiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mainActivity) {
        super();
        this.mManager=mManager;
        this.mChannel=mChannel;
        this.mActivity=mainActivity;
    }

    public void connect(int position){
        WifiP2pConfig config = mConfigs.get(position);
        mDevice=mPeers.get(position);

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(mActivity.TAG,"Connect Succeeded");
            }

            @Override
            public void onFailure(int i) {
                Log.d(mActivity.TAG,"Connect Failed: Error: "+i);
            }
        });

    }

    WifiP2pManager.ConnectionInfoListener infoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if(info.groupFormed){
                if(info.isGroupOwner){
                    Log.d(mActivity.TAG,"HOST");
                    mActivity.play(groupOwnerAddress,true);
                }else{
                    Log.d(mActivity.TAG,"CLIENT");
                    mActivity.play(groupOwnerAddress,false);
                }
            }
        }
    };

    public void disconnect(){
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(mActivity.TAG,"Removed Group");
            }

            @Override
            public void onFailure(int i) {
                Log.d(mActivity.TAG,"Remove Group Failed Error "+i);

            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Log.d(mActivity.TAG,"Wifi-Direct: Enabled");
            }else{
                Log.d(mActivity.TAG,"wifi-Direct:Disabled");
            }
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            mPeers = new ArrayList<WifiP2pDevice>();
            mConfigs= new ArrayList<WifiP2pConfig>();

            if(mManager!=null){
                WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peerList) {
                        mPeers.clear();
                        mPeers.addAll(peerList.getDeviceList());

                        mActivity.displayPeers(peerList);

                        mPeers.addAll(peerList.getDeviceList());

                        for(int i=0; i<peerList.getDeviceList().size();i++){
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress=mPeers.get(i).deviceAddress;
                            mConfigs.add(config);
                        }
                    }
                };

                mManager.requestPeers(mChannel,peerListListener);
            }
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //Respond to new connection or disconnection
            if(mManager==null){

            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()){
                mManager.requestConnectionInfo(mChannel,infoListener);
            }

        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //Respond to this device's wifi state changing
        }
    }
}
