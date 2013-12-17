package com.remedy.glass;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.twilio.client.Connection;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;
import com.twilio.client.Twilio;
import com.twilio.example.hellomonkey.HttpHelper;
 
public class RemedyPhone implements Twilio.InitListener, DeviceListener
{
    private static final String TAG = "Remedy.phone";
    private final static String AUTH_URL = "http://remedy-glass.herokuapp.com/auth/";
    
	private Device device;
	private Connection connection;
	private Context context;
	private String username;

	public RemedyPhone(Context context, String username)
    {
		this.context = context;
		this.username = username;
        Twilio.initialize(context, this /* Twilio.InitListener */);
    }
 
    @Override /* Twilio.InitListener method */
    public void onInitialized()
    {
        Log.d(TAG, "Twilio SDK is ready");
        
        try {
        	new RetreiveTokenTask().execute(AUTH_URL + "?username=" + username);
        } catch (Exception e) {
        	Log.e(TAG, "Failed to obtain capability token: " + e.getLocalizedMessage());
        }
    }
    
    class RetreiveTokenTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
            	return HttpHelper.httpGet(urls[0]);
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String capabilityToken) {
        	if (this.exception != null) {
        		Log.e(TAG, "Error fetching token", this.exception);
        	} else {
        		if (device != null) {
        			device.updateCapabilityToken(capabilityToken);
        		} else {
        			device = Twilio.createDevice(capabilityToken, RemedyPhone.this);
        		}
        		Intent intent = new Intent(context, ReceiveCallActivity.class);
        		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        		device.setIncomingIntent(pendingIntent);
        	}
        }
    }
    
    public void setContext(Context context) {
    	this.context = context;
    	if (device != null) {
    		Intent intent = new Intent(context, ReceiveCallActivity.class);
    		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    		device.setIncomingIntent(pendingIntent);
    	}
    }
 
    @Override /* Twilio.InitListener method */
    public void onError(Exception e)
    {
        Log.e(TAG, "Twilio SDK couldn't start: " + e.getLocalizedMessage());
    }
 
    @Override
    protected void finalize()
    {
        release();
    }
    
    public void release() {
    	if (connection != null)
            connection.disconnect();
        if (device != null)
            device.release();
        connection = null;
        device = null;
        Twilio.shutdown();
    }
    
    public void connect()
    {
        connection = device.connect(null /* parameters */, null /* ConnectionListener */);
        if (connection == null)
            Log.w(TAG, "Failed to create new connection");
    }
    
    public void disconnect()
    {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }
    
    public void handleIncomingConnection(Device inDevice, Connection inConnection)
    {
        Log.i(TAG, "Device received incoming connection");
        if (connection != null)
            connection.disconnect();
        connection = inConnection;
        connection.accept();
    }
    
    public void rejectIncomingConnection(Device inDevice, Connection inConnection)
    {
        Log.i(TAG, "Device received incoming connection");
        if (connection != null)
            connection.disconnect();
        connection = inConnection;
        connection.reject();
    }

	@Override
	public void onStartListening(Device inDevice) {
		Log.i(TAG, "Device is now listening for incoming connections");
	}

	@Override
	public void onStopListening(Device inDevice) {
		Log.i(TAG, "Device is no longer listening for incoming connections");
	}

	@Override
	public void onStopListening(Device inDevice, int inErrorCode, String inErrorMessage) {
        Log.i(TAG, "Device is no longer listening for incoming connections due to error " +
                inErrorCode + ": " + inErrorMessage);
	}

	@Override
	public boolean receivePresenceEvents(Device arg0) {
		return false;
	}

	@Override
	public void onPresenceChanged(Device arg0, PresenceEvent arg1) {}
}
    