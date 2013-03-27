package com.mohchi.example.web.framework;

import javax.servlet.http.HttpServletRequest;

import com.mohchi.example.web.framework.ActionUriBuilder.PortResolver;

public class BasicPortResolver implements PortResolver {

	private final int httpPort;
	private final int httpsPort;

	public BasicPortResolver(int httpPort, int httpsPort) {
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
	}

	@Override
	public int getSecurePort(HttpServletRequest request) {
		return httpsPort;
	}

	@Override
	public int getUnsecurePort(HttpServletRequest request) {
		return httpPort;
	}

}
