package com.walter.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class WebClient {

	public static void main(String[] args) throws IOException {
		try {
			ByteBuffer writeBuff = ByteBuffer.allocate(32);
			ByteBuffer readBuff = ByteBuffer.allocate(32);
			
			Selector selector = Selector.open();
			SocketChannel socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress("127.0.0.1", 8888));
			socketChannel.register(selector, SelectionKey.OP_CONNECT);

			while (true) {
				int nReady = selector.select();//Will block here
				if(nReady <= 0) {
					continue;
				}
				
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();

					if (key.isConnectable()) {
						socketChannel = (SocketChannel)key.channel();
						socketChannel.configureBlocking(false);
						if(socketChannel.isConnectionPending()) {
							socketChannel.finishConnect();
							System.out.println("[Client]已连上服务端...");
						}
						socketChannel.register(selector, SelectionKey.OP_WRITE);
					}
					
					if (key.isWritable()) {
						socketChannel = (SocketChannel) key.channel();
						writeBuff.clear();
						
						Scanner scan = new Scanner(System.in);
						String line = scan.nextLine();
						writeBuff.put(line.getBytes());
						writeBuff.flip();
						socketChannel.write(writeBuff);
						socketChannel.register(selector, SelectionKey.OP_READ);
					}
					
					if (key.isReadable()) {
						socketChannel = (SocketChannel) key.channel();
						readBuff.clear();
						socketChannel.read(readBuff);
						readBuff.flip();
						System.out.println("[Client]接收：" + new String(readBuff.array(), 0, readBuff.limit()));
						socketChannel.register(selector, SelectionKey.OP_WRITE);
					}
				}
			}
		} catch (IOException e) {
		}
	}

}
