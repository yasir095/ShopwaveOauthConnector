package com.shopwave.java.oauth.http;

public interface HttpRequestDelegate {
	public void onCompleteHttpRequest(String result);
	public void onFailHttpRequest(com.shopwave.java.oauth.model.Error e);
	public void onProgressHttpRequest(int progress);
}
