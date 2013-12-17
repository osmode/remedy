package com.remedy.glass;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.glass.app.Card;
import com.twilio.client.Connection;
import com.twilio.client.Device;

public class ReceiveCallActivity extends LogoutOnStopActivity {
	protected static final String TAG = "remedy.ReceiveCall";
	Messenger mService = null;
	private Device device;
	private Connection connection;
	private boolean inCall;
	
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inCall = false;
		
		//Keep the screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Card card = new Card(this);
		card.setText("Incoming call from Dr. Siddiqui");
		card.setInfo("Tap to accept");
		card.addImage(R.drawable.drsiddiqui);
		
		setContentView(card.toView());
		
		Intent callingIntent = getIntent();
		connection = callingIntent.getParcelableExtra(Device.EXTRA_CONNECTION);
		device = callingIntent.getParcelableExtra(Device.EXTRA_DEVICE);
		
		bindService(new Intent(this, RemedyService.class), mConnection, Context.BIND_IMPORTANT | Context.BIND_AUTO_CREATE);
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	inCall = false;
    	unbindService(mConnection);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.accept_call, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupVisible(R.id.group_accept_call, !inCall);
    	menu.setGroupVisible(R.id.group_in_call, inCall);
		return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection. Menu items typically start another
        // activity, start a service, or broadcast another intent.
    	Message msg;
        switch (item.getItemId()) {
            case R.id.accept_menu_item:
            	msg = Message.obtain(null, RemedyService.MSG_RECEIVE_CALL);
            	try {
            		closeOptionsMenu();
            		if (mService == null) {
            			finish();
            		} else {
            			Bundle data = new Bundle();
                        data.putParcelable(Device.EXTRA_CONNECTION, connection);
                        data.putParcelable(Device.EXTRA_DEVICE, device);
                        msg.setData(data);
                        mService.send(msg);
                        
                        inCall = true;
                		Card card = new Card(this);
                		card.setText("Call with Dr. Siddiqui");
                		card.setInfo("Tap to end");
                		card.addImage(R.drawable.drsiddiqui);
                		
                		setContentView(card.toView());
            		}
                } catch (RemoteException e) {
                    // In this case the service has crashed before we could even do anything with it
                	Log.e(TAG, "Error connecting to service");
                }
            	return true;
            case R.id.decline_menu_item:
            	inCall = false;
            	msg = Message.obtain(null, RemedyService.MSG_REJECT_CALL);
            	try {
            		if (mService != null) {
            			Bundle data = new Bundle();
                        data.putParcelable(Device.EXTRA_CONNECTION, connection);
                        data.putParcelable(Device.EXTRA_DEVICE, device);
                        msg.setData(data);
            			mService.send(msg);
            		}
            		closeOptionsMenu();
                    finish();
            	} catch (RemoteException e) {
            		Log.e(TAG, "Couldn't hangup", e);
            		e.printStackTrace();
            		closeOptionsMenu();
            	}
                return true;
            case R.id.hangup_menu_item:
            	inCall = false;
            	msg = Message.obtain(null, RemedyService.MSG_HANGUP);
            	try {
            		if (mService != null) {
            			mService.send(msg);
            		}
            		closeOptionsMenu();
                    finish();
            	} catch (RemoteException e) {
            		Log.e(TAG, "Couldn't hangup", e);
            		e.printStackTrace();
            		closeOptionsMenu();
            	}
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
              openOptionsMenu();
              return true;
          }
          return false;
    }
}
