package com.remedy.glass;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twilio.example.hellomonkey.HttpHelper;

public class MessagePoller {
	private static final int INTERVAL = 5; //Seconds between polls
	private static final String URL = "http://remedy-glass.herokuapp.com/message/";
	public static final String TAG = "Remedy.messagePoller";
	private Timer timer;
	private Context context;
	private Set<String> messageSet;
	private boolean stopped = false;
	
	//In case we want to allow filtering by username later
	private String username;
	
	public MessagePoller(Context context, String username){
		this.context = context;
		this.username = username;
		
		this.messageSet = Collections.synchronizedSet(new HashSet<String>());
		
		this.timer = new Timer("MessagePoller", true); //Run this as a daemon
		this.timer.schedule(new URLPollTimerTask(), 0, INTERVAL);
	}
	
	private class URLPollTimerTask extends TimerTask {
		@Override
		public void run() {
			try {
            	String response = HttpHelper.httpGet(URL);
            	if (response != null && response.length() != 0) {
            		MessagePoller.this.processMessage(response);	
            	}
            } catch (Exception e) {
                Log.e(TAG, "Error fetching url", e);
            }
		}
	}
	
	private void processMessage(String message) {
		if (stopped) {return;}
		
		Log.i(TAG, "Got message from server:" + message);
		//Don't want any repeats
		if (this.messageSet.contains(message)) {
			Log.i(TAG, "Already seen mesage: " + message);
			return;
		}
		Log.i(TAG, "New message! " + message);
		this.messageSet.add(message);
		
		//Processing
		String[] parts = message.split("\\|");
		//Text, subtext, image, uuid
		if (parts.length != 4) {
			return;
		}

		//Display
		Intent display = new Intent(this.context, DisplayMessageActivity.class);
		display.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		display.putExtra(DisplayMessageActivity.EXTRA_TEXT, parts[0]);
		display.putExtra(DisplayMessageActivity.EXTRA_SUBTEXT, parts[1]);
		display.putExtra(DisplayMessageActivity.EXTRA_IMAGE, parts[2]);
		context.startActivity(display);
	}
	
	public void stop() {
		Log.i(TAG, "Stopping message poller");
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		stopped = true;
	}
	
	@Override
	protected void finalize() {
		stop();
	}
}
