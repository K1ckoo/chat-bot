module com.bot.chatbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.bot.chatbot to javafx.fxml, javafx.graphics;
    exports com.bot.chatbot;
}