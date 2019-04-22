package com.tdquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * QueryBuilder construct the SELECT statement based on inputs. 
 */
public class QueryBuilder {
	
	private String columns;
	private String tablename;
	private int limit;
	private long minTime;
	private long maxTime;
	
	public QueryBuilder() {}
	
	public QueryBuilder(String columns, String tablename, long minTime, long maxTime, int limit) {
		this.columns = columns;
		this.tablename = tablename;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.limit = limit;
	} 

	public void setColumns(String columns) {
		this.columns = columns;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}
	
	public void setMinTime(long minTime) {
		this.minTime = minTime;
	}
	
	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String[] getColumnNames() {
		if(this.columns == null || this.columns.trim().isEmpty()) {
			return new String[0];
		}
		return this.columns.split("\\s*,\\s*");
	}
	
	public String getTablename() {
		return this.tablename;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		String[] columnNames = this.getColumnNames();
		String columns = (columnNames.length > 0) ? String.join(",", columnNames) : "*";
		sb.append(String.format("SELECT %s FROM %s", columns, this.tablename));

		List<String> conditions = new ArrayList<String>();				

		if ((this.minTime) > 0 && (this.maxTime) > 0) {
			String range = String.format("TD_TIME_RANGE(time, %d, %d)", this.minTime, this.maxTime);
			conditions.add(range);
		} else {
			if (this.minTime > 0) {
				conditions.add(String.format("time > %d", this.minTime));
			} else if (this.maxTime > 0) {
				conditions.add(String.format("time < %d", this.maxTime));
			}
		}

		if(conditions.size() > 0) {
			String where = String.join(" AND ", conditions);
			sb.append(String.format(" WHERE %s", where));
		}
		
		if (this.limit > 0) {
			sb.append(String.format(" LIMIT %d", this.limit));
		}

		return sb.toString();
	}
}
