package com.outridersw.tapinspectandroid;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.params.ClientPNames;
import ch.boye.httpclientandroidlib.client.params.CookiePolicy;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;
import ch.boye.httpclientandroidlib.impl.client.AbstractHttpClient;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class ApiTransaction {
	
	//for logging to console
	private static final String TAG = "ApiTransaction";
	//to make calls to the dev server
	public boolean testing = true;
	//to print out log messages
	private boolean debugging = true;
	
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
    
    protected HttpClient httpclient;
    MultipartEntity reqEntity;
    StringEntity entity;
    HttpResponse response;
    private String url;
    private String httpMethod;
    protected List<ArrayList<String>> request;
    private String serverResponseMessage;
    private String serverResponse;
    private int serverResponseCode = -1;
    String contentLength = null;
    
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
    	
    	httpclient = new DefaultHttpClient();
    	
    	reqEntity = new MultipartEntity(HttpMultipartMode.STRICT, "--begin-boundary--" + System.currentTimeMillis() + "--end-boundary--", Charset.forName("UTF-8"));
    	
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
		this.url = buildUrlForEndpoint(toURL);
		this.httpMethod = httpMethod;
    }
    
     public void addPostParameters(String params) throws IOException {
    	 if (params != null) {
    		 //this.reqEntity.addPart("Content-length", StringBody.create(Integer.valueOf(params.length()).toString(), "text/plain", Charset.forName("UTF-8")));
    		 //this.reqEntity.addPart("", StringBody.create(params, "text/plain", Charset.forName("UTF-8")));
    		 Log.e(TAG, "params = " + params);
    		 contentLength = Integer.toString(params.length());
    		 entity = new StringEntity(params);
     	}
     }     
    
    public void addMultipartParameter(String name, String value) {
        if ((name != null) && (value != null)) {
        	this.reqEntity.addPart(name, StringBody.create(value));
        }
    }
    
    public void addMultipartParameter(String name, File file) {
        if ((name != null) && (file != null)) {
        	this.reqEntity.addPart(name, new FileBody(file));
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

			url = url.replace(" ", "%20");
			Log.e(TAG, "url = " + url);
			
			try {
				
				httpclient.getParams().setParameter(
				        ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);	

				if (httpMethod.equals("GET")) {				
					if (debugging) {
						Log.i(TAG, "GET");
					}
					
					HttpGet httpget = new HttpGet(url);
					httpget.setHeader("Content-Type", "application/x-www-form-urlencoded");
					httpget.setHeader("Accept", "application/xml");
					httpget.setHeader("Authorization", "Basic " + getB64Auth(taskEmail, taskPassword));
					
					response = httpclient.execute(httpget);		
					
					if (debugging) {
						Log.e(TAG, "end get");
					}
				}//end GET portion
					
				if ( httpMethod.equals("POST")) {
					if (debugging) {
						Log.i(TAG, "POST");
					}
					
					HttpPost httppost = new HttpPost(url);
					httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
					httppost.setHeader("Accept", "application/xml");
					httppost.setHeader("Authorization", "Basic " + getB64Auth(taskEmail, taskPassword));
					
					Log.e(TAG, "contentLength = " + contentLength);
					if (contentLength != null)
						httppost.setHeader("Content-length", contentLength);

					if (entity != null) {
						httppost.setEntity(entity);
					} else {
						httppost.setEntity(reqEntity);
					}
					
					response = httpclient.execute(httppost);
					
					if (debugging) {
						Log.e(TAG, "end post");
					}
				}					
					
				 serverResponse = EntityUtils.toString(response.getEntity());
				
				// Responses from the server (code and message)
				serverResponseCode = response.getStatusLine().getStatusCode();
				serverResponseMessage = response.toString();

				if (debugging) {
					Log.i(TAG, "serverResponseCode = " + serverResponseCode);
					Log.i(TAG, "serverResponseMessage = " + serverResponseMessage);
				}
																
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

			} catch (Exception e) {			

				if (serverResponseCode != -1 && serverResponseMessage != null) {
					serverResponseCode = response.getStatusLine().getStatusCode();
					serverResponseMessage = response.getStatusLine().getReasonPhrase();
				}
				
				Log.e(TAG, "Exception = " + e.getMessage());
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
					 
			}
			
			if (debugging) {
    			Log.i(TAG, "before return");
    			Log.i(TAG, "serverResponseMessage = " + serverResponseMessage);
			}
			
			httpclient.getConnectionManager().shutdown();
			return serverResponse;
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
