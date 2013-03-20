package com.outridersw.tapinspectandroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity { // implements ApiGetRevisionsDelegate {
	
	//for logging to console
	//private static final String TAG = "MainActivity";

	/*
 	UserInfoCache userInfoCache;
	LinkedHashMap<String, Object> plist;
	LinkedHashMap<String, Object> records;
	String fileName;
	PlistParser parser;	
	*/
	
	//networking
	//boolean asyncTaskDone;	
	//ApiGetRevisions mm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*
		fileName = "test_user_info_cache_file_main_activity.plist";		
		plist = new LinkedHashMap<String, Object>();
		records = new LinkedHashMap<String, Object>();
		parser = new PlistParser(this);
		
		InspectionItem_Email email1 = new InspectionItem_Email();
		email1.linkedHashMap.put(StaticStrings.PLIST_ITEM_TEXTDB, "ralph.pina@gmail.com");
		UserInfoRecord userInfoRecord1 = new UserInfoRecord();
		userInfoRecord1.userEmail = email1;
		userInfoRecord1.userID = "ralph";
		userInfoRecord1.defaultPublishRecipients.add("Jerome");
		userInfoRecord1.writeToMap(plist);		
		records.put("first", plist);
		
		plist = new LinkedHashMap<String, Object>();
		
		InspectionItem_Email email = new InspectionItem_Email();
		email.linkedHashMap.put(StaticStrings.PLIST_ITEM_TEXTDB, "jason.adams@gmail.com");
		UserInfoRecord userInfoRecord2 = new UserInfoRecord();
		userInfoRecord2.userEmail = email;
		userInfoRecord2.userID = "jason";	
		userInfoRecord2.writeToMap(plist);
		records.put("second", plist);
		parser.convertToPlist(fileName, records);
		
		Log.w(TAG, "records for userInfoRecord = " + records.toString());
		
		File file = (this).getFileStreamPath(fileName);
		Log.i(TAG, "file exists = " + file.exists());
		userInfoCache = new UserInfoCache(this, fileName);
		
		LinkedHashMap<String, Object> testPlist = new LinkedHashMap<String, Object>();
		userInfoCache.users.get(0).writeToMap(testPlist);
		Log.i(TAG, "testPlist = " + testPlist);
		*/	
		
		/*Testing networking 
		
		try {
			mm = new ApiGetRevisions("tester", "monkey", this);
        
			//use dev URL
			mm.testing = true;
			
			mm.goWithDelegate(this);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
