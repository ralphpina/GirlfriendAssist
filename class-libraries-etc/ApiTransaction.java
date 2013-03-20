package com.outridersw.tapinspectandroid;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONML;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class ApiTransaction {
	
	//for logging to console
	private static final String TAG = "ApiTransaction";
	//to make calls to the dev server
	public boolean testing = false;
	//to print out log messages
	private boolean debugging = false;
	
	public enum SyncStatusType {
		SYNC_STATUS_OK,
		SYNC_STATUS_AUTH_FAIL,
	    SYNC_STATUS_PARSE_ERR,
	    SYNC_STATUS_XMSN_ERR,
	    SYNC_STATUS_SERVER_ERR,
	    SYNC_STATUS_INTERNAL_ERR,
	    SYNC_STATUS_NOT_SUBSCRIBED_ERR
	}
	
	protected boolean abort;
	protected JSONObject receivedData;
	protected String taskEmail;
	protected String taskPassword;
	public SyncStatusType syncStatus;
    Context context;
    
    private String device_uuid;
    private int device_sw_version;
    private String device_os_version;
    private String device_description;
    
    private HttpURLConnection connection;
    private DataOutputStream outputStream;
    InputStream response;
    private URL url;
    private String httpMethod;
    protected List<ArrayList<String>> request;
    private String serverResponseMessage;
    private int serverResponseCode;
    private int contentLength = -1;
    
    private String lineEnd = "\r\n";
    private String twoHyphens = "--";
    
    public ApiTransaction(String email, String password, Context context) {
    	this.context = context;
    	this.taskEmail = email;
    	this.taskPassword = password;
    	abort = false;
    	syncStatus = SyncStatusType.SYNC_STATUS_OK;
    	
    	device_uuid = DeviceInfo.getUniqueIdentifier(context);
    	
    	try {
			device_sw_version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	device_os_version = android.os.Build.VERSION.RELEASE;
    	device_description = DeviceInfo.getDeviceType();
    	request = new ArrayList<ArrayList<String>>();
    	receivedData = null;
    	
    }
    
    //Will probable get this stuff from nameValuePairs
    private String buildUrlForEndpoint(String toURL) {
    	String deviceString = "device[uuid]=" +
        device_uuid +
        "&device[sw_version]=" +
        device_sw_version +
        "&device[os_version]=" +
        device_os_version +
        "&device[description]=" +
        device_description + 
        "&device[platform]=android";
    	
    	String url = (this.testing ? StaticStrings.DEV_OUTRIDER_SERVER_URL : StaticStrings.OUTRIDER_SERVER_URL);
    	
    	if (debugging) {
    		Log.i(TAG, "buildUrlForEndpoint = " + (url + toURL + "?" + deviceString));
    	}
    	return url + toURL + "?" + deviceString;
    }
    
    
    public void buildApiRequestWithURL(String toURL, String httpMethod) throws IOException {
		this.url = new URL(buildUrlForEndpoint(toURL));
		this.httpMethod = httpMethod;
    }
    
     public void addPostParameters(String params) throws IOException {
    	 if (params != null) {
    		 contentLength = params.length();
    		 ArrayList<String> item = new ArrayList<String>();
    		 item.add(params);
    		 this.request.add(item);
     	}
     }
     
    
    public void addMultipartParameter(String name, String value, String boundary) {
        if ((name != null) && (value != null) && (boundary != null)) {
        	ArrayList<String> item = new ArrayList<String>();
        	item.add(boundary);
        	item.add(name);
        	item.add(value);
        	this.request.add(item);
        }
    }
    
    public void addMultipartParameter(String name, String fileName, String boundary, String contentType) {
        if ((name != null) && (fileName != null) && (boundary != null) && (contentType != null)) {
        	ArrayList<String> item = new ArrayList<String>();
        	item.add(boundary);
        	item.add(name);
        	item.add(fileName);
        	item.add(contentType);
        	this.request.add(item);
        }
    }
    
    public void execute() {
    	new AsyncTransaction().execute(this.httpMethod, this.taskEmail, this.taskPassword);    	
    }
	
    public void transactionComplete(boolean transactionSuccess) {
		//stud!!!
	}
	
	public void cancel() {
	    abort = true;
	}
	
	private class AsyncTransaction extends AsyncTask<String, Integer, String> {
		
		private String getB64Auth(String username, String password) {
	        String source= username + ":" + password;
	        String ret= Base64.encodeToString(source.getBytes(),Base64.DEFAULT);
	        return ret;
	    }
		
		@Override
		protected String doInBackground(String... args) {
			
			String httpMethod = args[0];
			String taskEmail = args[1];
			String taskPassword = args[2];
			
			try {
				//accept no cookies
				//TODO requires Gingerbread and up, we'll need to decide on this.
				//CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_NONE));
				connection = (HttpURLConnection) url.openConnection();
	            
				// Allow Inputs & Outputs
				connection.setRequestMethod(httpMethod);
				connection.setDoInput(true);
				connection.setUseCaches(false);			
				if (httpMethod.equals("POST")) {
					connection.setDoOutput(true);
					if (contentLength != -1)
						connection.setRequestProperty("Content-length", (Integer.valueOf(contentLength).toString()));
				}
	            			
				//Headers
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Accept", "application/xml");
				connection.setRequestProperty("Authorization", "Basic " + getB64Auth(taskEmail, taskPassword));
		
				if (httpMethod.equals("GET")) {
					if (debugging) {
						Log.i(TAG, "GET");
					}
					
					response = connection.getInputStream();
					
					serverResponseMessage = "";
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(response));
					String line = "";
				    while ((line = reader.readLine()) != null) {
				    	serverResponseMessage += line;
				    }
					
					reader.close();		
					response.close();
					
					// Responses from the server (code and message)
					serverResponseCode = connection.getResponseCode();

					if (debugging) {
						Log.i(TAG, "serverResponseCode" + serverResponseCode);
						Log.i(TAG, "serverResponseMessage = " + serverResponseMessage);
					}
					
				}//end GET portion
				
				if ( httpMethod.equals("POST")) {
					if (debugging) {
						Log.i(TAG, "POST");
					}

					//buffer for file transfers
					int bytesRead, bytesAvailable, bufferSize;
					byte[] buffer;
					int maxBufferSize = 1*1024*1024;
					
					//open stream and start writting
					outputStream = new DataOutputStream(connection.getOutputStream());
					
					for (ArrayList<String> part : request) {
						
						if (abort) break;			//stop transmitting if cancel is called
						
						if (part.size() == 1) {

							outputStream.writeBytes(part.get(0));
							
						} else if (part.size() == 3) {		//if it is a simple multipart parameter		
							
							outputStream.writeBytes(twoHyphens + part.get(0) + lineEnd);
							outputStream.writeBytes("Content-Disposition: form-data; name=\"" + part.get(1) + "\"" + lineEnd);
							outputStream.writeBytes(lineEnd);
							outputStream.writeBytes(part.get(2));
							outputStream.writeBytes(lineEnd);
							
						} else {					//else, we are sending up a file
							
							outputStream.writeBytes(twoHyphens + part.get(0) + lineEnd);
							outputStream.writeBytes("Content-Disposition: form-data; name=\"" + part.get(1) + "\"; filename=\"" + part.get(2) +"\"" + lineEnd);
							outputStream.writeBytes("Content-Type: " + part.get(3) + "" + lineEnd);
							outputStream.writeBytes(lineEnd);
							
							//File I/O
							FileInputStream fileInputStream = new FileInputStream(context.getFileStreamPath(part.get(2)));
							
							bytesAvailable = fileInputStream.available();
							bufferSize = Math.min(bytesAvailable, maxBufferSize);
							buffer = new byte[bufferSize];
							
							// Read file
							bytesRead = fileInputStream.read(buffer, 0, bufferSize);
							
							while (bytesRead > 0)
							{
				                outputStream.write(buffer, 0, bufferSize);
				                bytesAvailable = fileInputStream.available();
				                bufferSize = Math.min(bytesAvailable, maxBufferSize);
				                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
							}
							
							fileInputStream.close();
							
							outputStream.writeBytes(lineEnd);
						}
					}
		            
					outputStream.flush();
					outputStream.close();		
					
					response = connection.getInputStream();
					
					serverResponseMessage = "";
					
					Log.e(TAG, "before BufferedReader");
					BufferedReader reader = new BufferedReader(new InputStreamReader(response));
					String line = "";
				    while ((line = reader.readLine()) != null) {
				    	Log.e(TAG, line);
				    	serverResponseMessage += line;
				    }
					
					reader.close();		
					response.close();
					
					serverResponseCode = connection.getResponseCode();

					if (debugging) {
						Log.i(TAG, "serverResponseCode = " + serverResponseCode);
						Log.i(TAG, "serverResponseMessage = " + serverResponseMessage);
					}
				}//end POST portion
							
				Log.e(TAG, "end post");
				if (serverResponseCode != 200) {
					if(serverResponseCode == 401) {
						Log.e(TAG, "authentication error: un: " + taskEmail + " pw: " + taskPassword);
						syncStatus = SyncStatusType.SYNC_STATUS_AUTH_FAIL;
					} else if(serverResponseCode >= 400) {
						Log.e(TAG, "failed with server error = " + serverResponseCode + " and code = " + serverResponseCode);
						syncStatus = SyncStatusType.SYNC_STATUS_SERVER_ERR;
					} else {
						Log.e(TAG, "http status = " + serverResponseCode + " and code = " + serverResponseCode);
					}
				}

			} catch (IOException e) {			
				try {
					serverResponseCode = connection.getResponseCode();
					serverResponseMessage = connection.getResponseMessage();
					
					Log.e(TAG, "serverResponseCode = " + serverResponseCode);
					Log.e(TAG, "serverResponseMessage = " + serverResponseMessage);
					
					if(serverResponseCode == 401) {
						Log.e(TAG, "authentication error: un: " + taskEmail + " pw: " + taskPassword);
						syncStatus = SyncStatusType.SYNC_STATUS_AUTH_FAIL;
					} else if(serverResponseCode == 500) {
						Log.e(TAG, "failed with status code 500");
						syncStatus = SyncStatusType.SYNC_STATUS_SERVER_ERR;
					} else {
						Log.e(TAG, "http status = " + serverResponseCode + " and code = " + serverResponseCode);
						syncStatus = SyncStatusType.SYNC_STATUS_XMSN_ERR;
					}
					transactionComplete(false);
					
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
					ioe.printStackTrace();
				}
			}
			
			if (debugging) {
    			Log.i(TAG, "before return");
    			Log.i(TAG, "serverResponseMessage = " + serverResponseMessage);
			}
			return serverResponseMessage;
		}
	    
		@Override
		protected void onPostExecute(String result) {
			//if (debugging) {
				Log.i(TAG, "entered onPostExecute : result = " + result);
			//}
			super.onPostExecute(result);
			if(result != null && serverResponseCode == 200) {
				try {
					int startIndex = result.indexOf("<plist version=\"1.0\">") + 21;
				    int endIndex = result.indexOf("</plist>");	
				    
				    if (startIndex != 20 && endIndex != -1) {
				    	result = result.substring(startIndex, endIndex);
				    }
				    receivedData = JSONML.toJSONObject(result);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					syncStatus = SyncStatusType.SYNC_STATUS_PARSE_ERR;
				}
				transactionComplete(syncStatus == SyncStatusType.SYNC_STATUS_OK);
			} 
		}
		
	}
}
