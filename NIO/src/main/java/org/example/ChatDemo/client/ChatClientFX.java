package org.example.ChatDemo.client;

import org.example.ChatDemo.client.Chat.ChatBubbleCell;
import org.example.ChatDemo.client.Chat.ChatMessage;
import org.example.ChatDemo.client.ClientThread;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class ChatClientFX extends Application {

    private ListView<ChatMessage> chatList;
    private TextField inputField;
    private TextField nameField;
    private SocketChannel socketChannel;
    private Button connectBtn; // 添加成员变量
    private Button sendBtn;   // 添加成员变量

    @Override
    public void start(Stage stage) {
        // 初始化所有UI组件
        initializeComponents();

        // 构建界面布局
        BorderPane root = buildLayout();

        // 创建场景
        Scene scene = new Scene(root, 640, 480);
        scene.getStylesheets().add(
                getClass().getResource("/style/chat-dark.css").toExternalForm()
        );

        // 设置舞台属性
        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();
    }

    private void initializeComponents() {
        // ===== 聊天列表（核心）=====
        chatList = new ListView<>();
        chatList.setCellFactory(v -> new ChatBubbleCell());
        chatList.getStyleClass().add("chat-list");

        // ===== 顶部（用户名 + 连接）=====
        nameField = new TextField();
        nameField.setPromptText("用户名");

        connectBtn = new Button("连接"); // 修改为成员变量赋值
        connectBtn.setOnAction(e -> connect());

        // ===== 底部（输入 + 发送）=====
        inputField = new TextField();
        inputField.setPromptText("输入消息");

        sendBtn = new Button("发送"); // 修改为成员变量赋值
        sendBtn.setOnAction(e -> send());
    }

    private BorderPane buildLayout() {
        HBox topBar = new HBox(10, nameField, connectBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        HBox bottomBar = new HBox(10, inputField, sendBtn);
        bottomBar.setPadding(new Insets(10));
        bottomBar.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(chatList);
        root.setBottom(bottomBar);

        return root;
    }

    // ===== 连接服务器 =====
    private void connect() {
        try {
            socketChannel = SocketChannel.open(
                    new InetSocketAddress("127.0.0.1", 8888)
            );
            socketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);

            // 启动监听线程
            Platform.runLater(() -> {
                new Thread(new ClientThread(selector, msg ->
                        Platform.runLater(() -> addMessage(msg, false))
                )).start();
            });

            Platform.runLater(() -> {
                addSystemMessage("已连接服务器");
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                addSystemMessage("连接失败");
            });
        }
    }

    // ===== 发送消息 =====
    private void send() {
        try {
            String name = nameField.getText();
            String msg = inputField.getText();

            if (msg == null || msg.isEmpty()) return;

            // ① 本地立即显示
            addMessage("我: " + msg, true);

            // ② 发送给服务器
            if (socketChannel != null && socketChannel.isConnected()) {
                socketChannel.write(
                        Charset.forName("UTF-8")
                                .encode(name + ": " + msg)
                );
            } else {
                Platform.runLater(() -> {
                    addSystemMessage("未连接到服务器");
                });
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                addSystemMessage("发送失败: " + e.getMessage());
            });
        } finally {
            // 确保无论是否发生异常都清空输入框
            Platform.runLater(() -> inputField.clear());
        }
    }

    // ===== UI 添加消息 =====
    private void addMessage(String text, boolean self) {
        // 确保只有在JavaFX应用程序线程中才更新UI
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> addMessage(text, self));
            return;
        }

        chatList.getItems().add(new ChatMessage(text, self));
        chatList.scrollTo(chatList.getItems().size() - 1);
    }

    private void addSystemMessage(String text) {
        Platform.runLater(() -> {
            chatList.getItems().add(
                    new ChatMessage("[系统] " + text, false)
            );
            chatList.scrollTo(chatList.getItems().size() - 1);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
