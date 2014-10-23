package com.shopwave.java.oauth.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.shopwave.java.oauth.http.AsyncTaskExecutor;
import com.shopwave.java.oauth.http.HttpRequest;
import com.shopwave.java.oauth.http.HttpRequestDelegate;
import com.shopwave.java.oauth.model.Token;
import com.shopwave.java.oauth.model.User;
import com.shopwave.java.oauth.model.UserGet;
import com.shopwave.java.oauth.parser.TokenParser;
import com.shopwave.java.oauth.parser.UserGetParser;

public class ShopwaveConnectManager {
	
    private static AsyncTaskExecutor asyncTaskExecutor;
    
    private static String accessType = "online";
    private static String responseTypeCode = "code";
    private static String OAuthBaseUrl = "http://secure.merchantstack.com";
    private static String ApiBaseUrl = "http://api.merchantstack.com";
    private static String AuthUri = "/oauth/authorize";
    private static String TokenUri = "/oauth/token";
    private static String LogoutUri = "/logout";
    private static String grantTypeAuthoricationCode = "authorization_code";
    private static String grantTypeRefreshToken = "refresh_token";
    

    public ShopwaveConnectManager(){

    	asyncTaskExecutor = new AsyncTaskExecutor();
    }

    public String GetAuthoriseApplicationUri(String redirectUrl,  String clientId, String scope)
    {	
        return generateConnectionUrl(redirectUrl, accessType, responseTypeCode, clientId, scope);
    }

    public Token exchangeCodeForToken(String redirectUrl, String clientId, String clientSecret, String scope, String code, HttpRequestDelegate responseDelegate)
    {
        return getRefreshToken(grantTypeAuthoricationCode, redirectUrl, clientId, clientSecret, scope, code, null, responseDelegate);
    }
    
    public void refreshToken(String redirectUrl, String clientId, String clientSecret, String scope, String refreshToken, HttpRequestDelegate responseDelegate)
    {
    	getRefreshToken(grantTypeRefreshToken, redirectUrl, clientId, clientSecret, scope, null, refreshToken, responseDelegate);
    }
    
    public User getUser(Token token, HashMap<String, String> params, HttpRequestDelegate callback)
    {
    	HashMap<String, String> headers = new HashMap<String, String>();
    	
    	String userJSONObject = this.makeShopwaveApiCall("/user", token, "GET", params, headers, callback);
    	UserGetParser parser = new UserGetParser();
    	UserGet userRootNode=  parser.getParsedData(userJSONObject);
    	
    	if(userRootNode != null)
    	{
    		return userRootNode.getUser();
    		
    	}
    	return null;
    }
    
    public String makeShopwaveApiCall(String endpoint, Token tokens, String method, HashMap<String, String> params, HashMap<String, String> headers, HttpRequestDelegate callback){
    	
    	HashMap<String, String> defaultHeaders = new HashMap<String, String>();
    	defaultHeaders.put("accept", "application/json");
    	
    	if(tokens != null)
    	{
    		defaultHeaders.put("Authorization", "OAuth "+tokens.getAccess_token());
    	}
    	
    	if(headers != null)
    	{
    		for (String key : headers.keySet()) {
    			defaultHeaders.put(key, headers.get(key));
    		}
    	}
    	
    	method = (method == null) ? "GET" : method; 
    	endpoint = getApiEndpoint(endpoint);
    	
    	return makeWebQuery(endpoint, method, params, defaultHeaders, callback);
    }
    
    public String getLogoutUrl(String redirectUrl, String clientId, String scope){
    	String uriParts = OAuthBaseUrl+LogoutUri+"?access_type="+accessType
                +"&redirect_uri="+redirectUrl
                +"&response_type="+responseTypeCode
                +"&client_id="+clientId
                +"&scope="+scope;
    	
    	return uriParts;
    }


    /********* PRIVATE FUNCTIONS *******************/

    private String generateConnectionUrl(String redirectUrl, String accessType, String responseType, String clientId, String scope)
    {

        String uriParts = "?access_type="+ accessType
                +"&redirect_uri="+redirectUrl
                +"&response_type="+responseType
                +"&client_id="+clientId
                +"&scope="+scope;

        return OAuthBaseUrl+AuthUri+uriParts;
    }
    
    private String getApiEndpoint(String endpoint){
    	return ApiBaseUrl+endpoint;
    }
    
    /**
     * The function exchanges code for token / refresh token,
     * @param token
     * @param params
     * @param responseDelegate
     * @return 
     */
    private Token getRefreshToken(String grantType, String redirectUrl,  String clientId, String clientSecret, String scope, String code, String refreshToken, HttpRequestDelegate callback)
    {
        HashMap<String, String> defaultParams = new HashMap<String, String>();
        defaultParams.put("redirect_uri", redirectUrl);
        defaultParams.put("client_id", clientId);
        defaultParams.put("client_secret", clientSecret);
        defaultParams.put("grant_type", grantType);
        
        if(grantType == grantTypeAuthoricationCode){
        	defaultParams.put("code", code);
        }
        else
        {
        	defaultParams.put("refresh_token", refreshToken);
        }
        
        String method = "POST";
        String results = makeWebQuery(OAuthBaseUrl+TokenUri, method, defaultParams, null, null);
        
        TokenParser parser = new TokenParser();
        return parser.getParsedData(results);
    }
    
    /**
     * fetch the data in background if callback provided else return result in main thread.
     * @param requestUrl
     * @param method
     * @param params
     * @param callback
     * @return 
     */
    private String makeWebQuery(String requestUrl, String method, HashMap<String, String> params, HashMap<String, String> headers, HttpRequestDelegate callback){
    	
    	if(callback != null){
        	makeWebQuery(requestUrl, method, params, headers, callback);
        }
        else
        {
        	HttpRequest httpRequest = new HttpRequest();
        	try 
    		{
        		if(method == "GET")
        		{
        			return httpRequest.getDataFromWeb(new URL(requestUrl), headers);
        		}
        		else
        		{
        			String results = httpRequest.post(new URL(requestUrl), params, headers);
        			return results;
        		}
    		} 
    		catch (MalformedURLException e) 
    		{
    			e.printStackTrace();
    		}
        	
        }
    	
    	asyncTaskExecutor.requestWebContents(requestUrl, method, params, headers, callback);
    	return null;
    }
}
