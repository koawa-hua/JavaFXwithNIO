package org.example.ChatDemo.client.Chat;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class ChatBubbleCell extends ListCell<ChatMessage> {

    @Override
    protected void updateItem(ChatMessage msg, boolean empty) {
        super.updateItem(msg, empty);

        if (empty || msg == null) {
            setGraphic(null);
            return;
        }

        Label bubble = new Label(msg.getText());
        bubble.setWrapText(true);
        bubble.setMaxWidth(380);
        bubble.setPadding(new Insets(8, 12, 8, 12));

        bubble.setStyle(msg.isSelf()
                ? "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:14 14 2 14;"
                : "-fx-background-color:#1f2937; -fx-text-fill:#e5e7eb; -fx-background-radius:14 14 14 2;"
        );

        HBox box = new HBox(bubble);
        box.setPadding(new Insets(4));

        box.setAlignment(msg.isSelf()
                ? Pos.CENTER_RIGHT
                : Pos.CENTER_LEFT
        );

        // 移除可能引起背景闪烁的动画
        // FadeTransition fade = new FadeTransition(Duration.millis(180), box);
        // fade.setFromValue(0);
        // fade.setToValue(1);
        // fade.play();

        setGraphic(box);
    }
}


