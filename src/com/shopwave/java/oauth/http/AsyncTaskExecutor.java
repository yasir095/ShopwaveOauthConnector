package com.shopwave.java.oauth.http;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Function make async Http Request 
 * Should be called as a single instance 
 * accepts a Runnable task or just call requestWebContents with url to make an async Request.
 * Response will be thrown in a callback function passed as delegate.
 * @author yasir.mahmood
 *
 */
public class AsyncTaskExecutor {
	
	private ExecutorService taskExecutor;
	private HttpRequestRunnable runnable;
	
	public void addTask(Runnable task){
		taskExecutor.execute(task);
	}
	/**
	 * Provide delegate to get the response in that function
	 * @param urlString
	 * @param requestType
	 * @param params
	 */
	public void requestWebContents(String urlString, String requestType, HashMap<String, String> params, HashMap<String, String> headers, HttpRequestDelegate responseDelegate){
		runnable = new HttpRequestRunnable(urlString, requestType, responseDelegate);
		
		if(params != null){
			runnable.setPostParams(params);
		}
		if(headers != null){
			runnable.setHeaders(headers);
		}
		
		taskExecutor.execute(runnable);
	}
	
	public AsyncTaskExecutor(){
		taskExecutor = Executors.newCachedThreadPool();
	}
	
}
