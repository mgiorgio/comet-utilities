package edu.uci.ics.como.analyzer.query;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EventQuery {

	private List<QueryMember> queryMembers;

	public EventQuery() {
		queryMembers = new LinkedList<EventQuery.QueryMember>();
	}

	/**
	 * Creates an {@link EventQuery}, adding initial query members. EQ operation
	 * is assumed.
	 * 
	 * @param fields
	 *            Keys and values of the initial query members.
	 */
	public EventQuery(Map<String, Object> fields) {
		for (Entry<String, Object> field : fields.entrySet()) {
			addQueryMember(field.getKey(), field.getValue(), QueryOperation.EQ);
		}
	}

	/**
	 * Adds a {@link QueryMember} to this {@link EventQuery}.
	 * 
	 * @param key
	 * @param value
	 * @param operation
	 */
	public void addQueryMember(String key, Object value, QueryOperation operation) {
		queryMembers.add(new QueryMember(key, value, operation));
	}

	/**
	 * @return The first {@link QueryMember}. If there are no query members,
	 *         <code>null</code>.
	 */
	public QueryMember first() {
		if (queryMembers.isEmpty()) {
			return null;
		}
		return queryMembers.get(0);
	}

	/**
	 * @return The number of {@link QueryMember}s.
	 */
	public int size() {
		return queryMembers.size();
	}

	/**
	 * @return <code>true</code> if there are no {@link QueryMember}s.
	 *         Otherwise, <code>false</code>.
	 */
	public boolean isEmpty() {
		return this.size() == 0;
	}

	/**
	 * @return An immutable list of {@link QueryMember}s.
	 */
	public List<QueryMember> getQueryMembers() {
		return Collections.unmodifiableList(queryMembers);
	}

	public static class QueryMember {
		private String key;

		private Object value;

		private QueryOperation operation;

		public QueryMember(String key, Object value, QueryOperation operation) {
			this.key = key;
			this.value = value;
			this.operation = operation;
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public QueryOperation getOperation() {
			return operation;
		}
	}

	public enum QueryOperation {
		EQ, NE, GT, GE, LT, LE;
	}
}