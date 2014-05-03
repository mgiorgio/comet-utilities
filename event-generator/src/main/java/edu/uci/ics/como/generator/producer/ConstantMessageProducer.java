package edu.uci.ics.como.generator.producer;

import edu.uci.ics.como.generator.config.Config;
import edu.uci.ics.comon.protocol.CoMonMessage;

public class ConstantMessageProducer extends AbstractMessageProducer {

	private CoMonMessage message;

	public ConstantMessageProducer() {
		this.message = createCoMonMessage(Config.get().getString("producer.constant.msg"));
	}

	@Override
	public CoMonMessage produce() {
		return this.message;
	}

}