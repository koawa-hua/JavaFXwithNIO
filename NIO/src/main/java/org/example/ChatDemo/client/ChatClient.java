package org.example.ChatDemo.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class ChatClient {

    public void startClient(String name) throws IOException {

        //获取SocketChannel连接服务器
        SocketChannel socketChannel =
                SocketChannel.open(new InetSocketAddress("127.0.0.1",8888));

        //接收服务端响应数据
        Selector selector = Selector.open();
        socketChannel.configureBlocking( false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        //创建线程
        new Thread(new ClientThread(selector,null)).start();

        //向服务器端发送消息
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()){
            String msg = sc.nextLine();
            if (!msg.isEmpty()){
                socketChannel.write(Charset.forName("UTF-8").encode(name+":"+msg));
            }
        }

    }
}
