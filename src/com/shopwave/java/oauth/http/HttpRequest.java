package com.shopwave.java.oauth.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Simply makes httpGet and POST request
 * Requires delgate to be setup using setDelegate method to handle response.
 * params required (Url to make request to)
 * To enable file cache just call setCacheDir with File(directoryPath) params
 * use getcacheDir to get the cache directory pathName.
 * @author yasir.mahmood
 *
 */
public class HttpRequest {
	
	private HttpRequestDelegate delegate;
	private File directoryName;
	
	public void setDelegate(HttpRequestDelegate delegate){
		this.delegate = delegate;
	}
	
	public void setCacheDir(File directoryName){
		this.directoryName = directoryName;
	}
	
	/**
	 * When no delegate set , return response as inputstream
	 * @param url
	 * @return
	 */
	public InputStream getInputStream(URL url, HashMap<String, String> headers)
	{
		InputStream in = null;
		try
		{
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			
			if(headers != null)
			{
				for(String key: headers.keySet()){
					urlConnection.setRequestProperty(key, headers.get(key));
				}
			}
			
			try {
				in = new BufferedInputStream(urlConnection.getInputStream());
				return in;
			} finally {
				//urlConnection.disconnect();
			}	
			
		}
		catch(Exception e)
		{
			
		}
		
		return in;
	}
	
	/**
	 * get data from web
	 * @param url
	 * @param headers
	 * @return String object
	 */
	public String getDataFromWeb(URL url, HashMap<String, String> headers){
		InputStream stream = get(url, headers);
		if(stream != null){
			return readStream(stream);
		}
		return null;
	}

    public InputStream get(URL url, HashMap<String, String> headers) {
		//enableHttpResponseCache();
		String result = null;
		InputStream in = null;
		HttpURLConnection urlConnection = null;
		if(delegate!=null){
			try{
				urlConnection = (HttpURLConnection) url.openConnection();
				
				if(headers != null)
				{
					for(String key: headers.keySet()){
						urlConnection.setRequestProperty(key, headers.get(key));
					}
				}
				
				try 
				{
					in = new BufferedInputStream(urlConnection.getInputStream());
					result = readStream(in);
				} finally {
					urlConnection.disconnect();
					delegate.onCompleteHttpRequest(result);
				}
				
			}catch(Exception e){
				try {
					com.shopwave.java.oauth.model.Error error = new com.shopwave.java.oauth.model.Error();
					error.setStatusCode(urlConnection.getResponseCode());
					delegate.onFailHttpRequest(error);
				} catch (IOException e1) {
					delegate.onFailHttpRequest(null);
				}
			}
		}
		else
		{
			return getInputStream(url, headers);
		}
		
		return null;
	}

	public String post(URL url, HashMap<String, String> params, HashMap<String, String> headers) {
		enableHttpResponseCache();
		String result = null;
		
		try{
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			
			if(headers != null)
			{
				for(String key: headers.keySet()){
					urlConnection.setRequestProperty(key, headers.get(key));
				}
			}
			
			try {
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod("POST");
				urlConnection.setChunkedStreamingMode(0);
	
				DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
				
				if(params!=null){
					writeStream(out, getParseUrlEncodeParams(params));
				}
	
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				result = readStream(in);
			}
			catch(Exception e){
				System.out.println(e.toString());
			}
			
			finally {
				urlConnection.disconnect();
				if(delegate != null){
					delegate.onCompleteHttpRequest(result);
				}
			}
		}catch(Exception e){
			System.out.println(e.toString());
			if(delegate != null){
				delegate.onFailHttpRequest(null);
			}
		}
		return result;
	}
	
	private String readStream(InputStream in) {
		
		StringBuffer result = new StringBuffer();
		
		try{
			BufferedReader reader = null;
			try {
	
				reader = new BufferedReader(new InputStreamReader(in));
	
				String line = "";
	
				while ((line = reader.readLine()) != null) {
					result.append(line);
					if(delegate != null){
						delegate.onProgressHttpRequest(0);
					}
				}
	
			} catch (IOException e) {
				System.out.println(e.toString());
			}
			
			finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}catch(Exception exception){
			
		}
		if(result!=null){
			//delegate.onCompleteHttpRequest(result.toString());
			return result.toString();
		}else{
			return "";
		}
		
	}
	
	private void writeStream(DataOutputStream output, String contents) {
		try {
			output.writeBytes(contents);
			output.flush();
			output.close();
		} catch (IOException ex) {

		}
	}
	
	private String getParseUrlEncodeParams(HashMap<String, String> params){
		StringBuilder postParams = new StringBuilder();
		String key, value, result = "";
		
		for (Map.Entry<String, String> entry : params.entrySet()) {
		    key = entry.getKey();
		    value =  URLEncoder.encode((String) entry.getValue());
		    postParams.append(key+"="+value+"&");
		}
		result = postParams.toString();
		result = result.substring(0, result.length() - 1);
		return result;
	}
	
	private void enableHttpResponseCache() {
		if(this.directoryName!= null){
		    try {
		        long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
		        File httpCacheDir = new File(this.directoryName, "http");
		        Class.forName("android.net.http.HttpResponseCache")
		            .getMethod("install", File.class, long.class)
		            .invoke(null, httpCacheDir, httpCacheSize);
		    } catch (Exception httpResponseCacheNotAvailable) {
		    }
		}
	}
}
