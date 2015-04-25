package com.zhongli.john.iblackboard;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Zhongli on 2015/2/19.
 */
public class Playback extends Thread {

    private String IPaddr;
    private int port;
    private int playBufSize;
    static final int frequency = 8000;
    static final int channelConfiguration = AudioFormat.CHANNEL_OUT_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private Socket socket;
    private AudioTrack audioTrack;
    private boolean isPlaying;
    private BufferedInputStream playbackInputStream;

    public Playback(String IPaddr, int port) {
        this.IPaddr = IPaddr;
        this.port = port;
    }

    public void run() {
        playBufSize = 3*AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
//        audioTrack.setStereoVolume(1f, 1f);
        try {
            socket = new Socket(IPaddr, port);
            playbackInputStream = new BufferedInputStream(socket.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        audioTrack.play();
        isPlaying = true;
        byte[] data = new byte[playBufSize];
        int numBytesRead = 0;
        while (isPlaying) {
            try {
                numBytesRead = playbackInputStream.read(data);
                audioTrack.write(data, 0, numBytesRead);
//                System.out.println("write Voice:"+numBytesRead+";total:"+playBufSize);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        audioTrack.stop();
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
