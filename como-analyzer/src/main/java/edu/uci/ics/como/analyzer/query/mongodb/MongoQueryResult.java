package edu.uci.ics.como.analyzer.query.mongodb;

import org.bson.Document;

import edu.uci.ics.como.analyzer.query.QueryResult;

public class MongoQueryResult implements QueryResult {

	private Document document;

	public MongoQueryResult(Document document) {
		this.document = document;
	}

	@Override
	public String getString(String key) {
		return document.getString(key);
	}

	@Override
	public Integer getInteger(String key) {
		return document.getInteger(key);
	}

	@Override
	public Long getLong(String key) {
		return document.getLong(key);
	}

}