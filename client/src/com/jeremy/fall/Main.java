package com.jeremy.fall;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import javax.swing.JFrame;

import com.sineshore.j2dge.v1_1.Game;
import com.sineshore.j2dge.v1_1.assets.Assets;

public class Main extends Game {

	public static final int WIDTH = 320;
	public static final int HEIGHT = 448;

	public static final Random RANDOM = new Random();

	public static BufferedImage[] textures;
	public static BufferedImage[] backgrounds;
	public static Save save;
	public static Font font;
	public static Font special;

	public Main() {
		super("Fall", "1.0", WIDTH, HEIGHT);
		BufferedImage sheet = Assets.getImage("/textures.png");
		textures = new BufferedImage[sheet.getWidth() * sheet.getHeight() / 16 / 16];
		for (int i = 0, iLen = sheet.getHeight() / 16; i < iLen; i++) {
			for (int j = 0, jLen = sheet.getWidth() / 16; j < jLen; j++) {
				textures[i * jLen + j] = sheet.getSubimage(j * 16, i * 16, 16, 16);
			}
		}
		sheet = Assets.getImage("/backgrounds.png");
		backgrounds = new BufferedImage[sheet.getWidth() * sheet.getHeight() / 320 / 448];
		for (int i = 0, iLen = sheet.getHeight() / 448; i < iLen; i++) {
			for (int j = 0, jLen = sheet.getWidth() / 320; j < jLen; j++) {
				backgrounds[i * jLen + j] = sheet.getSubimage(j * 320, i * 448, 320, 448);
			}
		}

		try {
			File file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (!file.isDirectory()) {
				file = file.getParentFile();
			}
			save = new Save(new File(file, "save.data"));
			font = Font.createFont(Font.PLAIN, Main.class.getResourceAsStream("/font.ttf")).deriveFont(Font.PLAIN, 20f);
			special = Font.createFont(Font.PLAIN, Main.class.getResourceAsStream("/november.ttf")).deriveFont(Font.PLAIN, 16f);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		stateManager.registerState(new MenuState(this));
		stateManager.registerState(new GameState(this));
		stateManager.registerState(new HelpState(this));
		stateManager.enterState(MenuState.class);
		window.setIconImage(textures[0]);
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		new Main().start();
	}

}
