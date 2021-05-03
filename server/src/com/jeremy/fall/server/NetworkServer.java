package com.jeremy.fall.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

public abstract class NetworkServer {

	private final int port;

	private ServerSocket tcpServerSocket;
	private DatagramSocket udpServerSocket;

	public HashMap<InetSocketAddress, Socket> clients;

	private int bufferSize;

	public NetworkServer(int port) {
		this.port = port;
		this.clients = new HashMap<>();
		this.bufferSize = 1024;
	}

	public void start() throws IOException {
		tcpServerSocket = new ServerSocket(port);
		udpServerSocket = new DatagramSocket(port);

		tcpServerSocket.setSoTimeout(500);
		udpServerSocket.setSoTimeout(500);

		new Thread(this::runTcp, "server-tcp").start();
		new Thread(this::runUdp, "server-udp").start();
	}

	private final void runTcp() {
		while (isConnected()) {
			try {
				Socket clientSocket = tcpServerSocket.accept();
				String address = clientSocket.getInetAddress().getHostAddress();
				int port = clientSocket.getPort();
				new Thread(() -> {
					connect(clientSocket);
					Exception disconnectCause = null;
					try {
						receiveTcp(address, port, clientSocket.getInputStream());
					} catch (IOException exception) {
						disconnectCause = exception;
					} finally {
						disconnect((InetSocketAddress) clientSocket.getRemoteSocketAddress(), disconnectCause);
					}
				}, String.format("server-client-%s:%s", address, port)).start();
			} catch (SecurityException blockedConnection) {

			} catch (SocketTimeoutException timeout) {

			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	private void runUdp() {
		while (isConnected()) {
			try {
				DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
				udpServerSocket.receive(packet);
				receiveUdp(packet.getAddress().getHostAddress(), packet.getPort(), packet.getData());
			} catch (SocketTimeoutException timeout) {

			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	public void stop() throws IOException {
		tcpServerSocket.close();
		udpServerSocket.close();
	}

	protected abstract void receiveTcp(String address, int port, InputStream inputStream);

	protected abstract void receiveUdp(String address, int port, byte[] data);

	protected void connect(Socket clientSocket) {
		clients.put((InetSocketAddress) clientSocket.getRemoteSocketAddress(), clientSocket);
	}

	protected void disconnect(InetSocketAddress address, Exception cause) {
		clients.remove(address);
		if (cause != null) {
			new Exception("Exception caused disconnect: " + cause.getMessage(), cause).printStackTrace();
		}
	}

	public final void disconnect(String address, int port, Exception cause) {
		disconnect(new InetSocketAddress(address, port), cause);
	}

	public final void disconnect(String address, int port) {
		disconnect(new InetSocketAddress(address, port), null);
	}

	public boolean isConnected(String address, int port) {
		return isConnected() && clients.containsKey(new InetSocketAddress(address, port));
	}

	public boolean isConnected() {
		return tcpServerSocket != null && udpServerSocket != null && !tcpServerSocket.isClosed()
				&& !udpServerSocket.isClosed();
	}

	public void send(String address, int port, byte[] data, boolean tcp) throws IOException {
		if (tcp) {
			clients.get(new InetSocketAddress(address, port)).getOutputStream().write(data);
		} else {
			udpServerSocket.send(
					new DatagramPacket(data, Math.min(data.length, bufferSize), InetAddress.getByName(address), port));
		}
	}

	public void send(String address, int port, byte[] data) throws IOException {
		send(address, port, data, true);
	}

	public String getAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}

	public int getPort() {
		return port;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

}