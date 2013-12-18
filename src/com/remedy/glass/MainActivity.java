package com.remedy.glass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Camera;

/*** 
 MainActivity is started in LoginActivity after  QR code is successfully read 
 It extends LogoutOnStopActivity, which broadcasts an intent as it's 
 entering the background
***/

public class MainActivity extends LogoutOnStopActivity {
	public static final String LOGIN_INTENT_EXTRA_CREDENTIALS = "login_credentials";
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	protected static final String TAG = "Remedy.main";
	private User currentUser;
	private static final int CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CAPTURE_VIDEO_REQUEST_CODE = 200;

	private Uri mFileUri;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Keep the screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		String loginCredentials = getIntent().getStringExtra(LOGIN_INTENT_EXTRA_CREDENTIALS);
		currentUser = UserDB.getUser(loginCredentials);

		/***
		Once the user is logged in, start RemedyService (in the background) with the username as an extra
		and show the user's card (along with their avatar)
		***/
		
		if (currentUser == null) {  
			System.out.println("No user, forcing login.");
			
			//Starting service without username will create login activity
            Intent service = new Intent(this, RemedyService.class);
    		startService(service);
    		
    		finish();
    		
		} else {  
			System.out.println("Logged in");
			
			//Logging in/making sure service is running
			Intent service = new Intent(this, RemedyService.class);
    		service.putExtra(RemedyService.EXTRA_USERNAME, currentUser.username);
    		startService(service);
    		
			Card card = createUserCard(currentUser);
			setContentView(card.toView());
		}
	}
	
	private Card createUserCard(User user) {
		Card card = new Card(this);
		card.setText(user.fullName);
		card.setInfo(user.title);
		card.addImage(user.image);
		
		return card;
	}
    
	@Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.i(TAG, "Asked to be destroyed");
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.remedy, menu);
        return true;
    }

	/*** 
	 Presently only the call functionality is enabled
	 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection. Menu items typically start another
        // activity, start a service, or broadcast another intent.
        switch (item.getItemId()) {
            case R.id.call_menu_item:
            	closeOptionsMenu();
                startActivity(new Intent(this, CallExpertActivity.class));
                return true;
            case R.id.take_photo_menu_item:
            	Log.d(TAG, "Take photo menu item selected");
            	startPhoto();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    // when Glass is tapped, inflate the options menu (defined in remedy.xml)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
              openOptionsMenu();
              return true;
          }
          return false;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (requestCode == CAPTURE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK ) {
    		
    		String thumbnailFilepath = data.getStringExtra(Camera.EXTRA_THUMBNAIL_FILE_PATH);
    		String fullFilepath = data.getStringExtra(Camera.EXTRA_PICTURE_FILE_PATH);
    		
    		Log.d(TAG, "Intent extra: " + data.getExtras());
    		Log.d(TAG, "Thumbnail file path: " + fullFilepath);
    	}
    	/*
    	if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
    		if (resultCode == RESULT_OK) {
    			Toast.makeText(this, "Image saved to: \n" + data.getData(), Toast.LENGTH_LONG).show();
    			
    		} else if (resultCode == RESULT_CANCELED) {
    			// user cancelled the image capture
    		} else {
    			// image capture failed, advise user
    		}
    	}
    	
    	   if (requestCode == CAPTURE_VIDEO_REQUEST_CODE) {
    	        if (resultCode == RESULT_OK) {
    	            // Video captured and saved to fileUri specified in the Intent
    	            Toast.makeText(this, "Video saved to:\n" +
    	                     data.getData(), Toast.LENGTH_LONG).show();
    	        } else if (resultCode == RESULT_CANCELED) {
    	            // User cancelled the video capture
    	        } else {
    	            // Video capture failed, advise user
    	        }
    	    }
    	    */
    }
    
   	private void startPhoto() {
	   
   		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
   		startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
	}
   	
   	private void uploadPhoto(String filePath) {
   		File imgFile = new File(filePath);
   		if (! imgFile.exists()) 
   			return;
   		
   		Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
   		ByteArrayOutputStream stream = new ByteArrayOutputStream();
   		myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
   		byte[] bitmapData = stream.toByteArray();
   	}
   	
   	private static Uri getOutputMediaFileUri(int type) {
   		return Uri.fromFile(getOutputMediaFile(type));
   	}
   	
   	private static File getOutputMediaFile(int type) {
   		
   		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
   		
   		Log.d(TAG, "mediaStorageDir: " + mediaStorageDir.getPath());
   		
   		if (!mediaStorageDir.exists()) {
   			if (!mediaStorageDir.mkdirs()) {
   				Log.d(TAG, "failed to create directory");
   				return null;
   			}
   		}
   		
   		// create a media file name
   		String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
   		File mediaFile;
   		if (type == MEDIA_TYPE_IMAGE) {
   			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
   			Log.d(TAG, "media file name: " + mediaFile.getAbsolutePath());
   			
   		} else if( type == MEDIA_TYPE_VIDEO) {
   			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
   		} else {
   			return null;
   		}
   		
   		return mediaFile;
   	}
}
