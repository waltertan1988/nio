package com.walter.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class WebServer {
	public static void main(String[] args) {
		try {
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8888));
			ssc.configureBlocking(false);

			Selector selector = Selector.open();
			// 注册 channel，并且指定感兴趣的事件是 Accept
			ssc.register(selector, SelectionKey.OP_ACCEPT);

			ByteBuffer readBuff = ByteBuffer.allocate(32);
			ByteBuffer writeBuff = ByteBuffer.allocate(32);

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

					if (key.isAcceptable()) {
						// 创建新的连接，并且把连接注册到selector上，而且，声明这个channel只对读操作感兴趣。
						SocketChannel socketChannel = ssc.accept();
						socketChannel.configureBlocking(false);
						socketChannel.register(selector, SelectionKey.OP_READ);
						System.out.println("[Server]连接了一个客户端...");
					}
					
					if (key.isReadable()) {
						SocketChannel socketChannel = (SocketChannel) key.channel();
						readBuff.clear();
						socketChannel.read(readBuff);

						readBuff.flip();
						System.out.println("[Server]接收：" + new String(readBuff.array(), 0, readBuff.limit()));
						socketChannel.register(selector, SelectionKey.OP_WRITE);
					}
					
					if (key.isWritable()) {
						SocketChannel socketChannel = (SocketChannel) key.channel();
						writeBuff.clear();
						writeBuff.put("[Server]收到了".getBytes());
						writeBuff.flip();
						socketChannel.write(writeBuff);
						socketChannel.register(selector, SelectionKey.OP_READ);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
