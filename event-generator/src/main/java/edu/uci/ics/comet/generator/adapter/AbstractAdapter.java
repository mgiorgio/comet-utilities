package edu.uci.ics.comet.generator.adapter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.comet.components.LifecycleException;
import edu.uci.ics.comet.components.serializer.COMETSerializer;
import edu.uci.ics.comet.components.serializer.JSONCOMETSerializer;
import edu.uci.ics.comet.generator.producer.MessageProducer;
import edu.uci.ics.comet.generator.rates.Rate;
import edu.uci.ics.comet.protocol.COMETMessage;

public abstract class AbstractAdapter implements EventStreamAdapter {

	private static final Logger log = LoggerFactory.getLogger(AbstractAdapter.class);

	private HierarchicalConfiguration config;

	private COMETSerializer serializer;

	public void setSerializer(COMETSerializer serializer) {
		this.serializer = serializer;
	}

	public HierarchicalConfiguration getConfig() {
		return config;
	}

	public void setConfig(HierarchicalConfiguration config) {
		this.config = config;
	}

	@Override
	public void init() throws LifecycleException {
		this.setSerializer(createSerializer());
	}

	private COMETSerializer createSerializer() {
		String serializationMethod = getConfig().getString("serializer");

		if (serializationMethod == null) {
			log.warn("Serialization method was not defined. Using default {}", JSONCOMETSerializer.class.getName());
			return new JSONCOMETSerializer();
		}

		try {
			@SuppressWarnings("unchecked")
			Class<COMETSerializer> loadClass = (Class<COMETSerializer>) ClassLoader.getSystemClassLoader().loadClass(serializationMethod);

			return loadClass.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			log.warn("Serialization method could not be created. Using default {}", JSONCOMETSerializer.class.getName());
			return new JSONCOMETSerializer();
		}
	}

	public COMETSerializer getSerializer() {
		return this.serializer;
	}

	protected abstract void doSend(COMETMessage message) throws IOException;

	@Override
	public void sendWithRate(MessageProducer producer, Rate rate) throws IOException {
		long remaining = rate.total();

		/*
		 * remaining > 0: Limited number of messages to send. remaining = 0: No
		 * more messages to send. remaining < 0: Unlimited messages to send.
		 */
		while (remaining != 0) {
			long before = System.nanoTime();

			for (int i = 0; i < rate.amount() && remaining != 0; i++) {
				doSend(producer.produce());
				// sent++;
				if (remaining > 0) {
					remaining--;
				}
			}
			long after = System.nanoTime();
			try {
				long nanosSpent = TimeUnit.NANOSECONDS.toMillis(after - before);
				long timeSlotInNanos = rate.unit().toNanos(1);

				if (nanosSpent < timeSlotInNanos && remaining != 0) {
					Thread.sleep(TimeUnit.NANOSECONDS.toMillis(timeSlotInNanos - nanosSpent));
				}
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}