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

public class CallExpertActivity extends LogoutOnStopActivity {
	protected static final String TAG = "remedy.CallExpert";
	Messenger mService = null;
	
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, RemedyService.MSG_CALL_EXPERT);
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            	Log.e(TAG, "Error connecting to service");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Keep the screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Card card = new Card(this);
		card.setText("Calling Dr. Siddiqui...");
		card.setInfo("Physician at UPenn");
		card.addImage(R.drawable.drsiddiqui);
		
		setContentView(card.toView());
		
		bindService(new Intent(this, RemedyService.class), mConnection, Context.BIND_IMPORTANT | Context.BIND_AUTO_CREATE);
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unbindService(mConnection);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.call_expert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection. Menu items typically start another
        // activity, start a service, or broadcast another intent.
        switch (item.getItemId()) {
            case R.id.hangup_menu_item:
            	Message msg = Message.obtain(null, RemedyService.MSG_HANGUP);
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
