package com.remedy.glass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class VoiceTextObject {

	private String mName;
	private static final String TAG = "VOICETEXTOBJECT";
	private static final String POST_URL = "http://162.243.122.22/upload_text";
	
	public VoiceTextObject(String name) {
		setName(name);
		
	}
	
	public void send() {
		if (getName().length() == 0)
			return;
		
		new UploadVoiceTextTask().execute();
		
	}
	
	private void uploadText(String textToUpload) {
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(POST_URL);
		HttpResponse response;
		JSONObject jsonObject = new JSONObject();
		String jsonString = "";
		
		Log.d(TAG, "Trying to send data: " +textToUpload);
		
		try {
			jsonObject.accumulate("text", textToUpload);
			jsonString = jsonObject.toString();
			StringEntity se = new StringEntity(jsonString);
			httpPost.setEntity(se);
			
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			
			response = client.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();
			
			if (responseEntity != null) {
				InputStream instream = responseEntity.getContent();
				String result = convertStreamToString(instream);
				//JSONObject resultObject = new JSONObject(result);
				
				Log.d(TAG, "Result: " + result);
				
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
}
	
	private class UploadVoiceTextTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void...params) {
			try {
				uploadText(getName());
			} catch (Exception e) {
				Log.e(TAG, "Failed to POST: ", e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			
		}
	}
	
	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}
	
    private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();

    String line = null;
    try {
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    return sb.toString();
}

}
