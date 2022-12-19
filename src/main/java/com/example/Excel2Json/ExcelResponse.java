package com.example.Excel2Json;

import java.util.List;
import java.util.UUID;

import org.json.simple.JSONObject;

public class ExcelResponse {
	List<JSONObject> data;
	UUID id;

	public List<JSONObject> getData() {
		return data;
	}

	public void setData(List<JSONObject> data) {
		this.data = data;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

}
