package edu.uci.ics.como.generator.rates;

import java.util.Random;

import edu.uci.ics.como.generator.config.Config;

public class RandomRate implements Rate {

	private Random random;

	private int min;

	private int max;

	public RandomRate() {
		random = new Random(System.currentTimeMillis());
		min = Config.get().getInt("rate.random.min");
		max = Config.get().getInt("rate.random.max");
	}

	@Override
	public int howMany() {
		return random.nextInt(max - min + 1) + min;
	}

}