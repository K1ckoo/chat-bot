package com.bot.chatbot;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    TextField userNameField; // Поле для ввода имени пользователя

    private MainApp mainApp; // Ссылка на главное приложение

    /**
     * Устанавливает ссылку на главное приложение
     * @param mainApp - экземпляр MainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Обрабатывает нажатие кнопки входа
     */
    @FXML
    void handleLogin() {
        String userName = userNameField.getText().trim(); // Получаем имя пользователя
        if (!userName.isEmpty()) {
            mainApp.login(userName); // Передаем в главное приложение
        }
    }
}