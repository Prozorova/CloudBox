package com.cloud.utils.jsonQueries;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cloud.utils.JSON;

public class StandardJsonQuery implements JSON {
	
	public enum QueryType {AUTH_DATA};
	
	private String queryType;
	private Map<String, String> parameters;
	
	public StandardJsonQuery() {
		parameters = new LinkedHashMap<>();
	}
	
	public StandardJsonQuery(QueryType queryType, Map<String, String> parameters) {
		this.queryType = queryType.toString();
		this.parameters = parameters;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	

}
