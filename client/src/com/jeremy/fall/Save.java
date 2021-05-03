package com.jeremy.fall;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import com.sineshore.serialization.Batch;
import com.sineshore.serialization.Capsule;

public class Save {

	private File saveFile;

	private Capsule capsule;

	public Save(File saveFile) {
		this.saveFile = saveFile;
	}

	public void persistDimension(String name, Dimension dimension) {
		Batch dimensionBatch = new Batch(name);
		dimensionBatch.add("width", dimension.width);
		dimensionBatch.add("height", dimension.height);
		capsule.add(dimensionBatch);
	}

	public Dimension getDimension(String name, Dimension fallback) {
		if (capsule.hasBatch(name)) {
			Batch dimensionBatch = capsule.getBatch(name);
			return new Dimension((int) dimensionBatch.get("width"), (int) dimensionBatch.get("height"));
		} else {
			return fallback;
		}
	}

	public void persistInteger(String name, int integer) {
		Batch integersBatch = capsule.getBatch("integers");
		integersBatch.add(name, integer);
	}

	public int getInteger(String name, int fallback) {
		Batch integersBatch = capsule.getBatch("integers");
		if (integersBatch.contains(name)) {
			return (int) integersBatch.get(name);
		} else {
			return fallback;
		}
	}

	public void persistString(String name, String string) {
		Batch stringsBatch = capsule.getBatch("strings");
		stringsBatch.add(name, string);
	}

	public String getString(String name, String fallback) {
		Batch stringsBatch = capsule.getBatch("strings");
		if (stringsBatch.contains(name)) {
			return (String) stringsBatch.get(name);
		} else {
			return fallback;
		}
	}

	public void saveToFile() {
		try {
			capsule.writeToFile(saveFile);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public void loadFromFile() {
		if (saveFile.exists()) {
			try {
				capsule = new Capsule(saveFile);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		if (capsule == null) {
			capsule = new Capsule("Preferences");
			try {
				capsule.writeToFile(saveFile);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}

		if (!capsule.hasBatch("integers")) {
			capsule.add(new Batch("integers"));
		}
		if (!capsule.hasBatch("strings")) {
			capsule.add(new Batch("strings"));
		}
	}

}
