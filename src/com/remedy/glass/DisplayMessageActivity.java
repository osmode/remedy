package com.remedy.glass;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.glass.app.Card;

public class DisplayMessageActivity extends LogoutOnStopActivity {
	public static final String EXTRA_TEXT = "extra_text";
	public static final String EXTRA_SUBTEXT = "extra_subtext";
	public static final String EXTRA_IMAGE = "extra_image";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Keep the screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Intent i = getIntent();
		//We null out empty strings for cleanliness
		String text = i.getStringExtra(EXTRA_TEXT);
		text = text.length() > 0 ? text : null;
		String subtext = i.getStringExtra(EXTRA_SUBTEXT);
		subtext = subtext.length() > 0 ? subtext : null;
		String image = i.getStringExtra(EXTRA_IMAGE);
		image = image.length() > 0 ? image : null;
    		
		Card card = createMessageCard(text, subtext, image);	
		setContentView(card.toView());
	}
	
	public Card createMessageCard(String text, String subtext, String image) {
		Card card = new Card(this);
		
		if (text != null) {
			card.setText(text);
		}
		if (subtext != null) {
			card.setInfo(subtext);
		}
		if (image != null) {
			String uriString = "android.resource://com.remedy.glass/raw/" + image;
			card.addImage(Uri.parse(uriString));
		}
		
		if (image != null && subtext == null) {
			card.setFullScreenImages(true);
		}
		
		return card;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.display_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection. Menu items typically start another
        // activity, start a service, or broadcast another intent.
        switch (item.getItemId()) {
            case R.id.acknowledge_menu_item:
            	closeOptionsMenu();
                finish();
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
