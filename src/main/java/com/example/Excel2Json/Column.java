package com.example.Excel2Json;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "localColName", "remoteColName" })
@Generated("jsonschema2pojo")
public class Column {

	@JsonProperty("localColName")
	private String localColName;
	@JsonProperty("remoteColName")
	private String remoteColName;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("localColName")
	public String getLocalColName() {
		return localColName;
	}

	@JsonProperty("localColName")
	public void setLocalColName(String localColName) {
		this.localColName = localColName;
	}

	@JsonProperty("remoteColName")
	public String getRemoteColName() {
		return remoteColName;
	}

	@JsonProperty("remoteColName")
	public void setRemoteColName(String remoteColName) {
		this.remoteColName = remoteColName;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public String toString() {
		return "Column [localColName=" + localColName + ", remoteColName=" + remoteColName + ", additionalProperties="
				+ additionalProperties + "]";
	}
	
	

}