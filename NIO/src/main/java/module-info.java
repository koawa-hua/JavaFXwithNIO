module org.example.nio {
    requires javafx.controls;
    requires javafx.fxml;
    requires junit;
    requires java.xml.crypto;


    opens org.example.nio to javafx.fxml;
    exports org.example.nio;
    // 导出包含 JavaFX Application 的包
    exports org.example.ChatDemo.client;


}