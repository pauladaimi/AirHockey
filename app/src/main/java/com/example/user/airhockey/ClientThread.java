package com.example.user.airhockey;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by User on 4/2/2018.
 */

//Class that runs the clientThread of a P2P Socket
public class ClientThread implements Runnable,WifiThread{
    InetAddress mHostAddress;
    int mPort;

    DatagramSocket socket;

    byte[] sendData = new byte[64];
    byte[] receiveData = new byte[64];

    String player1String="Client";
    String player2String;

    public ClientThread(InetAddress hostAddress, int port){
        mHostAddress=hostAddress;
        mPort=port;
    }

    //sends and receives messages
    @Override
    public void run() {
        if(mHostAddress!=null && mPort!=0){
            while(true){
                try{
                    if(socket==null){
                        socket = new DatagramSocket(mPort);
                        socket.setSoTimeout(1);
                    }
                }catch(IOException exc){
                    if(exc.getMessage() == null){
                        Log.e("Set Socket","Unknown Message");
                    }else{
                        Log.e("Set Socket",exc.getMessage());
                    }
                }

                try{

                    DatagramPacket packet = new DatagramPacket(sendData,sendData.length,mHostAddress,mPort);
                    socket.send(packet);
                    //Log.e("MyTag","Client: Packet Sent");
                }catch (IOException exc){
                    if(exc.getMessage()==null){
                        Log.e("Set Socket", "Unknown Message: Likely Timeout");
                    }else{
                        Log.e("Set Socket",exc.getMessage());
                    }
                }

                try{
                    DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);

                    socket.receive(receivePacket);

                    receivePacket.getData();

                    player2String=new String(receivePacket.getData(),0,receivePacket.getLength());
                }catch (IOException exc){
                    if(exc.getMessage()==null){
                        Log.e("Set Socket","Unknown Message");
                    }else{
                        Log.e("Set Socket", exc.getMessage());
                    }
                    continue;
                }
            }
        }
    }

    //Sets message to be sent
    @Override
    public void setMessage(String message) {
        sendData=message.getBytes();
    }

    //receives a message
    @Override
    public String receiveMessage() {
        return player2String;
    }
}
