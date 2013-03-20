package com.outridersw.tapinspectandroid;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;

public class ApiTransaction extends AsyncTask<String, String, String>{
	
	//for logging to console
	private static final String TAG = "ApiTransaction";
	
	public enum SyncStatusType {
		SYNC_STATUS_OK,
		SYNC_STATUS_AUTH_FAIL,
	    SYNC_STATUS_PARSE_ERR,
	    SYNC_STATUS_XMSN_ERR,
	    SYNC_STATUS_SERVER_ERR,
	    SYNC_STATUS_INTERNAL_ERR,
	    SYNC_STATUS_NOT_SUBSCRIBED_ERR
	}
	
	private boolean abort;
	private String taskEmail;
	private String taskPassword;
	private SyncStatusType syncStatus;
    Context context;
    
    private String device_uuid;
    private int device_sw_version;
    private String device_os_version;
    private String device_description;
    
    private HttpURLConnection connection;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private URL url;
    private String httpMethod;
    private List<String> request;
    
    private String lineEnd = "\r\n";
    private String twoHyphens = "--";
    
    public ApiTransaction(String email, String password, Context context) throws NameNotFoundException {
    	this.context = context;
    	this.taskEmail = email;
    	this.taskPassword = password;
    	abort = false;
    	syncStatus = SyncStatusType.SYNC_STATUS_OK;
    	
    	device_uuid = DeviceInfo.getUniqueIdentifier(context);
    	device_sw_version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    	device_os_version = android.os.Build.VERSION.RELEASE;
    	device_description = DeviceInfo.getDeviceType();
    	request = new ArrayList<String>();
    	
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
        device_description;
    	
    	return StaticStrings.OUTRIDER_SERVER_URL + toURL + "?" + deviceString;
    }
    
    
    public void buildApiRequestWithURL(String toURL, String httpMethod) throws IOException {
		this.url = new URL(buildUrlForEndpoint(toURL));
		this.httpMethod = httpMethod;
    }
    
    /*
     public void addPostParameters(String params) throws IOException {
     if (params != null) {
     connection.setRequestProperty("Content-length", (Integer.valueOf(params.length())).toString());
     outputStream.writeBytes(params);
     }
     }
     */
    
    public void addMultipartParameter(String name, String value, String boundary) {
        if ((name != null) && (value != null) && (boundary != null)) {
        	request.add(twoHyphens + boundary + lineEnd);
        	request.add("Content-Disposition: form-data; name=\"" + name + "\"" + lineEnd);
        	request.add(lineEnd);
        	request.add(value);
        	request.add(lineEnd);
        }
    }
    
    public void addMultipartParameter(String name, String fileName, String boundary, boolean withFile) {
        if ((name != null) && (fileName != null) && (boundary != null) && withFile) {
        	request.add(twoHyphens + boundary + lineEnd);
        	request.add("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName +"\"" + lineEnd);
        	request.add(lineEnd);
        }
    }
    
    private String getB64Auth(String username, String password) {
        String source=username+":"+password;
        String ret="Basic "+Base64.encodeToString(source.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);
        return ret;
    }
    
	@Override
	protected String doInBackground(String... args) {
		
		try {
			connection = (HttpURLConnection) url.openConnection();
            
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
            
			// Enable POST method
			connection.setRequestMethod(this.httpMethod);
			
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept", "application/xml");
			connection.setRequestProperty("Authorization", getB64Auth(this.taskEmail, this.taskPassword));
			
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
            
			outputStream = new DataOutputStream(connection.getOutputStream());
			
			for (String part : request) {
				outputStream.writeBytes(part);
			}
			
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1*1024*1024;
            
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
            
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            
			// Responses from the server (code and message)
			serverResponseCode = connection.getResponseCode();
			serverResponseMessage = connection.getResponseMessage();
            
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
			
			HttpResponse response = httpClient.execute(httppost);
			StatusLine statusLine = response.getStatusLine();
			if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
				Log.i(TAG, responseString);
			} else{
				//Closes the connection.
				response.getEntity().getContent().close();
				Log.e(TAG, statusLine.getReasonPhrase());
				if(statusLine.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
					Log.e(TAG, "authentication error: un: " + taskEmail + " pw: " + taskPassword);
					syncStatus = SyncStatusType.SYNC_STATUS_AUTH_FAIL;
				} else if(statusLine.getStatusCode() >= 400) {
					Log.e(TAG, "failed with server error = " + statusLine.getReasonPhrase() + " and code = " + statusLine.getStatusCode());
					syncStatus = SyncStatusType.SYNC_STATUS_SERVER_ERR;
				} else {
					Log.e(TAG, "http status = " + statusLine.getReasonPhrase() + " and code = " + statusLine.getStatusCode());
					syncStatus = SyncStatusType.SYNC_STATUS_XMSN_ERR;
				}
				transactionComplete(false);
			}
		} catch (ClientProtocolException e) {
			//TODO Handle problems..
		} catch (IOException e) {
			//TODO Handle problems..
			Log.e(TAG, e.toString());
		}
		return responseString;
	}
	
	public void transactionComplete(boolean transactionSuccess) {
		
	}
    
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		transactionComplete(syncStatus == SyncStatusType.SYNC_STATUS_OK);
		if(result != null)
			try {
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing data: " + e.toString());
			}
        
		try {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void cancel() {
	    abort = true;
	}
    
    
}
