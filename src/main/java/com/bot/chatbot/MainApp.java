package com.bot.chatbot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {
    private Stage primaryStage; // Главное окно приложения
    private String userName; // Имя текущего пользователя

    // Точка входа JavaFX приложения
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Сохраняем ссылку на главное окно
        showLoginWindow(); // Показываем окно входа
    }

    // Показывает окно входа
    private void showLoginWindow() {
        try {
            // Загружаем FXML-файл с интерфейсом входа
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bot/chatbot/Login.fxml"));
            Scene scene = new Scene(loader.load()); // Создаем сцену
            LoginController controller = loader.getController(); // Получаем контроллер
            controller.setMainApp(this); // Передаем ссылку на MainApp в контроллер

            primaryStage.setTitle("Вход в чат-бот"); // Устанавливаем заголовок окна
            // Загружаем иконку приложения
            URL iconUrl = getClass().getResource("/image.png");
            if (iconUrl != null) {
                Image appIcon = new Image(iconUrl.toString());
                primaryStage.getIcons().add(appIcon);
            }
            primaryStage.setScene(scene); // Устанавливаем сцену
            primaryStage.setResizable(false); // Запрещаем изменение размера окна
            primaryStage.show(); // Показываем окно
        } catch (IOException e) {
            e.printStackTrace(); // Обработка ошибок загрузки
        }
    }

    // Обрабатывает вход пользователя
    public void login(String userName) {
        this.userName = userName; // Сохраняем имя пользователя
        showChatWindow(); // Показываем окно чата
    }

    // Показывает окно чата
    private void showChatWindow() {
        try {
            // Загружаем FXML-файл с интерфейсом чата
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bot/chatbot/Chat.fxml"));
            Scene scene = new Scene(loader.load());
            ChatController controller = loader.getController();
            // Настраиваем контроллер чата
            controller.setMainApp(this);
            controller.setPrimaryStage(primaryStage);
            controller.setUserName(userName);
            controller.initChat(); // Инициализируем чат

            // Устанавливаем иконку (аналогично окну входа)
            URL iconUrl = getClass().getResource("/image.png");
            if (iconUrl != null) {
                Image appIcon = new Image(iconUrl.toString());
                primaryStage.getIcons().add(appIcon);
            }

            primaryStage.setTitle("Чат с ботом - " + userName); // Обновляем заголовок
            primaryStage.setScene(scene); // Устанавливаем новую сцену
            primaryStage.show(); // Показываем окно
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Возвращает имя текущего пользователя
    public String getUserName() {
        return userName;
    }

    // Точка входа в приложение
    public static void main(String[] args) {
        launch(args); // Запуск JavaFX приложения
    }
}