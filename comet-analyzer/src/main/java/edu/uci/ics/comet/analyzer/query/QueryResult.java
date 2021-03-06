package edu.uci.ics.comet.analyzer.query;

public interface QueryResult {

	public String getString(String key);

	public Integer getInteger(String key);

	public Long getLong(String key);

	public Object get(String key);

	boolean containsKey(String key);
}
