package com.jeremy.fall.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Scanner;

import com.sineshore.networkengine.NetworkServer;
import com.sineshore.serialization.v2_1.Batch;

public class Main extends NetworkServer {

	public static final int PORT = 5411;

	private Batch confirmation = new Batch("confirm");

	private LinkedList<ScoreEntry> entries;

	public Main() {
		super(PORT);
		entries = new LinkedList<>();

		try {
			File directory = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (!directory.isDirectory()) {
				directory = directory.getParentFile();
			}
			File saveFile = new File(directory, "leaderboard.data");

			if (saveFile.exists()) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(saveFile));
					String line;
					while ((line = reader.readLine()) != null) {
						entries.add(new ScoreEntry(line));
					}
					reader.close();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					if (!saveFile.exists()) {
						saveFile.createNewFile();
					}
					BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
					for (ScoreEntry entry : entries) {
						writer.append(entry.toString());
						writer.append('\n');
					}
					writer.close();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}));
		} catch (URISyntaxException exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public void start() throws IOException {
		super.start();
		new Thread(() -> {
			Scanner scanner = new Scanner(System.in);
			while (isConnected()) {
				if (scanner.nextLine().equalsIgnoreCase("exit")) {
					try {
						stop();
					} catch (IOException exception) {
						exception.printStackTrace();
					}
				}
			}
			scanner.close();
		}).start();
	}

	@Override
	protected void receiveTcp(String address, int port, InputStream inputStream) {
		try {
			Batch request = new Batch(inputStream);
			System.out.println(request);

			switch (request.getName()) {
			case "new-score":
				String name = request.get("name", String.class);
				int level = request.get("level", Integer.class);
				int score = request.get("score", Integer.class);
				newEntry(new ScoreEntry(name, level, score));
			case "request-scores":
				Batch scoreBatch = new Batch("scores");
				for (int i = 0; i < entries.size(); i++) {
					ScoreEntry e = entries.get(i);
					if (e != null) {
						scoreBatch.add(e.name, e.level + " " + e.score);
					}
				}
				send(address, port, scoreBatch.toBytes(), true);
				break;
			default:
				send(address, port, confirmation.toBytes(), true);
				break;
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	protected void receiveUdp(String address, int port, byte[] data) {
	}

	public void newEntry(ScoreEntry entry) {
		ScoreEntry exitingEntry = entries.stream().filter(e -> e.name.equals(entry.name)).findAny().orElse(null);
		if (exitingEntry != null) {
			if (exitingEntry.score < entry.score) {
				entries.remove(exitingEntry);
			} else {
				return;
			}
		}
		for (int i = 0, len = entries.size(); i <= len; i++) {
			if (i == len) {
				entries.add(i, entry);
			} else {
				if (entries.get(i).score < entry.score) {
					entries.add(i, entry);
					break;
				}
			}
		}
		while (entries.size() > 5) {
			entries.removeLast();
		}
	}

	private static class ScoreEntry {

		private String name;
		private int level;
		private int score;

		public ScoreEntry(String name, int level, int score) {
			this.name = name;
			this.level = level;
			this.score = score;
		}

		public ScoreEntry(String line) {
			String[] values = line.split("\\.");
			name = values[0];
			level = Integer.parseInt(values[1]);
			score = Integer.parseInt(values[2]);
		}

		@Override
		public String toString() {
			return name + "." + level + "." + score;
		}

	}

	public static void main(String[] args) {
		try {
			new Main().start();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

}
