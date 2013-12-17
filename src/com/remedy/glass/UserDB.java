package com.remedy.glass;

import java.util.HashMap;
import java.util.Map;

public class UserDB {
	/**
	 * This class should wrap the user information database and do things like ensure valid logins,
	 * etc. Right now it's just a dummy wrapper
	 */
	private static final Map <String, User> db = new HashMap<String,User>();
	static {
		db.put("12345", new User("brettcvz", "Brett van Zuiden", "Developer Extraordinare", "android.resource://com.remedy.glass/raw/brett"));
		db.put("54321", new User("noor", "Noor Siddiqui", "Super Duper Entrepenuer", "android.resource://com.remedy.glass/raw/noor"));
		db.put("wPX0ZjBHHOcSYIW", new User("suesiegel", "Sue Siegel", "CEO of Healthymagination", "android.resource://com.remedy.glass/raw/suesiegel"));
		db.put("GwBT7ZQkFCMlS44", new User("peterserp", "Peter Serpentino", "External Technology Initiatives", "android.resource://com.remedy.glass/raw/peterserp"));
		db.put("q51jPX0P4KV6v4q", new User("siavash", "Siavash Yazdanfar", "Applied Optics Laboratory", "android.resource://com.remedy.glass/raw/siavash"));
		db.put("PTp3hD506EH4O28", new User("alexdw", "Alex DeWinter", "Director of Healthcare Investment", "android.resource://com.remedy.glass/raw/alexdw"));
		db.put("eaTCcRC4AggczhL", new User("robfilk", "Robert Filkins", "Molecular Diagnostics Platforms", "android.resource://com.remedy.glass/raw/robfilk"));
	};
	
	public static User getUser(String loginCredentials) {
		return db.get(loginCredentials);
	}
	
	public static boolean isValidUser(String loginCredentials) {
		return db.containsKey(loginCredentials);
	}
}
