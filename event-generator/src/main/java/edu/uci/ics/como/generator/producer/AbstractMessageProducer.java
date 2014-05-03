package edu.uci.ics.como.generator.producer;

import edu.uci.ics.como.generator.config.Config;
import edu.uci.ics.comon.protocol.CoMonMessage;
import edu.uci.ics.comon.protocol.CoMonMessageBuilder;

public abstract class AbstractMessageProducer implements MessageProducer {

	protected CoMonMessage createCoMonMessage(String value) {
		CoMonMessageBuilder builder = new CoMonMessageBuilder();
		builder.setEventType(Config.get().getString("producer.event.type"));
		builder.setSourceID(Config.get().getString("producer.source.id"));
		builder.setValue(value);
		builder.setVersion(Config.get().getString("producer.protocol.version"));
		builder.setTime(System.currentTimeMillis());

		return builder.build();
	}

}