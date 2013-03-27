package com.mohchi.example.web.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

public class ActionResolver implements InitializingBean, ServletContextAware {

	private static final Logger logger = LoggerFactory.getLogger(ActionResolver.class);

	public static final String CONTEXT_ATTR = ActionResolver.class.getName() + ".instance";

	@Resource
	private RequestMappingInfoHandlerMapping handlerMapping;

	private Map<String, ActionInfo> actionMap = new HashMap<String, ActionInfo>();

	public void setServletContext(ServletContext servletContext) {
		servletContext.setAttribute(CONTEXT_ATTR, this);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = handlerMapping.getHandlerMethods();
		for (Entry<RequestMappingInfo, HandlerMethod> handlerMethodEntry : handlerMethodMap.entrySet()) {
			ActionInfo action = new ActionInfo(handlerMethodEntry.getValue().getMethod(),
					handlerMethodEntry.getKey(), handlerMapping.getPathMatcher());
			ActionInfo oldAction = actionMap.put(action.getFullyQualifiedName(), action);
			if (oldAction != null) {
				throw new IllegalArgumentException("More than one mapping found for method: "
						+ oldAction.getFullyQualifiedName());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Assigned request mapping '" + action.getPattern()
						+ "' to name '" + action.getFullyQualifiedName() + "'");
			}
		}
		for (ActionInfo action : new ArrayList<ActionInfo>(actionMap.values())) {
			if (action.getName() != null) {
				ActionInfo oldAction = actionMap.put(action.getName(), action);
				if (oldAction != null) {
					throw new IllegalArgumentException("Action name '" + action.getName()
							+ "' matches an existing action name");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Assigned request mapping '" + action.getPattern()
							+ "' to name '" + action.getName() + "'");
				}
			}
		}
	}

	public ActionInfo resolve(String name) {
		return actionMap.get(name);
	}

	public static ActionResolver getInstance() {
		ServletContext servletContext = getCurrentRequest().getServletContext();
        ActionResolver router = (ActionResolver) servletContext.getAttribute(CONTEXT_ATTR);
		if (router != null) {
			return router;
		}
		throw new IllegalStateException("No RequestMappingRouter associated with " +
				"the current servlet context");
	}

	public ActionUriBuilder uri(String name) {
		return ActionUriBuilder.actionUri(name);
	}

	public ActionUriBuilder secureUri(String name) {
		return ActionUriBuilder.secureActionUri(name);
	}

	public ActionUriBuilder unsecureUri(String name) {
		return ActionUriBuilder.unsecureActionUri(name);
	}

	/**
	 * Gets the current thread-bound request from Spring's {@link RequestContextHolder}.
	 * @return
	 */
	protected static HttpServletRequest getCurrentRequest() {
		// code borrowed from ServletUriComponentsBuilder
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
		Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
		HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
		return servletRequest;
	}

}
