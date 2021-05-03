package com.jeremy.fall;

import com.sineshore.j2dge.v1_1.graphics.Renderer;
import com.sineshore.j2dge.v1_1.input.KeyInput;
import com.sineshore.j2dge.v1_1.input.KeyInput.KeyAction;
import com.sineshore.j2dge.v1_1.input.KeyInput.KeyInputEvent;

public class Player {

	public int x, y;
	private GameState gameState;

	public Player(GameState gameState) {
		y = 0;
		x = Map.WIDTH / 2;
		this.gameState = gameState;

		gameState.getGame().getKeyInput().addKeyEventCallback((KeyInputEvent event) -> {
			if (gameState.isPause()) {
				return;
			}
			Map map = gameState.getMap();
			if (event.action == KeyAction.PRESS) {
				if (event.asciiCode == 'D' || event.asciiCode == KeyInput.KEY_RIGHT) {
					if (x < Map.WIDTH - 1) {
						if (map.getTile(x + 1, y) == null) {
							Audio.playAudio("move", 1f);
							x++;
						} else if (map.getTile(x + 1, y + 1) == null) {
							Audio.playAudio("move", 1f);
							x++;
							y++;
						}
					}
				} else if (event.asciiCode == 'A' || event.asciiCode == KeyInput.KEY_LEFT) {
					if (x > 0) {
						if (map.getTile(x - 1, y) == null) {
							Audio.playAudio("move", 1f);
							x--;
						} else if (map.getTile(x - 1, y + 1) == null) {
							Audio.playAudio("move", 1f);
							x--;
							y++;
						}
					}
				}
				drop();
			}
		});
	}

	public void render(Renderer renderer) {
		renderer.drawImage(Main.textures[0], x * Map.TILE_SIZE, Main.HEIGHT - (y + 1) * Map.TILE_SIZE, Map.TILE_SIZE, Map.TILE_SIZE);
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void drop() {
		while (y > 0 && gameState.getMap().getTile(x, y - 1) == null) {
			y--;
		}
	}

}
