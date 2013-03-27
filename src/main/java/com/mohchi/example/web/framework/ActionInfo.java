package com.mohchi.example.web.framework;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class ActionInfo {

	private static final Pattern ANT_VARIABLE_PATTERN = Pattern.compile("(?:\\{[^/]+?\\}(?=/|$))|(?:\\*\\*)|(?:\\*)|(?:\\?)");

	private final String fullyQualifiedName;
	private final String name;
	private final RequestMethod formMethod;
	private final String pattern;
	private final boolean hasVariables;
	private final PathMatcher pathMatcher;

	public ActionInfo(Method method, RequestMappingInfo requestMappingInfo, PathMatcher pathMatcher) {
		fullyQualifiedName = method.getDeclaringClass().getName() + "#" + method.getName();

		Action action = method.getAnnotation(Action.class);
		if (action != null) {
			if (!action.value().isEmpty()) {
				name = action.value(); 
			} else {
				name = method.getName();
			}
		} else {
			name = null;
		}

		// set the form method (preferring POST)
		if (requestMappingInfo.getMethodsCondition().getMethods().isEmpty()
				|| requestMappingInfo.getMethodsCondition().getMethods().contains(RequestMethod.POST)) {
			formMethod = RequestMethod.POST;
		} else if (requestMappingInfo.getMethodsCondition().getMethods().contains(RequestMethod.GET)) {
			formMethod = RequestMethod.GET;
		} else {
			formMethod = null;
		}

		Set<String> patterns = requestMappingInfo.getPatternsCondition().getPatterns();
		if (patterns.isEmpty()) {
			pattern = "/";
			hasVariables = false;
		} else {
			String tempPattern = patterns.iterator().next();
			if (!tempPattern.startsWith("/")) {
				tempPattern = "/" + tempPattern;
			}
			pattern = tempPattern;
			hasVariables = ANT_VARIABLE_PATTERN.matcher(pattern).find();
		}

		if (!(pathMatcher instanceof AntPathMatcher)) {
			throw new IllegalArgumentException("Path matcher must be an AntPathMatcher");
		}
		
		this.pathMatcher = pathMatcher;
	}

	public String getName() {
		return name;
	}

	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	public RequestMethod getFormMethod() {
		return formMethod;
	}

	public String getPattern() {
		return pattern;
	}

	public String getPath(String... args) {
		if (!hasVariables) {
			return pattern;
		}

		StringBuilder sb = new StringBuilder();
		int end = 0;
		int i = 0;
		Matcher m = ANT_VARIABLE_PATTERN.matcher(pattern);
		while (m.find()) {
			sb.append(pattern.substring(end, m.start()));
			end = m.end();
			if (args != null && i < args.length && args[i] != null) {
				sb.append(args[i++]);
			} else {
				if (!m.group().startsWith("*")) {
					throw new IllegalArgumentException("No value was given for " +
							"parameter " + i + " in pattern '" + pattern + "'");
				}
				i++;
			}
		}
		sb.append(pattern.substring(end));
		
		String path = sb.toString();
		if (!pathMatcher.match(pattern, path)) {
			throw new IllegalArgumentException("Invalid path was generated with pattern '"
					+ pattern + "' when using given parameters: " + Arrays.toString(args));
		}
		return path;
	}

}
