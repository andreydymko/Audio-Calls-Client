package com.andreydymko.spoaudiocalls.SPONetworking.Sound;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPOCouldNotFindServer;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOServerConnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SoundReceivingThread extends Thread {
    private final static String TAG = SoundReceivingThread.class.getSimpleName();

    private final DatagramSocket datagramSocket;
    private final InetAddress serverInetAddress;
    private final int UDPPort;

    public SoundReceivingThread(DatagramSocket datagramSocket) throws SPOCouldNotFindServer {
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
//        int bufferSize = AudioTrack.getMinBufferSize(
//                sampleRate,
//                AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT
//        );
        byte[] buffer = new byte[bufferSize];

        Log.d(TAG, "buffer size = " + bufferSize);

        DatagramPacket datagramPacket = new DatagramPacket(
                buffer,
                buffer.length,
                serverInetAddress,
                UDPPort
        );

        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_VOICE_CALL,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
        );

        audioTrack.play();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            audioTrack.write(datagramPacket.getData(), 0, bufferSize);
        }

        audioTrack.pause();
        audioTrack.flush();
        audioTrack.stop();
        audioTrack.release();
    }
}
