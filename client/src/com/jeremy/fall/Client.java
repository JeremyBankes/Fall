package com.jeremy.fall;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.sineshore.j2dge.v1_1.J2DGE;
import com.sineshore.serialization.v2_1.Batch;
import com.sineshore.serialization.v2_1.Element;

public class Client {

	private final InetAddress address;
	private final int port;

	private Socket tcpSocket;

	private InputStream inputStream;
	private OutputStream outputStream;

	private int bufferSize;

	private Main main;

	public Client(Main main, String serverAddress, int serverPort) throws UnknownHostException {
		this.address = InetAddress.getByName(serverAddress);
		this.port = serverPort;
		this.bufferSize = 1024;
		this.main = main;
	}

	public void connect() throws IOException {
		tcpSocket = new Socket();
		tcpSocket.setSoTimeout(500);
		tcpSocket.connect(new InetSocketAddress(address, port), 1000);

		inputStream = tcpSocket.getInputStream();
		outputStream = tcpSocket.getOutputStream();

		new Thread(this::listenTcp, "client-tcp").start();
	}

	private void listenTcp() {
		while (!tcpSocket.isClosed()) {
			receiveTcp(inputStream);
			try {
				disconnect();
			} catch (SocketTimeoutException timeout) {

			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	protected void receiveTcp(InputStream inputStream) {
		try {
			Batch response = new Batch(inputStream);
			if (response.getName().equals("scores")) {
				MenuState menuState = main.getStateManager().getState(MenuState.class);
				String displayLine = "";
				for (Element element : response.getElements()) {
					displayLine += element.getName() + " Level: " + element.getValue().toString().replace(" ", " Score: ") + "\n";
				}
				menuState.setHighscoreLine(displayLine);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public void sendScore(String name, String location, int level, int score) {
		J2DGE.runOnThread(() -> {
			try {
				connect();
				Batch batch = new Batch("new-score");
				batch.add("name", name);
				batch.add("location", location);
				batch.add("level", level);
				batch.add("score", score);
				send(batch.toBytes());
			} catch (IOException exception) {
				main.getStateManager().getState(MenuState.class).setHighscoreLine("Failed to connect to database.");
				exception.printStackTrace();
			}
		});
	}

	public void requestScores() {
		J2DGE.runOnThread(() -> {
			try {
				connect();
				send(new Batch("request-scores").toBytes());
			} catch (IOException exception) {
				main.getStateManager().getState(MenuState.class).setHighscoreLine("Failed to connect to database.");
				exception.printStackTrace();
			}
		});
	}

	public void disconnect(Exception cause) throws IOException {
		if (cause != null) {
			new Exception("Exception caused disconnect: " + cause.getMessage(), cause).printStackTrace();
		}
		tcpSocket.close();
	}

	public void disconnect() throws IOException {
		disconnect(null);
	}

	public void send(byte[] data) throws IOException {
		outputStream.write(data);
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public boolean isConnected() {
		return tcpSocket != null && tcpSocket.isConnected() && !tcpSocket.isClosed();
	}

	public String getServerAddress() {
		return address.getHostAddress();
	}

	public int getServerPort() {
		return port;
	}

	public int getLocalTcpPort() {
		return tcpSocket.getLocalPort();
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public static char[] WHITESPACE_CHARACTERS = { ' ', '_' };

	public static final boolean arrayContains(char[] array, char value) {
		for (int object : array)
			if (object == value)
				return true;
		return false;
	}

	public static final String beautify(String message) {
		char[] characters = message.toLowerCase().replaceAll(" +", " ").toCharArray();
		characters[0] = Character.toUpperCase(characters[0]);
		for (int i = 0, len = characters.length; i < len; i++)
			if (i != 0)
				if (arrayContains(WHITESPACE_CHARACTERS, characters[i - 1]))
					characters[i] = Character.toUpperCase(characters[i]);
		return new String(characters);
	}

}
