package com.shopwave.java.oauth.parser;

import com.shopwave.java.oauth.model.UserGet;

public class UserGetParser extends Parser<UserGet> {

	@Override
	public UserGet getParsedData(String jsonData) {
		try
        {
            return mapper.readValue(jsonData, UserGet.class);
        }
        catch(Exception e)
        {
            return null;
        }
	}
}
