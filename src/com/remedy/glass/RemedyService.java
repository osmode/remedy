/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.remedy.glass;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;

import com.twilio.client.Connection;
import com.twilio.client.Device;


 /* The main application service that manages the lifetime of activities */
public class RemedyService extends Service {
    /**
     * A binder that gives other components access to the speech capabilities provided by the
     * service.
     
    public class CompassBinder extends Binder {
        //Read the current heading aloud using the text-to-speech engine.
        public void readHeadingAloud() {
            String headingText = "Hello world";
            mSpeech.speak(headingText, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
	

    private final CompassBinder mBinder = new CompassBinder();

    */

    private TextToSpeech mSpeech;

    private RemedyPhone mPhone;
    private MessagePoller mPoller;
    
    static final int MSG_CALL_EXPERT = 1;
    static final int MSG_HANGUP = 2;
    static final int MSG_RECEIVE_CALL = 3;
    static final int MSG_LOGIN = 4;
    static final int MSG_REJECT_CALL = 5;
    static final int MSG_LOGOUT = 6;
    
    static final String EXTRA_USERNAME = "username";

	private static final String TAG = "Remedy Service";
	
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
    	
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_CALL_EXPERT:
                callExpert();
                break;
            case MSG_HANGUP:
                hangup();
                break;
            case MSG_RECEIVE_CALL:
            	Device device = msg.getData().getParcelable(Device.EXTRA_DEVICE);
            	Connection connection = msg.getData().getParcelable(Device.EXTRA_CONNECTION);
                acceptConnection(device, connection);
                break;
            case MSG_REJECT_CALL:
            	device = msg.getData().getParcelable(Device.EXTRA_DEVICE);
            	connection = msg.getData().getParcelable(Device.EXTRA_CONNECTION);
                rejectConnection(device, connection);
                break;
            case MSG_LOGOUT:
            	logout();
            	break;
            default:
                super.handleMessage(msg);
            }
        }
    }  // end IncomingHandler 

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    // called when a service is created
    @Override
    public void onCreate() {
        super.onCreate();

        //mTimelineManager = TimelineManager.from(this);
        // Even though the text-to-speech engine is only used in response to a menu action, we
        // initialize it when the application starts so that we avoid delays that could occur
        // if we waited until it was needed to start it up.
        mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            }
        });
        
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                mLogoutMessageReceiver, new IntentFilter("logout"));
    }
    
    private BroadcastReceiver mLogoutMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	logout();
        }
    };

    // called after a service is started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	String username = intent.getStringExtra(EXTRA_USERNAME);
    	
    	// if user is logged in, construct new RemedyPhone and MessagePoller objects using the username
    	if (username != null) {
    		Context ctx = getApplicationContext();
            mPhone = new RemedyPhone(ctx, username);
            mPoller = new MessagePoller(ctx, username);
    	} else {  // if user is not logged in, start LoginActivity
    		Intent loginIntent = new Intent(this, LoginActivity.class);
    		loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    		getApplication().startActivity(loginIntent);
    	}
    	
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mSpeech.shutdown();
        
        mSpeech = null;
        logout();

        super.onDestroy();
    }
    
    public void logout() {
    	if (mPhone != null) {
    		mPhone.release();
    		mPhone = null;
    	}
    	if (mPoller != null) {
    		mPoller.stop();
    		mPoller = null;
    	}
    }
    
    public void callExpert() {
    	mPhone.connect();
    }
    
    public void hangup() {
    	mPhone.disconnect();
    }
    
    public void acceptConnection(Device device, Connection connection) {
    	mPhone.handleIncomingConnection(device, connection);
    }
    
    public void rejectConnection(Device device, Connection connection) {
    	mPhone.rejectIncomingConnection(device, connection);
    }
}
