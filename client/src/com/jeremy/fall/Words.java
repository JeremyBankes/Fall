package com.jeremy.fall;

public class Words {

	public static final String[] NICE = { "Nice", "Bravo", "Great", "Good Job", "Fantastic", "Amazing", "Spectacular" };

	public static final String getNice() {
		return NICE[Main.RANDOM.nextInt(NICE.length)];
	}

}
