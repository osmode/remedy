/*
 *  Copyright (c) 2011 by Twilio, Inc., all rights reserved.
 *
 *  Use of this software is subject to the terms and conditions of 
 *  the Twilio Terms of Service located at http://www.twilio.com/legal/tos
 */

package com.twilio.example.hellomonkey;

import android.content.Context;
import android.util.Log;

import com.twilio.client.Device;
import com.twilio.client.Twilio;

public class MonkeyPhone implements Twilio.InitListener
{
    private static final String TAG = "MonkeyPhone";

    private Device device;

    public MonkeyPhone(Context context)
    {
        Twilio.initialize(context, this /* Twilio.InitListener */);
    }

    /* Twilio.InitListener method */
    @Override
    public void onInitialized()
    {
        Log.d(TAG, "Twilio SDK is ready");

        try {
            String capabilityToken = HttpHelper.httpGet("http://companyfoo.com/auth.php");
            device = Twilio.createDevice(capabilityToken, null /* DeviceListener */);
        } catch (Exception e) {
            Log.e(TAG, "Failed to obtain capability token: " + e.getLocalizedMessage());
        }
    }

    /* Twilio.InitListener method */
    @Override
    public void onError(Exception e)
    {
        Log.e(TAG, "Twilio SDK couldn't start: " + e.getLocalizedMessage());
    }

    @Override
    protected void finalize()
    {
        if (device != null)
            device.release();
    }
}
