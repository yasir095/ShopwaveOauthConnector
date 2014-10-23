package com.shopwave.java.oauth;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shopwave.java.oauth.core.ShopwaveConnectManager;
import com.shopwave.java.oauth.model.Token;
import com.shopwave.java.oauth.model.User;

@SuppressWarnings("serial")
public class ShopwaveOauthConnectorServlet extends HttpServlet {
	
	private RequestDispatcher requestDispatcher;
	private ShopwaveConnectManager connector;
	private Token token;
	
	@Override
	public void init(ServletConfig config) throws ServletException 
	{
		ServletContext context = config.getServletContext();
		requestDispatcher = context.getRequestDispatcher("/WEB-INF/jsp/home.jsp");
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String authCode = req.getParameter("code");
		
		boolean logout = Boolean.parseBoolean(req.getParameter("logout"));
		
		if(logout == true)
		{
			this.token = null;
			String uriParts = connector.getLogoutUrl(Config.redirectUrl, Config.ClientIdentifier, Config.Scope);
			resp.sendRedirect(uriParts);
		}
		else
		{
		
			if(authCode != null && authCode.length()>0)
			{
				this.token = this.getDefaultConnector().exchangeCodeForToken(Config.redirectUrl, Config.ClientIdentifier, Config.ClientSecret, Config.Scope, authCode, null);
				
			}
			
			if(this.token != null)
			{
				//this.token.setAuthCode(authCode);
				
				User user = this.getDefaultConnector().getUser(this.token, null, null);
				if(user != null)
				{
					req.setAttribute("firstName", user.getFirstName());
					req.setAttribute("lastName", user.getLastName());
					req.setAttribute("merchantId", user.getEmployee().getMerchantId());
				}
			}
	
			req.setAttribute("isLoggedIn", (this.token == null) ? false : true);
			req.setAttribute("connectionUrl", this.getConnectionString());
			
	        try 
	        {
				requestDispatcher.forward(req, resp);
			} 
	        catch (ServletException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getConnectionString(){
		return this.getDefaultConnector().GetAuthoriseApplicationUri(Config.redirectUrl, Config.ClientIdentifier, Config.Scope);
	}
	
	public ShopwaveConnectManager getDefaultConnector(){
		if(this.connector == null){
			this.connector = new ShopwaveConnectManager();
		}
		
		return this.connector;
	}
}
