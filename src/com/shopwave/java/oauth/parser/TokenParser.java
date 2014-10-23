package com.shopwave.java.oauth.parser;

import com.shopwave.java.oauth.model.Token;

public class TokenParser extends Parser<Token> {

	@Override
	public Token getParsedData(String jsonTokenData) {
		try
        {
            return mapper.readValue(jsonTokenData, Token.class);
        }
        catch(Exception e)
        {
            return null;
        }
	}

}
