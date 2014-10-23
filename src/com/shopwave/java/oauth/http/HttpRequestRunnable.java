package com.shopwave.java.oauth.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class HttpRequestRunnable implements Runnable {
	
	private URL url;
	private HttpRequest httpRequest;
	private String requestType;
	private HashMap<String, String> httpPostParams;
	private HashMap<String, String> headers;
	
 
	public HttpRequestRunnable(String urlString, String requestType, HttpRequestDelegate delegate){
		
		try 
		{
			this.url = new URL(urlString);
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
		
		this.requestType = requestType;
		httpRequest = new HttpRequest();
		httpRequest.setDelegate(delegate);
	}
	
	public void setPostParams(HashMap<String, String> httpPostParams){
		this.httpPostParams = httpPostParams;
	}
	
	public void setHeaders(HashMap<String, String> headers){
		this.headers = headers;
	}

	@Override
	public void run() {
		if(requestType != null && requestType == "POST")
		{
			httpRequest.post(this.url, this.httpPostParams, this.headers);
		}
		else
		{
			httpRequest.get(this.url, this.headers);
		}	
	}
}