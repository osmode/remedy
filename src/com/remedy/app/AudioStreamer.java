package com.remedy.app;

import android.media.MediaRecorder;

/**
 * User: mheld
 * Date: 10/3/13
 * Time: 11:16 AM
 */
public class AudioStreamer {
    private MediaRecorder mMediaRecorder;

    AudioStreamer(){
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setAudioSamplingRate(8000);
        mMediaRecorder.setAudioEncodingBitRate(32000);

        // We write the ouput of the camera in a local socket instead of a file !
        // This one little trick makes streaming feasible quiet simply: data from the camera
        // can then be manipulated at the other end of the socket
      //  mMediaRecorder.setOutputFile(mSender.getFileDescriptor());

        //mMediaRecorder.prepare();
        //mMediaRecorder.start();

    //    try {
            // mReceiver.getInputStream contains the data from the camera
            // the mPacketizer encapsulates this stream in an RTP stream and send it over the network
            //mPacketizer.setDestination(mDestination, mRtpPort, mRtcpPort);
            //mPacketizer.setInputStream(mReceiver.getInputStream());
            //mPacketizer.start();
            //mStreaming = true;
        //} catch (IOException e) {
          //  stop();
            //throw new IOException("Something happened with the local sockets :/ Start failed !");
        //}
    }
}
