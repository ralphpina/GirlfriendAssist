package com.outridersw.tapinspectandroid;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.params.ClientPNames;
import ch.boye.httpclientandroidlib.client.params.CookiePolicy;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class MainActivity extends Activity {
	
	//for logging to console
	private static final String TAG = "MainActivity";
	
	TextView textView;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.textView);		
		textView.setText("Hey, TapInspect Android");	

		AsyncTask<String, String, String> task = new AsyncTask<String, String, String>(){

			private String getB64Auth(String username, String password) {
		        String source= username + ":" + password;
		        String ret= Base64.encodeToString(source.getBytes(),Base64.DEFAULT);
		        return ret;
		    }
			
			@Override
			protected String doInBackground(String... arg0) {
				// TODO Auto-generated method stub			
				
				String request = "user[email]=tester_dc4895b37241a2a502_01_2013_14_46_48@tapinspect.com&user[password]=testtest&user[password_confirmation]=testtest";
				
				//MultipartEntity reqEntity = new MultipartEntity();
				
				//reqEntity.addPart("Content-length", StringBody.create(Integer.valueOf(request.length()).toString(), "text/xml", Charset.forName("UTF-8")));
	    		//reqEntity.addPart("", StringBody.create(request, "text/xml", Charset.forName("UTF-8")));
	    		
	    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	    	    nameValuePairs.add(new BasicNameValuePair("Content-length", Integer.valueOf(request.length()).toString()));
				
	    	    HttpClient httpclient = new DefaultHttpClient();
	    	    
				try {
					
					httpclient.getParams().setParameter(
					        ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
					
					HttpPost httppost = new HttpPost("https://dev.tapinspect.com/sync_new_user?device[uuid]=dc4895b37241a2a5&device[sw_version]=1&device[os_version]=4.1.1&device[description]=Galaxy%20Nexus&device[platform]=android");
					httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
					httppost.setHeader("Accept", "application/xml");
					httppost.setHeader("Authorization", "Basic " + getB64Auth("tester@tapinspect.com", "monkey"));
					//httppost.setHeader("Content-length", Integer.valueOf(request.length()).toString());
					
					MyStringEntity se = new MyStringEntity(request, "utf-8");
					se.setContentType("application/xml");
					
					httppost.setEntity(se);
					
					HttpResponse response = httpclient.execute(httppost);
					
					String serverResponseMessage = response.toString();
					
					// Responses from the server (code and message)
					Integer serverResponseCode = response.getStatusLine().getStatusCode();
		
					Log.i(TAG, "serverResponseCode = " + serverResponseCode);
					Log.i(TAG, "serverResponseMessage = " + serverResponseMessage);

					String res = EntityUtils.toString(response.getEntity());
					Log.e(TAG, "res = " + res);
					httpclient.getConnectionManager().shutdown();
					
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				
				httpclient.getConnectionManager().shutdown();
				
				return null;
			}
		};
		
		task.execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private class MyStringEntity extends StringEntity {

		public MyStringEntity(String string)
				throws UnsupportedEncodingException {
			super(string);
			// TODO Auto-generated constructor stub
		}
		
		public MyStringEntity(String string, String charset)
				throws UnsupportedEncodingException {
			super(string, charset);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public boolean isStreaming() {
			return true;
		}
		
	}
}
