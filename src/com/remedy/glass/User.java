package com.remedy.glass;

import android.net.Uri;

public class User {
	public String username;
	public String fullName;
	public String title;
	public Uri image;
	
	public User(String username, String fullName, String title, String image) {
		this.username = username;
		this.fullName = fullName;
		this.title = title;
		this.image = Uri.parse(image);
	}
}
