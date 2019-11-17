package com.jeremy.fall;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sineshore.j2dge.v1_1.J2DGE;

public class Audio {

	private static HashMap<String, Sound> sounds = new HashMap<>();

	public static void loadAudio(String name, URL url) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		sounds.put(name, new Sound(url));
	}

	public static void playAudio(String name, float volume) {
		if (!sounds.containsKey(name)) {
			throw new IllegalStateException("Couldn't find audio file '" + name + "'");
		}
		Sound sound = sounds.get(name);
		J2DGE.runOnThread(() -> {
			try {
				sound.play(volume);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		});
	}

	static class Sound {

		private AudioFormat format;
		private DataLine.Info info;
		private byte[] data;
		private Clip clip;

		public Sound(URL url) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
			AudioInputStream src = AudioSystem.getAudioInputStream(url);
			format = src.getFormat();

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int bytesRead;
			byte[] dataChunk = new byte[1024];
			while ((bytesRead = src.read(dataChunk, 0, dataChunk.length)) != -1) {
				buffer.write(dataChunk, 0, bytesRead);
			}
			buffer.flush();
			this.data = buffer.toByteArray();

			info = new DataLine.Info(Clip.class, format);
		}

		public void play(float volume) throws LineUnavailableException {
			if (clip == null) {
				clip = (Clip) AudioSystem.getLine(info);
				clip.open(format, data, 0, data.length);
			}
			clip.stop();
			clip.setFramePosition(0);
			FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float decibles = 20f * (float) Math.log10(volume);
			volumeControl.setValue(decibles);
			clip.start();
		}

	}

}
