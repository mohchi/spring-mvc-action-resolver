package com.mohchi.example.web.framework;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * A wrapper around Spring's {@link UriComponentsBuilder}
 * that can be used from both controllers and JSPs to build
 * URIs for actions that are local to the web application.
 * 
 * @author andy
 */
public class ActionUriBuilder {

	private final UriComponentsBuilder builder;
	private final ActionInfo action;
	private final String contextPath;
	private List<String> pathParams;

	private transient boolean buildPath = true;

	public static interface PortResolver {

		int getSecurePort(HttpServletRequest request);

		int getUnsecurePort(HttpServletRequest request);

	}

	private static PortResolver portResolver = new PortResolver() {

		@Override
		public int getSecurePort(HttpServletRequest request) {
			if (request.isSecure()) {
				return request.getServerPort();
			}
			switch (request.getServerPort()) {
				case 8080:
					return 8443;
				default:
					return 443;
			}
		}

		@Override
		public int getUnsecurePort(HttpServletRequest request) {
			if (request.isSecure()) {
				switch (request.getServerPort()) {
					case 8443:
						return 8080;
					default:
						return 80;
				}
			}
			return request.getServerPort();
		}

	};

	/**
	 * Sets the port resolver to be used by {@code ActionUriBuilder} to
	 * determine the server's secure and unsecure ports for a given request.
	 * The default port resolver maps between 80 <-> 443 and 8080 <-> 8443.
	 * @param portResolver
	 */
	public static void setPortResolver(PortResolver portResolver) {
		ActionUriBuilder.portResolver = portResolver;
	}

	public static ActionUriBuilder secureActionUri(String action) {
		return secureActionUri(action, false);
	}

	public static ActionUriBuilder unsecureActionUri(String action) {
		return unsecureActionUri(action, false);
	}

	public static ActionUriBuilder actionUri(String action) {
		return actionUri(action, false);
	}

	public static ActionUriBuilder secureActionUri(String action, boolean absoluteUri) {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		HttpServletRequest request = ActionResolver.getCurrentRequest();
		if (!request.isSecure() || absoluteUri) {
			// build an absolute url to switch from http to https
			int port = portResolver.getSecurePort(request);
			if (port == 443) {
				port = -1;
			}
			builder.port(port).scheme("https").host(request.getServerName());
		}
		return new ActionUriBuilder(getRequiredAction(action), builder, request.getContextPath());
	}

	public static ActionUriBuilder unsecureActionUri(String action, boolean absoluteUri) {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		HttpServletRequest request = ActionResolver.getCurrentRequest();
		if (request.isSecure() || absoluteUri) {
			// build an absolute url to switch from https to http
			int port = portResolver.getUnsecurePort(request);
			if (port == 80) {
				port = -1;
			}
			builder.port(port).scheme("http").host(request.getServerName());
		}
		return new ActionUriBuilder(getRequiredAction(action), builder, request.getContextPath());
	}

	public static ActionUriBuilder actionUri(String action, boolean absoluteUri) {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		HttpServletRequest request = ActionResolver.getCurrentRequest();
		if (absoluteUri) {
			builder.port(request.getServerPort()).scheme(request.getScheme())
					.host(request.getServerName());
		}
		return new ActionUriBuilder(getRequiredAction(action), builder, request.getContextPath());
	}

	private static ActionInfo getRequiredAction(String name) {
		ActionInfo action = ActionResolver.getInstance().resolve(name);
		if (action == null) {
			throw new IllegalArgumentException("No action is mapped to name '" + name + "'");
		}
		return action;
	}

	/**
	 * Creates an {@code ActionUriBuilder} which will not include the context path.
	 * This method is typically used by Spring MVC controllers.
	 * @param action
	 * @return
	 */
	public static ActionUriBuilder internalActionUri(String action) {
		return new ActionUriBuilder(ActionResolver.getInstance().resolve(action),
				UriComponentsBuilder.newInstance(), null);
	}

	protected ActionUriBuilder(ActionInfo action, UriComponentsBuilder builder,
			String contextPath) {
		this.action = action;
		this.builder = builder;
		this.contextPath = contextPath;
	}

	public ActionUriBuilder pathParam(Object value) {
		buildPath = true; // path needs to be rebuilt
		if (pathParams == null) {
			pathParams = new LinkedList<String>();
		}
		pathParams.add(value.toString());
		return this;
	}

	public ActionUriBuilder pathParams(Object... values) {
		buildPath = true; // path needs to be rebuilt
		if (pathParams == null) {
			pathParams = new LinkedList<String>();
		}
		for (Object value : values) {
			pathParams.add(value.toString());
		}
		return this;
	}
	
	public ActionUriBuilder queryParam(String param, Object value) {
		builder.queryParam(param, value);
		return this;
	}

	public ActionUriBuilder queryParams(String param, Object... values) {
		builder.queryParam(param, values);
		return this;
	}

	public ActionUriBuilder replaceQueryParam(String param, Object value) {
		if (value != null) {
			builder.replaceQueryParam(param, value);
		} else {
			builder.replaceQueryParam(param);
		}
		return this;
	}

	public ActionUriBuilder replaceQueryParams(String param, Object... values) {
		builder.replaceQueryParam(param, values);
		return this;
	}

	public ActionUriBuilder query(String query) {
		builder.query(query);
		return this;
	}

	public ActionUriBuilder replaceQuery(String query) {
		builder.replaceQuery(query);
		return this;
	}

	private void preparePath() {
		if (buildPath) {
			buildPath = false;
			String[] args = null;
			if (pathParams != null && pathParams.size() > 0) {
				args = pathParams.toArray(new String[0]);
			}
			String path = action.getPath(args);
			builder.replacePath(contextPath).path(path);
		}
	}

	public String getUri() {
		return getUri(false);
	}

	public String getUri(boolean encode) {
		preparePath();
		if (encode) {
			return builder.build().encode().toUriString();
		} else {
			return builder.build().toUriString();
		}
	}

	public UriComponentsBuilder getBuilder() {
		preparePath();
		return builder;
	}

	@Override
	public String toString() {
		return getUri(false);
	}

}
