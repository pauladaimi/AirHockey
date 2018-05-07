package com.example.user.airhockey;

/**
 * Created by User on 4/6/2018.
 */

//Interface that will be implemented by Client Thread and Server Thread since they both have similar functionalities
public interface WifiThread {
    public void setMessage(String text);

    public String receiveMessage();

}
