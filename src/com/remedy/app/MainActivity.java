package com.remedy.app;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.remedy.glass.R;

public final class MainActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "RMDY";

    private static final String WAKE_LOCK_TAG = "com.remedy.app";

    private static final int PREF_PORT_DEF = 8080;
    private static final int PREF_JPEG_QUALITY_DEF = 40;
    // preview sizes will always have at least one element, so this is safe

    private boolean mRunning = false;
    private boolean mPreviewDisplayCreated = false;
    private SurfaceHolder mPreviewDisplay = null;
    private CameraStreamer mCameraStreamer = null;

    private String mIpAddress = "";
    private int mPort = PREF_PORT_DEF;
    private int mJpegQuality = PREF_JPEG_QUALITY_DEF;
    private TextView mIpAddressView = null;
    private WakeLock mWakeLock = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mPreviewDisplay = ((SurfaceView) findViewById(R.id.camera)).getHolder();
        mPreviewDisplay.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mPreviewDisplay.addCallback(this);

        mIpAddress = tryGetIpV4Address();
        mIpAddressView = (TextView) findViewById(R.id.ip_address);
        updatePrefCacheAndUi();

        final PowerManager powerManager =
                (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, WAKE_LOCK_TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRunning = true;
        updatePrefCacheAndUi();
        tryStartStreaming();
        mWakeLock.acquire();
    }

    @Override
    protected void onPause() {
        mWakeLock.release();
        super.onPause();
        mRunning = false;
        ensureCameraStreamerStopped();
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format,
                               final int width, final int height) {
        // Ingored
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        mPreviewDisplayCreated = true;
        tryStartStreaming();
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
        mPreviewDisplayCreated = false;
        ensureCameraStreamerStopped();
    }

    private void tryStartStreaming(){
        tryStartCameraStreamer();
        tryStartAudioStreamer();
    }

    private void tryStartCameraStreamer() {
        if (mRunning && mPreviewDisplayCreated) {
            mCameraStreamer = new CameraStreamer(mPort, mJpegQuality, mPreviewDisplay);
            mCameraStreamer.start();
        }
    }


    private void tryStartAudioStreamer(){
        if(mRunning){
            //mAudioStreamer = new AudioStreamer();
            //mAudioStreamer.start();
        }
    }

    private void ensureCameraStreamerStopped() {
        if (mCameraStreamer != null) {
            mCameraStreamer.stop();
            mCameraStreamer = null;
        }
    }

    private final void updatePrefCacheAndUi() {
        mPort = PREF_PORT_DEF;
        // The port must be in the range [1024 65535]
        if (mPort < 1024) {
            mPort = 1024;
        } else if (mPort > 65535) {
            mPort = 65535;
        }

        mJpegQuality = PREF_JPEG_QUALITY_DEF;
        // The JPEG quality must be in the range [0 100]
        if (mJpegQuality < 0) {
            mJpegQuality = 0;
        } else if (mJpegQuality > 100) {
            mJpegQuality = 100;
        }
        mIpAddressView.setText("http://" + mIpAddress + ":" + mPort + "/");
    }

    private static String tryGetIpV4Address() {
        try {
            final Enumeration<NetworkInterface> en =
                    NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                final NetworkInterface intf = en.nextElement();
                final Enumeration<InetAddress> enumIpAddr =
                        intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    final  InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        final String addr = inetAddress.getHostAddress().toUpperCase();
                        if (InetAddressUtils.isIPv4Address(addr)) {
                            return addr;
                        }
                    }
                }
            }
        } catch (final Exception e) {
            // Ignore
        }

        return null;
    }
}