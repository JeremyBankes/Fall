package com.jeremy.fall;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.sineshore.j2dge.v1_1.graphics.Renderer;
import com.sineshore.j2dge.v1_1.input.KeyInput;
import com.sineshore.j2dge.v1_1.input.KeyInput.KeyAction;
import com.sineshore.j2dge.v1_1.input.KeyInput.KeyInputEvent;

public class Map {

	public static final int WIDTH = 10;
	public static final int HEIGHT = 14;
	public static final int TILE_SIZE = Main.WIDTH / WIDTH;
	public static final Color[] COLORS = {

			new Color(0xAAAAAA), //
			new Color(0xBBBBBB), //
			new Color(0xCCCCCC), //
			new Color(0xDDDDDD), //
			new Color(0xEEEEEE), //
			new Color(0xFFFFFF)

	};

	private final Tile[][] tiles;
	private final GameState gameState;
	private int pause;
	private int age;
	private int lastScore, score;
	private int level = 1, lastLevel;

	private HashMap<Integer, String> pauseMessages;

	public Map(GameState gameState) {
		tiles = new Tile[HEIGHT][WIDTH];
		this.gameState = gameState;
		pauseMessages = new HashMap<>();

		HashSet<Tile> actioned = new HashSet<>();
		gameState.getGame().getKeyInput().addKeyEventCallback((KeyInputEvent event) -> {

			if (gameState.isPause()) {
				return;
			}

			if (event.action == KeyAction.PRESS && event.asciiCode == KeyInput.KEY_SPACE) {
				for (int i = 0; i < HEIGHT; i++) {
					for (int j = 0; j < WIDTH; j++) {
						Tile tile = tiles[i][j];
						if (tile != null && !actioned.contains(tile)) {
							tile.action(j, i);
							actioned.add(tile);
						}
					}
				}
				if (!actioned.isEmpty()) {
					Audio.playAudio("shift", 1f);
				}
				actioned.clear();
			}
			if (event.action == KeyAction.PRESS && event.asciiCode == KeyInput.KEY_L) {
				if (gameState.getGame().getKeyInput().isPressed(KeyInput.KEY_SHIFT)) {
					age = age - (age % (60 * 60)) + (60 * 60) - 2;
				}
			}
		});

		// gameState.getGame().getMouseInput().addMouseCallback((MouseInputEvent event)
		// -> {
		// if (event.action == MouseAction.PRESS) {
		// spawnTile(event.x / TILE_SIZE, (Main.HEIGHT - event.y) / TILE_SIZE);
		// }
		// });
	}

	public void tick() {
		if (pause > 0) {
			if (pauseMessages.containsKey(pause)) {
				GameState.EpicText.show(pauseMessages.get(pause), false, false, pause);
			}
			pause--;
			return;
		} else {
			age++;
		}

		if (gameState.lose) {
			gameState.getManager().enterState(MenuState.class);
		}

		if (age % multiple() == 0) {
			if (chance(0.1)) {
				spawnRandomTile();
			}
			drop();
			testLineBreak(gameState.getPlayer().y);
		}

		if (age > 0 && age % (60 * 60) == 0) {
			score += 50 * level;
			level++;
			pause = 60 * 5;
			Audio.playAudio("levelup", 1f);
			pauseMessages.clear();
			pauseMessages.put(pause, "Level Up");
			pauseMessages.put(pause / 2, "Level " + level);
		}
	}

	private void drop() {
		Player player = gameState.getPlayer();
		boolean playAudio = false;
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				Tile tile = tiles[i][j];
				if (tile == null) {
					continue;
				}
				if (tile instanceof FastTile || age % (multiple() * 2) == 0) {
					if (tile.drop(j, i)) {
						playAudio = true;
					}
				}

			}
		}
		if (playAudio && age % (multiple() * 2) == 0) {
			Audio.playAudio("fall", 1f);
		}
		if (getTile(player.x, player.y) != null) {
			Audio.playAudio("lose", 1f);
			gameState.reset();
			pause = 60 * 5;
			pauseMessages.clear();
			pauseMessages.put(pause, "Final Level " + lastLevel + "\nFinal Score " + lastScore);
			gameState.getClient().sendScore(Client.beautify(System.getProperty("user.name")), "", lastLevel, lastScore);
		}
		player.drop();
	}

	private int multiple() {
		boolean speed = gameState.getManager().getKeyInput().isAnyPressed(KeyInput.KEY_DOWN, 'S');
		return Math.max(1, Math.max(2, (7 - level)) / (speed ? 3 : 1));
	}

	private void breakLine(int y) {
		score += 25;
		for (int j = 0; j < WIDTH; j++) {
			tiles[y][j] = null;
		}
		GameState.EpicText.show(Words.getNice(), true, false, 120);
		Audio.playAudio("bing", 1f);
	}

	private void breakColumn(int x) {
		score += 150;
		for (int i = 0; i < HEIGHT; i++) {
			tiles[i][x] = null;
		}
		GameState.EpicText.show(Words.getNice(), true, true, 120);
		Audio.playAudio("bing", 1f);
	}

	public void render(Renderer renderer) {
		renderer.drawImage(Main.backgrounds[Math.min(Main.backgrounds.length, level) - 1], 0, 0, Main.WIDTH, Main.HEIGHT);
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				Tile tile = tiles[i][j];
				if (tile == null) {
					continue;
				}
				tile.render(renderer, j, i);
			}
		}
		renderer.setColor(Color.WHITE);
		renderer.drawText("Level " + level, Main.WIDTH / 2, TILE_SIZE / 2, true, true);
		renderer.drawText("Score " + score, Main.WIDTH / 2, TILE_SIZE / 2 + renderer.getTextHeight(), true, true);
	}

	public Tile getTile(int x, int y) {
		return tiles[y][x];
	}

	private void spawnRandomTile() {
		int x = Main.RANDOM.nextInt(WIDTH);
		int y = HEIGHT - 1;
		spawnTile(x, y);
	}

	private void spawnTile(int x, int y) {
		if (chance(0.75)) {
			if (chance(0.75)) {
				tiles[y][x] = chance(0.5) ? new MoveRightTile() : new MoveLeftTile();
			} else {
				tiles[y][x] = new FastTile();
			}
		} else {
			tiles[y][x] = new DullTile();
		}
	}

	public void score(Tile tile, int x, int y) {
		tile.scored = true;
		score++;

		if (y >= HEIGHT - 1) {
			breakColumn(x);
		}
	}

	public void testLineBreak(int y) {
		for (int x = 0; x < WIDTH; x++) {
			if (tiles[y][x] == null) {
				if (gameState.getPlayer().x != x || gameState.getPlayer().y != y) {
					break;
				}
			}
			if (x == WIDTH - 1) {
				breakLine(y);
			}
		}
	}

	public void reset() {
		for (int i = 0; i < HEIGHT; i++) {
			Arrays.fill(tiles[i], null);
		}
		lastScore = score;
		lastLevel = level;
		level = 1;
		score = 0;
		age = 0;
	}

	private static boolean chance(double chance) {
		return Math.random() < chance;
	}

	private abstract class Tile {

		private int texture;
		protected volatile boolean scored;

		private Tile(int texture) {
			this.texture = texture;
		}

		public void action(int x, int y) {
		}

		public boolean drop(int x, int y) {
			if (y <= 0) {
				if (!scored) {
					Audio.playAudio("thunk", 1f);
					score(this, x, y);
				}
			} else if (tiles[y - 1][x] != null) {
				if (!scored && tiles[y - 1][x].scored) {
					Audio.playAudio("thunk", 1f);
					score(this, x, y);
				}
			} else {
				tiles[y][x] = null;
				tiles[y - 1][x] = this;
				return true;
			}
			testLineBreak(y);
			return false;
		}

		private void render(Renderer renderer, int x, int y) {
			renderer.drawImage(Main.textures[texture], x * TILE_SIZE, Main.HEIGHT - (y + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		}

	}

	private class DullTile extends Tile {

		public DullTile() {
			super(1);
		}

	}

	private class FastTile extends Tile {

		public FastTile() {
			super(2);
		}

	}

	private class MoveLeftTile extends Tile {

		public MoveLeftTile() {
			super(3);
		}

		@Override
		public void action(int x, int y) {
			if (scored) {
				return;
			}
			if (tiles[y][x == 0 ? WIDTH - 1 : x - 1] == null) {
				tiles[y][x == 0 ? WIDTH - 1 : x - 1] = this;
				tiles[y][x] = null;
			}
		}

	}

	private class MoveRightTile extends Tile {

		public MoveRightTile() {
			super(4);
		}

		@Override
		public void action(int x, int y) {
			if (scored) {
				return;
			}
			if (tiles[y][x == WIDTH - 1 ? 0 : x + 1] == null) {
				tiles[y][x == WIDTH - 1 ? 0 : x + 1] = this;
				tiles[y][x] = null;
			}
		}

	}

}
