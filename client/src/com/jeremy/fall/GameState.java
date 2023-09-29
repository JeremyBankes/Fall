package com.jeremy.fall;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import static java.awt.RenderingHints.*;

import com.sineshore.j2dge.v1_1.Game;
import com.sineshore.j2dge.v1_1.graphics.Renderer;
import com.sineshore.j2dge.v1_1.input.KeyInput;
import com.sineshore.j2dge.v1_1.input.KeyInput.KeyAction;
import com.sineshore.j2dge.v1_1.input.KeyInput.KeyInputEvent;
import com.sineshore.j2dge.v1_1.state.State;
import com.sineshore.j2dge.v1_1.state.component.Image;

public class GameState extends State {

	private Client client;
	private Player player;
	private Map map;
	boolean lose;

	boolean pause;
	boolean keyPaused;

	private int snap;
	private boolean keySnapped;

	public GameState(Game game) {
		super(game);
		map = new Map(this);
		player = new Player(this);

		try {
			client = new Client((Main) game, "jeremybankes.com", 5411);
			setFont(Main.font);
			Audio.loadAudio("fall", GameState.class.getResource("/fall.wav"));
			Audio.loadAudio("shift", GameState.class.getResource("/shift.wav"));
			Audio.loadAudio("thunk", GameState.class.getResource("/thunk.wav"));
			Audio.loadAudio("bing", GameState.class.getResource("/bing.wav"));
			Audio.loadAudio("lose", GameState.class.getResource("/lose.wav"));
			Audio.loadAudio("move", GameState.class.getResource("/move.wav"));
			Audio.loadAudio("start", GameState.class.getResource("/start.wav"));
			Audio.loadAudio("levelup", GameState.class.getResource("/levelup.wav"));
			Audio.loadAudio("snap", GameState.class.getResource("/snap.wav"));
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		getManager().getKeyInput().addKeyEventCallback((KeyInputEvent event) -> {
			if (!getManager().isCurrentState(getClass())) {
				return;
			}
			if (event.action == KeyAction.PRESS) {
				if (!keySnapped && event.asciiCode == 'C') {
					if (snap == 0) {
						Audio.playAudio("snap", 1f);
						Canvas canvas = getManager().getGame().getCanvas();
						new CopyImagetoClipBoard(new Rectangle(canvas.getLocationOnScreen(), canvas.getSize()));
						snap = 20;
						keySnapped = true;
					}
				}
				if (!keyPaused && (event.asciiCode == 'P' || event.asciiCode == KeyInput.KEY_ESCAPE)) {
					pause = !pause;
					keyPaused = true;
				}
			}
			if (event.action == KeyAction.RELEASE) {
				if (keyPaused) {
					keyPaused = false;
				}
				if (keySnapped) {
					keySnapped = false;
				}
			}
		});
	}

	public void reset() {
		map.reset();
		player.setLocation(Map.WIDTH / 2, 0);
		lose = true;
	}

	@Override
	public void enter() {
		super.enter();
		Audio.playAudio("start", 1f);
		lose = false;
	}

	@Override
	public void tick() {
		if (snap > 0) {
			snap--;
		}

		if (pause) {
			return;
		}
		super.tick();
		map.tick();
		EpicText.tick();
	}

	@Override
	public void render(Renderer renderer) {
		renderer.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		renderer.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF);
		renderer.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
		renderer.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON);

		renderer.setFont(getFont());

		if (pause) {
			renderer.setColor(Color.BLACK);
			renderer.drawRectangle(0, 0, Main.WIDTH, Main.HEIGHT);
			renderer.setColor(Color.WHITE);
			int y = Main.HEIGHT / 3;
			renderer.drawText("Paused", Main.WIDTH / 2, y, true, true);
			renderer.drawText("Press P", Main.WIDTH / 2, y += renderer.getTextAscent() * 2, true, true);
			renderer.drawText("To Unpause", Main.WIDTH / 2, y += renderer.getTextAscent(), true, true);
			return;
		}

		super.render(renderer);
		map.render(renderer);
		player.render(renderer);
		EpicText.render(renderer);

		if (snap > 0) {
			renderer.setColor(new Color(1f, 1f, 1f, (float) snap / 20));
			renderer.drawRectangle(0, 0, Main.WIDTH, Main.HEIGHT);
		}

	}

	public Player getPlayer() {
		return player;
	}

	public Map getMap() {
		return map;
	}

	public Client getClient() {
		return client;
	}

	public boolean isPause() {
		return pause;
	}

	public static class EpicText {

		private static String text;
		private static int age, lifetime;

		private static float scale;
		private static float rotation;
		private static Color color;
		private static boolean rotateDirection;
		private static boolean fancy;
		private static boolean spasm;

		public static void show(String text, boolean fancy, boolean spasm, int ticks) {
			rotateDirection = Main.RANDOM.nextBoolean();
			EpicText.text = text;
			EpicText.fancy = fancy;
			EpicText.spasm = spasm;
			lifetime = ticks;
			age = 0;

			rotation = 0;
			scale = 1;
		}

		private static void tick() {
			if (age > lifetime) {
				return;
			}
			if (spasm) {
				color = new Color((int) ((Math.random() - 0.5) * 2 * Integer.MAX_VALUE)).brighter().brighter();
			}
			if (fancy) {
				float a = (float) age / lifetime;
				rotation = (float) (a * a * a * a * a * Math.PI * 2);
				scale = 1f - a * a * a;
			}
			age++;
		}

		private static void render(Renderer renderer) {
			if (text == null || age > lifetime) {
				return;
			}

			if (spasm) {
				renderer.setColor(color);
			} else {
				renderer.setColor(Color.WHITE);
			}

			float textHeight = renderer.getTextAscent();

			float x = Main.WIDTH / 2;
			float y = Main.HEIGHT / 2;

			renderer.saveTransform();
			String[] lines = text.split("\n");
			for (String line : lines) {
				float textWidth = renderer.getTextWidth(line);
				AffineTransform transform = AffineTransform.getTranslateInstance(x - textWidth * scale / 2, y + textHeight * scale / 2);
				transform.scale(scale, scale);
				transform.rotate(rotateDirection ? rotation : -rotation, textWidth / 2, 0);

				renderer.setTransform(transform);
				renderer.drawText(line, 0, 0, false, false);
				y += textHeight;
			}
			renderer.loadTransform();
		}

	}

	class ImageSelection implements Transferable {

		private Image image;

		public ImageSelection(Image image) {
			this.image = image;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!DataFlavor.imageFlavor.equals(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return image;
		}

	}

}
