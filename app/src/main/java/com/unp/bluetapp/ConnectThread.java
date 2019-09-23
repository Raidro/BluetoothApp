package com.unp.bluetapp;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ConnectThread extends Thread {


    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private Handler handler;
    private static final int MSG_READ = 0;
    private static final int MSG_ERRO = 1;

    public ConnectThread(BluetoothSocket socket, Handler handler) {

        this.handler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            //Log.e(TAG, "Error occurred when creating input stream", e);
            e.printStackTrace();
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            //Log.e(TAG, "Error occurred when creating output stream", e);
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {

        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()


        while (true) {
            try {
                Message message = new Message();
                numBytes = mmInStream.read(mmBuffer);

                String dadoRecebidos = new String(mmBuffer, 0, numBytes);
                message.what = MSG_READ;
                message.obj = dadoRecebidos;
                handler.sendMessage(message);

            } catch (IOException e) {

                e.printStackTrace();
                break;
            }
        }

    }

    // Call this from the main activity to send data to the remote device.
    public void enviaDados(String msg) {

        byte [] msgBuffer = msg.getBytes();
        try {
            mmOutStream.write(msgBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




