package com.remedy.glass;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollView;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;

//Don't need to extend logout, as we're not logged in in the first place!
public class LoginActivity extends Activity {

	private List<Card> mCards;
	private CardScrollView mCardScrollView;
	private Handler mHandler;
	
	static final int LOGIN_SCAN_REQUEST = 0;
	static final int AUTOSCROLL_WAIT = 1500;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Keep the screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		createCards();
		final Context ctx = this;

		mCardScrollView = new CardScrollView(this);
		ListCardScrollAdapter adapter = new ListCardScrollAdapter(mCards);
		mCardScrollView.setAdapter(adapter);
		mCardScrollView.activate();
		setContentView(mCardScrollView);
		
		//When an item is clicked, if it's the splash screen move to the login
		//if it's the login screen launch the scanner.
		mCardScrollView.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            if (position == 0) {  // if splash screen selected, do nothing
	            	mCardScrollView.animateToIdSelection(1, 0);
	            } else if (position == 1){  // if login card selected, start CaptureActivity (QR code reader)
	            	Intent captureIntent = new Intent(ctx, CaptureActivity.class);
	            	startActivityForResult(captureIntent, LOGIN_SCAN_REQUEST);
	            }
	        }
	    });
		
		//Autoscrolls the user from the splash screen to the login screen
		mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {  
			@Override 
			public void run() {
				if (mCardScrollView.getSelectedItemPosition() == 0) {
					mCardScrollView.animateToIdSelection(1, 0);
				}
			}
		}, AUTOSCROLL_WAIT);
	}

	private void createCards() {
		mCards = new ArrayList<Card>();

		Card card;

		card = new Card(this);
		card.setText("Remedy");
		card.setInfo("For Google Glass");
		mCards.add(card);

		card = new Card(this);
		card.setText("Tap to Log In");
		
		mCards.add(card);
	}  
	
	// called after CaptureActivity finishes (i.e. QR Code reader)
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
		
        if (requestCode == LOGIN_SCAN_REQUEST) {
            if (resultCode == RESULT_OK) {
            	String loginCredentials = data.getStringExtra(Intents.Scan.RESULT);
                
            	// if QR code reading was successful, start MainActivity
                if (UserDB.isValidUser(loginCredentials)) {
                    Intent main = new Intent(this, MainActivity.class);
                    main.putExtra(MainActivity.LOGIN_INTENT_EXTRA_CREDENTIALS, loginCredentials);
                    finish();
                    startActivity(main);
                }
            } else if (resultCode == RESULT_CANCELED) {
            	System.out.println("Cancelled");
            }
        }
	}  // end onActivityResult
	
}  // end LoginActivity

