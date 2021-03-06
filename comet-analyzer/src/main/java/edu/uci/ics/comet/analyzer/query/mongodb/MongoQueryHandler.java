package edu.uci.ics.comet.analyzer.query.mongodb;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.ne;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

import edu.uci.ics.comet.analyzer.query.EventQuery;
import edu.uci.ics.comet.analyzer.query.EventQuery.QueryMember;
import edu.uci.ics.comet.analyzer.query.EventQuery.QueryOperation;
import edu.uci.ics.comet.analyzer.query.QueryHandler;
import edu.uci.ics.comet.analyzer.query.QueryResult;

public class MongoQueryHandler implements QueryHandler {

	private Map<String, Object> properties;
	private MongoDatabase database;
	private MongoClient client;
	private MongoCollection<Document> collection;

	public MongoQueryHandler(Map<String, Object> properties) {
		this.properties = properties;
	}

	public static enum MongoProperties {
		MONGO_HOST("mongo-host"), MONGO_PORT("mongo-port"), MONGO_DB("mongo-db"), MONGO_COLLECTION("mongo-collection");

		private String propertyName;

		private MongoProperties(String propertyName) {
			this.propertyName = propertyName;
		}

		public String getPropertyName() {
			return this.propertyName;
		}
	}

	public void init() {
		client = new MongoClient((String) properties.get(MongoProperties.MONGO_HOST.getPropertyName()), Integer.parseInt((String) properties.get(MongoProperties.MONGO_PORT.getPropertyName())));

		database = client.getDatabase((String) properties.get(MongoProperties.MONGO_DB.getPropertyName()));

		collection = database.getCollection((String) properties.get(MongoProperties.MONGO_COLLECTION.getPropertyName()));
	}

	public void shutdown() {
		if (client != null) {
			client.close();
		}
	}

	@Override
	public List<QueryResult> list(EventQuery query) {
		FindIterable<Document> iterable = collection.find(toBsonFilter(query));

		List<QueryResult> list = new LinkedList<>();

		for (Document document : iterable) {
			list.add(new MongoQueryResult(document));
		}

		return list;
	}

	@Override
	public Iterator<QueryResult> iterator(EventQuery query) {
		return new MongoResultsIterator(collection.find(toBsonFilter(query)));
	}

	@Override
	public Iterator<QueryResult> iteratorByTime(EventQuery query, long time, TimeUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	public long count(EventQuery query) {
		return collection.count(toBsonFilter(query));
	}

	@Override
	public QueryResult findOne(EventQuery query) {
		FindIterable<Document> iterable = collection.find(toBsonFilter(query));

		Document document = iterable.first();

		if (document != null) {
			return new MongoQueryResult(document);
		} else {
			return null;
		}
	}

	/**
	 * Converts a {@link QueryMember} to a {@link Bson} filter. If
	 * {@link QueryOperation} in the query member is <code>null</code>, EQ is
	 * assumed.
	 * 
	 * @param queryMember
	 *            The member to convert to {@link Bson} filter.
	 * @return A {@link Bson} filter.
	 */
	private Bson toBsonFilter(QueryMember queryMember) {
		switch (queryMember.getOperation()) {
		case NE:
			return ne(queryMember.getKey(), queryMember.getValue());
		case GT:
			return gt(queryMember.getKey(), queryMember.getValue());
		case GE:
			return gte(queryMember.getKey(), queryMember.getValue());
		case LT:
			return lt(queryMember.getKey(), queryMember.getValue());
		case LE:
			return lte(queryMember.getKey(), queryMember.getValue());
		case EQ:
		default:
			return eq(queryMember.getKey(), queryMember.getValue());
		}
	}

	/**
	 * Creates a {@link Bson} object representing a given {@link EventQuery}.
	 * The assumed operation for all the underlying {@link QueryMember}s is a
	 * bson and operation.
	 * 
	 * @param query
	 *            The query to convert to {@link Bson}.
	 * @return a {@link Bson} object.
	 */
	private Bson toBsonFilter(EventQuery query) {
		if (query.isEmpty()) {
			return new BsonDocument();
		}
		if (query.size() == 1) {
			return toBsonFilter(query.first());
		}

		List<Bson> bsons = new ArrayList<Bson>(query.size());
		for (QueryMember member : query.getQueryMembers()) {
			bsons.add(toBsonFilter(member));
		}

		return and(bsons);
	}

	@Override
	public QueryResult last(EventQuery query, String correlatorKey) {
		Document result = collection.find(toBsonFilter(query)).sort(Sorts.descending(correlatorKey)).first();
		if (result != null) {
			return new MongoQueryResult(result);
		} else {
			return null;
		}
	}
}