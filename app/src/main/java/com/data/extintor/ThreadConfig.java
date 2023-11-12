package com.data.extintor;

import jakarta.validation.constraints.NotNull;

public class ThreadConfig {

	@NotNull(message = "required")
	private String name;

	@NotNull(message = "required")
	private String whereFilter;

	@NotNull(message = "required")
	private String tableName;

	private Integer limit = 1000;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWhereFilter() {
		return whereFilter;
	}

	public void setWhereFilter(String whereFilter) {
		this.whereFilter = whereFilter;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
}
