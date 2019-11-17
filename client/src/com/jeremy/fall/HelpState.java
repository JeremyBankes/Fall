package com.jeremy.fall;

import java.awt.Color;

import com.sineshore.j2dge.v1_1.Game;
import com.sineshore.j2dge.v1_1.state.State;
import com.sineshore.j2dge.v1_1.state.component.Button;

public class HelpState extends State {

	public HelpState(Game game) {
		super(game);
		setFont(Main.font);
		setBackgroundImage(Main.backgrounds[5]);

		Button back = new Button("Back", 0.5f, 0.825f, 0.25f, 0.1f);
		back.setHoverForegroundColor(Color.GRAY);
		back.setExecution(() -> {
			getManager().enterState(MenuState.class);
		});
		add(back);
	}

}
