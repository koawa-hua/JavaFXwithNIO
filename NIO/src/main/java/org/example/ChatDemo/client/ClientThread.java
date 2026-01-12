package org.example.ChatDemo.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class ClientThread implements Runnable {

    private Selector selector;
    private Consumer<String> messageHandler;

    public ClientThread(Selector selector, Consumer<String> handler) {
        this.selector = selector;
        this.messageHandler = handler;
    }

    @Override
    public void run() {
        try {
            while (selector.select() > 0) {
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);

                        int bytesRead = channel.read(buffer);
                        if (bytesRead > 0) {
                            buffer.flip();
                            String msg = Charset.forName("UTF-8").decode(buffer).toString();

                            if (messageHandler != null) {
                                messageHandler.accept(msg);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

