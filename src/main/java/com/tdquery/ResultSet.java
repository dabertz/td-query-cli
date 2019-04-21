package com.tdquery;

import java.util.List;

import org.json.JSONArray;
import org.msgpack.value.ArrayValue;

public class ResultSet {
	private JSONArray schema;
	private List<Object[]> items;

	public ResultSet(JSONArray schema, List<Object[]> items) {
		this.schema = schema;
		this.items = items;
	}

	public JSONArray getSchema() {
		return schema;
	}

	public void setSchema(JSONArray schema) {
		this.schema = schema;
	}

	public List<Object[]> getItems() {
		return items;
	}

	public void setItems(List<Object[]> items) {
		this.items = items;
	}
	
}
