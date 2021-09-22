package com.andreydymko.spoaudiocalls.SPONetworking.Sound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPOCouldNotFindServer;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOServerConnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SoundSendingThread extends Thread {

    private final static String TAG = SoundSendingThread.class.getSimpleName();

    private final DatagramSocket datagramSocket;
    private final InetAddress serverInetAddress;
    private final int UDPPort;


    public SoundSendingThread(DatagramSocket datagramSocket) throws SPOCouldNotFindServer {
        this.datagramSocket = datagramSocket;
        this.UDPPort = datagramSocket.getLocalPort();

        try {
            serverInetAddress = InetAddress.getByName(SPOServerConnector.SERVER_ADDRESS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new SPOCouldNotFindServer();
        }
    }

    @Override
    public void run() {
        int sampleRate = 44100;
        int bufferSize = 8192;
//        int bufferSize = AudioRecord.getMinBufferSize(
//                sampleRate,
//                AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT
//        );
        byte[] buffer = new byte[bufferSize];

        Log.d(TAG, "buffer size = " + bufferSize);

        DatagramPacket datagramPacket = new DatagramPacket(buffer,
                buffer.length,
                serverInetAddress,
                UDPPort
        );

        AudioRecord micRecord = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

        micRecord.startRecording();

        while (!Thread.currentThread().isInterrupted()) {
            micRecord.read(buffer, 0, bufferSize);
            datagramPacket.setData(buffer);
            try {
                datagramSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        micRecord.stop();
        micRecord.release();
    }
}
