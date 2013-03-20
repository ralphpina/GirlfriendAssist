package com.outridersw.tapinspectandroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class PlistParser {
	
	//TODO Remove log tags and handle errors
	//for logging to console
	private static final String TAG = "PListParser";
	private static final String KeyTag = "key";
	private static final String Tag = "tag";
	private static final String ITEM = "item";
	
	public LinkedHashMap<String, Object> mLinkedHashMap;
	
	/***variables to convert to plist***/
	private FileOutputStream plistToWrite;
	public Context context;
	private String plistHeader = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                                    "<plist version=\"1.0\">\n" +
                                    "<array>\n\t" +
									"<dict>");
	private String plistFooter = new String("</dict>\n" +
									"</array>\n" +
									"</plist>");
	
	/***variables to convert from plist***/
	private FileInputStream plistToRead;
	private String sKeyTag = "<key>";
	//private String eKeyTag = "</key>";
	private String dict = "<dict>";
	private String endDict = "</dict>";	
	private String array = "<array>";
	private String endArray = "</array>";
	private String string = "<string>";
	private String integer = "<integer>";
	private String trueTag = "<true/>";
	private String real = "<real>";
	String content;
	private String tag = "";
	private String key = "";	
	private String value = "";
	//traversing Arrays
	private int beginTagInArrayIndex = 0;
	//traversing plist
	private int posIndex = 0;
	private int beginTagIndex = 0;
	private int endTagIndex = 0;
	
	/******Testing*******/
	private boolean debuggingConvertFromPlist = false;
	

	public PlistParser(Context context) {
		this.context = context;
	}
	
	public void convertToPlist(String fileName, LinkedHashMap<String, Object> linkedHashMap) {
		try {
			File file = context.getFileStreamPath(fileName);
			if (file.exists()) {
				context.deleteFile(fileName);
			}
			plistToWrite = context.openFileOutput(fileName, Context.MODE_PRIVATE);	
			plistToWrite.write(plistHeader.getBytes());
			parsePlist(linkedHashMap);
			plistToWrite.write(plistFooter.getBytes());
			plistToWrite.close();
		} catch (FileNotFoundException fnfe) {
			// TODO Auto-generated catch block
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}		
	}
	
	@SuppressWarnings("unchecked")
	private void parsePlist(LinkedHashMap<String, Object> linkedHashMap) {
		Object obj;
		String key;
		
		for (String keys : linkedHashMap.keySet()) {
			key = keys;
			obj = linkedHashMap.get(key);
			//Log.e(TAG, "key = " + key);
			//Log.e(TAG, "obj = " + obj + " type = " + obj.getClass());
			try {
				if (obj instanceof LinkedHashMap<?,?>) {
					plistToWrite.write(("<key>" + key + "</key>").getBytes());
					plistToWrite.write("<dict>".getBytes());
					parsePlist((LinkedHashMap<String, Object>)obj);
					plistToWrite.write("</dict>".getBytes());
				} else if (obj instanceof ArrayList<?>) {
					plistToWrite.write(("<key>" + key + "</key>").getBytes());
					plistToWrite.write("<array>".getBytes());					
					for (Object i : (ArrayList<?>)obj) {
						if (i instanceof String) {
							plistToWrite.write(("<string>" + i.toString() + "</string>").getBytes());
						} else if (i instanceof Integer) {
							plistToWrite.write(("<integer>" + i.toString() + "</integer>").getBytes());
						} else if (i instanceof Boolean) {
							plistToWrite.write(((Boolean) i).booleanValue() ? ("<true/>").getBytes() : ("<false/>").getBytes());
						}
					}	
					plistToWrite.write("</array>".getBytes());
				} else if (obj instanceof String) {
					plistToWrite.write(("<key>" + key + "</key>").getBytes());
					plistToWrite.write(("<string>" + obj.toString() + "</string>").getBytes());
				} else if (obj instanceof Integer) {
					plistToWrite.write(("<key>" + key + "</key>").getBytes());
					plistToWrite.write(("<integer>" + obj.toString() + "</integer>").getBytes());
				} else if (obj instanceof Boolean) {
					plistToWrite.write(("<key>" + key + "</key>").getBytes());
					plistToWrite.write(((Boolean) obj).booleanValue() ? ("<true/>").getBytes() : ("<false/>").getBytes());
				} else {
					plistToWrite.write(("<key>" + key + "</key>").getBytes());
					plistToWrite.write(("").getBytes());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}
	
	public LinkedHashMap<String, Object> convertFromPlist(String filename) {
		mLinkedHashMap = new LinkedHashMap<String, Object>();
		
		Log.i(TAG, "filename = " + filename);
		
		try {
			plistToRead = context.openFileInput(filename);
			InputStreamReader reader = new InputStreamReader(plistToRead);
			BufferedReader br = new BufferedReader(reader);
			
			String in = null;
			content = "";
			
			while ((in = br.readLine()) != null) {
				content += in;
			}
			
			plistToRead.close();
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "PListParser:convertFromPlist: File not found! " + filename);
			e.printStackTrace();
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}
		
		if (debuggingConvertFromPlist) {
			Log.i(TAG, "content = " + content.toString());
			
			try {
				
				File root = Environment.getExternalStorageDirectory();               
				File dir = new File (root.getAbsolutePath() + "/TapInspect");
		        if(dir.exists() == false) {
		            dir.mkdirs();
		        }
		        File file = new File(dir, "test_content_from_plist_parser.txt");
		        plistToWrite = new FileOutputStream(file);
		        
				plistToWrite.write(content.getBytes());
				plistToWrite.close();
			} catch (FileNotFoundException fnfe) {
				// TODO Auto-generated catch block
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}	
		}
				
		convertFromPlist(content, mLinkedHashMap);
		
		if (debuggingConvertFromPlist) {
			Log.e(TAG, "mLinkedHashMap before returning = " + mLinkedHashMap.toString());
		}	
		return mLinkedHashMap;
	}
	
	public void convertFromPlist(String data, LinkedHashMap<String, Object> map) {
		content = data;
		posIndex = content.indexOf(dict) + 6;
		addItemsToMap(map);
	}
	
	private void addItemsToMap(LinkedHashMap<String, Object> map) {
		
		tag = getNextTag();
		
		while ((tag != null) && !(tag.equals(endDict))) {
			
			if (tag.equals(sKeyTag)) {
				key = getCurrentValue();		
				tag = getNextTag();
			}
	
			if (tag.equals(dict)) {
				String tempDictKey = key;
				LinkedHashMap<String, Object> tempDict = new LinkedHashMap<String, Object>();
	
					if (debuggingConvertFromPlist) {
						Log.i(TAG + " " + KeyTag, " = " + key);
						Log.i(TAG + " value", " = " + value);
					}
					
					addItemsToMap(tempDict);

				if (debuggingConvertFromPlist) {
					Log.e(TAG, "tempDict = " + tempDict.toString());
				}
				map.put(tempDictKey, tempDict);
			} 
			
			if (tag.equals(array)) {
				String tempArrayKey = key;
				int beginClosingArrayTagIndex = 0;
				ArrayList<Object> tempArray = new ArrayList<Object>();
				
				beginClosingArrayTagIndex = content.indexOf(endArray, posIndex + 1);
				while (posIndex < beginClosingArrayTagIndex) {
					if (!(tag = getNextTag()).equals(endArray)) {
						addItemToArray(tempArray, tag);					
					}	
					if (debuggingConvertFromPlist) {
						Log.e(TAG + " " + Tag, " = " + tag);
					}
				}
				map.put(tempArrayKey, tempArray);			
			} 
			
			if (tag.equals(endArray)) {
				ArrayList<Object> tempArray = new ArrayList<Object>();
				map.put(key, tempArray);
				if (debuggingConvertFromPlist) {
					Log.e(TAG + " " + KeyTag, " = " + key);
					Log.e(TAG + " " + Tag, " = " + tag);
				}
			}
			
			if (tag.equals(string)) {
				value = getCurrentValue();
				map.put(key, value);	
				if (debuggingConvertFromPlist) {
					Log.e(TAG + " " + KeyTag, " = " + key);
					Log.e(TAG + " " + Tag, " = " + tag);
					Log.e(TAG + " " + ITEM, " = " + value);
				}
			} 
			else if (tag.equals(integer)) {
				value = getCurrentValue();
				map.put(key, Integer.valueOf(value));
				if (debuggingConvertFromPlist) {
					Log.e(TAG + " " + KeyTag, " = " + key);
					Log.e(TAG + " " + Tag, " = " + tag);
					Log.e(TAG + " " + ITEM, " = " + value);
				}
			} 
			else if (tag.equals(real)) {
				value = getCurrentValue();
				map.put(key, Double.valueOf(value));	
				if (debuggingConvertFromPlist) {
					Log.e(TAG + " " + KeyTag, " = " + key);
					Log.e(TAG + " " + Tag, " = " + tag);
					Log.e(TAG + " " + ITEM, " = " + value);
				}
			} 
			else if (tag.equals(trueTag)) {
				map.put(key, Boolean.valueOf(true));	
				if (debuggingConvertFromPlist) {
					Log.e(TAG + " " + KeyTag, " = " + key);
					Log.e(TAG + " " + Tag, " = " + tag);
					Log.e(TAG + " " + ITEM, "true");
				}
			} 
			else if (tag.equals(false)) {
				map.put(key, Boolean.valueOf(false));	
				if (debuggingConvertFromPlist) {
					Log.e(TAG + " " + KeyTag, " = " + key);
					Log.e(TAG + " " + Tag, " = " + tag);
					Log.e(TAG + " " + ITEM, "false");
				}
			}
			
			tag = getNextTag();
		}
	}
	
	private void addItemToArray(ArrayList<Object> array, String tag) {
		
		if (this.tag.equals(string)) {
			int endTag = content.indexOf("<", beginTagInArrayIndex + 1);
			array.add(content.substring(beginTagInArrayIndex + 8, endTag));	
			if (debuggingConvertFromPlist) {
				Log.e(TAG + " " + Tag, " = " + this.tag);
				Log.e(TAG + " " + ITEM, content.substring(beginTagInArrayIndex + 8, endTag));
			}
		} else if (this.tag.equals(integer)) {
			int endTag = content.indexOf("<", beginTagInArrayIndex + 1);
			array.add(Integer.valueOf(content.substring(beginTagInArrayIndex + 9, endTag)));
			if (debuggingConvertFromPlist) {
				Log.e(TAG + " " + Tag, " = " + this.tag);
				Log.e(TAG + " " + ITEM, content.substring(beginTagInArrayIndex + 9, endTag));
			}
		} else if (this.tag.equals(real)) {
			int endTag = content.indexOf("<", beginTagInArrayIndex + 1);
			array.add(Double.valueOf(content.substring(beginTagInArrayIndex + 6, endTag)));	
			if (debuggingConvertFromPlist) {
				Log.e(TAG + " " + Tag, " = " + this.tag);
				Log.e(TAG + " " + ITEM, content.substring(beginTagInArrayIndex + 6, endTag));
			}
		} else if (this.tag.equals(trueTag)) {
			array.add(Boolean.valueOf(true));	
			if (debuggingConvertFromPlist) {
				Log.e(TAG + " " + Tag, " = " + this.tag);
				Log.e(TAG + " " + ITEM, "true");
			}
		} else if (this.tag.equals(false)) {
			array.add(Boolean.valueOf(false));	
			if (debuggingConvertFromPlist) {
				Log.e(TAG + " " + Tag, " = " + this.tag);
				Log.e(TAG + " " + ITEM, "false");
			}
		}
	}
	
	private String getCurrentValue() {
		beginTagIndex = content.indexOf("<", posIndex);
		posIndex = beginTagIndex + 1;
		if (debuggingConvertFromPlist) {
			Log.e(TAG + " beginTagIndex", " = " + beginTagIndex);
			Log.e(TAG + " endTagIndex", " = " + endTagIndex);
		}
		return content.substring(endTagIndex + 1, beginTagIndex);
	}
	
	private String getNextTag() {
		beginTagIndex = content.indexOf("<", posIndex);
		if (beginTagIndex == -1)
			return null;
		endTagIndex = content.indexOf(">", beginTagIndex);
		posIndex = endTagIndex;
		if (debuggingConvertFromPlist) {
			Log.e(TAG + " beginTagIndex", " = " + beginTagIndex);
			Log.e(TAG + " endTagIndex", " = " + endTagIndex);
		}
		return content.substring(beginTagIndex, endTagIndex + 1);
	}
	
	public String getPlistHeader() {
		return plistHeader;
	}
	
	public String getPlistFooter() {
		return plistFooter;
	}
	
	/*** TESTING ***/
	/*** Dump a plist into the phone's SDCard so it can be manually inspected ***/
	public void convertToPlistSDCard(String fileName, LinkedHashMap<String, Object> linkedHashMap) {
		try {
			
			File root = Environment.getExternalStorageDirectory();               
			File dir = new File (root.getAbsolutePath() + "/TapInspect");
	        if(dir.exists() == false) {
	            dir.mkdirs();
	        }
	        File file = new File(dir, fileName);
	        plistToWrite = new FileOutputStream(file);
	        
	        plistToWrite.write(plistHeader.getBytes());
	        parsePlist(linkedHashMap);
			plistToWrite.write(plistFooter.getBytes());
			plistToWrite.close();
		} catch (FileNotFoundException fnfe) {
			// TODO Auto-generated catch block
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}		
	}
	/*** TESTING ***/
}
