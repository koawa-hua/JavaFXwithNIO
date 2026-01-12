package org.example.ChatDemo.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class ChatServer {

    //服务器启动方法
    public void startServer() throws IOException {
        //1 创建选择器Selector
        Selector selector = Selector.open();
        //2 创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //3 为Channel通道绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8888));
        //4 设置为非阻塞模式
        serverSocketChannel.configureBlocking( false);
        //5 把内核接进来的Channel注册到选择器上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //6 循环，等待有新链接接入，根据就绪状态，调用对应的方法实现具体的业务操作
        while ( true){
            //获取Channel数量
            int select = selector.select();
            if(select == 0){
                continue;
            };
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterators = selectionKeys.iterator();
            while (iterators.hasNext()){
                SelectionKey selectionKey = iterators.next();

                //移除集合当前的SelectionKey
                iterators.remove();

                //判断就绪状态，如果是acceptable，则调用accept方法
                if(selectionKey.isAcceptable()){
                    //调用acceptOperator方法
                    acceptOperator(serverSocketChannel,selector);
                }
                else if (selectionKey.isReadable()) {
                    //如果是readable，则调用read方法
                    readOperator(selectionKey,selector);
                }
                else if (selectionKey.isWritable()) {

                }
                else if (selectionKey.isConnectable()) {

                }
                else {

                }
            }
        }
    }

    private void readOperator(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(byteBuffer);
        String message = "";

        if (read > 0) {
            byteBuffer.flip();
            message += Charset.forName("UTF-8").decode(byteBuffer).toString();
        } else if (read == -1) {
            // 客户端断开连接
            socketChannel.close();
            selectionKey.cancel();
            System.out.println("客户端断开连接");
            return;
        }

        if (!message.isEmpty()) {
            System.out.println(message);
            castOtherClient(message, selector, socketChannel);
        }

        // 重新注册通道到选择器
        socketChannel.register(selector, SelectionKey.OP_READ);
    }


    private void castOtherClient(String message, Selector selector, SocketChannel socketChannel) throws IOException {
        Set<SelectionKey> selectionKeySet = selector.keys();

        for(SelectionKey selectionKey : selectionKeySet){
            Channel channel = selectionKey.channel();
            if (channel instanceof SocketChannel castChannel) {
                if (castChannel != socketChannel && castChannel != null && castChannel.isOpen()) {
                    try {
                        castChannel.write(Charset.forName("UTF-8").encode(message));
                    } catch (IOException e) {
                        // 如果某个客户端连接断开，取消注册并关闭通道
                        selectionKey.cancel();
                        castChannel.close();
                    }
                }
            }
        }
    }


    private void acceptOperator(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        //1 接入状态，创建SocketChannel
        SocketChannel acceptedChannel = serverSocketChannel.accept();

        //2 设置为非阻塞模式
        acceptedChannel.configureBlocking(false);

        //3 注册到Selector选择器上,监听可读状态
        acceptedChannel.register(selector, SelectionKey.OP_READ);

        //4 客户端回复消息 - 添加错误处理
        try {
            acceptedChannel.write(Charset.forName("UTF-8").encode("欢迎来到聊天室"));
        } catch (IOException e) {
            System.err.println("发送欢迎消息失败: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        try {
            new ChatServer().startServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
