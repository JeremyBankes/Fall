package com.jeremy.fall;

import java.awt.Color;

import com.sineshore.j2dge.v1_1.Game;
import com.sineshore.j2dge.v1_1.graphics.Renderer;
import com.sineshore.j2dge.v1_1.input.KeyInput;
import com.sineshore.j2dge.v1_1.input.KeyInput.KeyAction;
import com.sineshore.j2dge.v1_1.input.KeyInput.KeyInputEvent;
import com.sineshore.j2dge.v1_1.state.State;
import com.sineshore.j2dge.v1_1.state.component.Button;
import com.sineshore.j2dge.v1_1.state.component.Label;

public class MenuState extends State {

	private Label highscores;

	public MenuState(Game game) {
		super(game);
		setFont(Main.font);
		setBackgroundImage(Main.backgrounds[4]);

		Label title = new Label("Fall", 0.5f, 0.25f, 1f, 0.1f);
		title.setForegroundColor(new Color(0x2783B4));
		add(title);

		Button play = new Button("Play", 0.5f, 0.35f, 0.25f, 0.1f);
		play.setHoverForegroundColor(Color.GRAY);
		play.setExecution(() -> {
			getManager().enterState(GameState.class);
		});
		add(play);

		Button help = new Button("Help", 0.5f, 0.45f, 0.25f, 0.1f);
		help.setHoverForegroundColor(Color.GRAY);
		help.setExecution(() -> {
			getManager().enterState(HelpState.class);
		});
		add(help);

		getManager().getKeyInput().addKeyEventCallback((KeyInputEvent event) -> {
			if (!isCurrentState()) {
				return;
			}
			if (event.action == KeyAction.RELEASE) {
				if (event.asciiCode == KeyInput.KEY_SPACE || event.asciiCode == KeyInput.KEY_ENTER) {
					play.getExecution().run();
				}
			}
		});

		highscores = new Label("Loading...", 0.5f, 0.55f, 1.0f, 0.45f);
		highscores.setFont(Main.special);
		add(highscores);
	}

	@Override
	public void enter() {
		super.enter();
		setHighscoreLine("Loading...");
		Client client = getManager().getState(GameState.class).getClient();
		client.requestScores();
	}

	public void setHighscoreLine(String line) {
		highscores.setText("Leader Board\n" + line);
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(Renderer renderer) {
		super.render(renderer);
	}

}
