package com.outridersw.tapinspectandroid.test;

import java.io.File;
import java.util.LinkedHashMap;

import android.test.AndroidTestCase;
import android.util.Log;

import com.outridersw.tapinspectandroid.PlistParser;

public class PlistParserTest extends AndroidTestCase {
	
	//for logging to console
	private static final String TAG = "PlistParserTest";
	
	private PlistParser parser;
	private String fileName = "testplist.plist";
	private LinkedHashMap<String, Object> testLinkedHashMap = new LinkedHashMap<String, Object>();
	private LinkedHashMap<String, Object> testLinkedHashMap2 = new LinkedHashMap<String, Object>();
	
	@Override
	protected void setUp() throws Exception {
		
		Log.i(TAG, "Starting PlistParserTest");
		
		parser = new PlistParser(mContext);
		
		//build a nested LinkedHashMap for testing
		LinkedHashMap<String, Object> interiorLinkedHashMap1 = new LinkedHashMap<String, Object>();
		interiorLinkedHashMap1.put("allowmulti", "yes");
		interiorLinkedHashMap1.put("itemtype", "address");
		interiorLinkedHashMap1.put("option", "");
		interiorLinkedHashMap1.put("changed", Boolean.valueOf(false));
		interiorLinkedHashMap1.put("lastsyncedrev", Integer.valueOf(5));
				
		LinkedHashMap<String, Object> interiorLinkedHashMap2 = new LinkedHashMap<String, Object>();
		interiorLinkedHashMap2.put("shortdesc", "name and organization");
		interiorLinkedHashMap2.put("firstname", "Ralph");
		interiorLinkedHashMap2.put("prohibitcondition", "yes");
		interiorLinkedHashMap2.put("suppressprivacynotice", Boolean.valueOf(true));
		interiorLinkedHashMap2.put("number", Integer.valueOf(10));
		
		testLinkedHashMap.put("name", "Ralph Pina");
		testLinkedHashMap.put("suppressprivacynotice", Boolean.valueOf(true));
		testLinkedHashMap.put("first list", interiorLinkedHashMap1);
		testLinkedHashMap.put("lastsyncedrev", Integer.valueOf(5));
		testLinkedHashMap.put("city", "Round Rock");
		testLinkedHashMap.put("second list", interiorLinkedHashMap2);
		testLinkedHashMap.put("state", "Texas");
	}
	
	public void testconvertToPlist() {
		//test that we can save it to internal storage
		parser.convertToPlist(fileName, testLinkedHashMap);
		File file = this.getContext().getFileStreamPath(fileName);
		assertTrue(file.exists());
		//only for testing, dump the plist into the SDCard
		parser.convertToPlistSDCard(fileName, testLinkedHashMap);
		
	}
	
	public void testConvertFromPlist() {
		/*** Debugging
		parser.convertToPlist(fileName, testLinkedHashMap);
		parser.convertToPlistSDCard(fileName, testLinkedHashMap);
		File file = this.getContext().getFileStreamPath(fileName);
		assertTrue(file.exists());
		***/
		parser.convertToPlist(fileName, testLinkedHashMap);
		File file = this.getContext().getFileStreamPath(fileName);
		assertTrue(file.exists());
		//parse the plist into a LinkedHashMap
		testLinkedHashMap2 = parser.convertFromPlist(fileName);
		//test that fields are in the LinkedHashMap
		assertEquals("Ralph Pina", testLinkedHashMap2.get("name"));
		assertEquals("yes", ((LinkedHashMap<?, ?>)testLinkedHashMap2.get("first list")).get("allowmulti"));
		//only for testing, dump the plist into the SDCard
		parser.convertToPlistSDCard("testplist2.plist", testLinkedHashMap2);	
	}
	
	@Override
	public void tearDown() throws Exception {
		Log.i(TAG, "Ending PlistParserTest");
		//delete file
		File file = mContext.getFileStreamPath(fileName);
		if (file.exists()) {
			mContext.deleteFile(fileName);
		}
	}

}
