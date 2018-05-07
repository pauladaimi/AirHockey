package com.example.user.airhockey;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by User on 4/2/2018.
 */

//Class that runs the serverThread of a P2P Socket
public class ServerThread implements Runnable,WifiThread {
    DatagramSocket socket;

    int mPort;

    String player1String="HOST";
    String Player2String;

    byte[] sendData= new byte[64];
    byte[] receiveData = new byte[64];

    InetAddress mClientAddress;

    boolean gotPacket=false;

    public ServerThread(int initPort){
        mPort=initPort;
    }

    //Sets message to be sent
    @Override
    public void setMessage(String message){
        sendData=message.getBytes();
    }

    //recieves a message
    @Override
    public String receiveMessage() {
        return Player2String;
    }

    //sends and receieves messages
    @Override
    public void run() {
        while(true){

            try{
                if(socket==null){
                    socket=new DatagramSocket(mPort);
                    socket.setSoTimeout(1);
                }
            }catch(IOException exc){
                if(exc.getMessage() == null){
                    Log.e("Set Socket", "Unknown Message");
                }else{
                    Log.e("Set Socket", exc.getMessage());
                }
            }

            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
            Log.e("MyTag","Waiting for Packet");

            try{
                socket.receive(receivePacket);
                receivePacket.getData();

                Player2String = new String(receivePacket.getData(),0,receivePacket.getLength());
                Log.e("MyTag","Received Packet, contained: "+Player2String);

                if(mClientAddress==null){
                    mClientAddress=receivePacket.getAddress();
                }
            }catch (IOException exc){
                if(exc.getMessage()==null){
                    Log.e("Receive","Null Exception: Likely Timeout");
                }else{
                    Log.e("Receive",exc.getMessage());
                    continue;
                }
            }

            try{
                if(mClientAddress!=null){

                    DatagramPacket packet = new DatagramPacket(sendData,sendData.length,mClientAddress,mPort);
                    socket.send(packet);
                    //Log.e("MyTag","Server: Packet Sent: "+player1String);
                }
            }catch (IOException exc){
                if(exc.getMessage()==null){
                    Log.e("Sender","Null Exception: Likely Timeout");
                }else{
                    Log.e("Sender", exc.getMessage());
                }
            }
        }
    }
}
