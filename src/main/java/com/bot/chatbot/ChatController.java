package com.bot.chatbot;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatController {
    @FXML
    ListView<Message> messageListView; // Список сообщений
    @FXML
    TextArea inputTextArea; // Поле ввода сообщения
    @FXML private MenuItem clearChatMenuItem; // Пункт меню "Очистить чат"

    private MainApp mainApp; // Ссылка на главное приложение
    private ChatBotLogic chatBotLogic; // Логика чат-бота
    private Stage primaryStage; // Главное окно

    // Устанавливает ссылку на главное приложение
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    // Устанавливает главное окно
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Устанавливает имя пользователя и инициализирует логику бота
    public void setUserName(String userName) {
        this.chatBotLogic = new ChatBotLogic(userName);
    }

    // Инициализирует чат
    public void initChat() {
        messageListView.setCellFactory(param -> new MessageCell()); // Настраиваем отображение сообщений
        inputTextArea.setOnKeyPressed(this::handleKeyPress); // Обработка нажатия клавиш
        clearChatMenuItem.setOnAction(event -> clearChat()); // Обработка очистки чата
        loadHistory(); // Загружаем историю
        if (messageListView.getItems().isEmpty()) {
            addWelcomeMessage(); // Добавляем приветствие, если чат пуст
        }
    }

    // Обрабатывает нажатие клавиш в поле ввода
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
            event.consume(); // Предотвращаем перенос строки
            sendMessage(); // Отправляем сообщение
        }
    }

    // Обрабатывает клик по кнопке отправки
    @FXML
    private void handleSendButton(MouseEvent event) {
        sendMessage();
    }

    // Отправляет сообщение
    void sendMessage() {
        String text = inputTextArea.getText().trim();
        if (!text.isEmpty()) {
            // Создаем и добавляем сообщение пользователя
            Message userMessage = createMessage(text, true);
            addMessageToChat(userMessage);
            // Получаем и добавляем ответ бота
            String botResponse = chatBotLogic.getResponse(text);
            Message botMessage = createMessage(botResponse, false);
            addMessageToChat(botMessage);
            inputTextArea.clear(); // Очищаем поле ввода
            scrollToBottom(); // Прокручиваем вниз
        }
    }

    // Создает объект сообщения
    private Message createMessage(String text, boolean isUser) {
        return new Message(
                isUser ? mainApp.getUserName() : "Бот", // Автор
                text, // Текст
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), // Время
                isUser // Флаг пользователя/бота
        );
    }

    // Добавляет сообщение в чат и сохраняет его
    private void addMessageToChat(Message message) {
        messageListView.getItems().add(message); // Добавляем в ListView
        chatBotLogic.saveMessage(message); // Сохраняем в логике
        chatBotLogic.saveHistoryToFile(); // Сохраняем в файл
    }

    // Добавляет приветственное сообщение
    private void addWelcomeMessage() {
        Message welcomeMessage = createMessage(
                "Привет, " + mainApp.getUserName() + "! Я чат-бот.\nНапишите /help для списка команд.",
                false
        );
        addMessageToChat(welcomeMessage);
    }

    // Очищает чат
    void clearChat() {
        messageListView.getItems().clear(); // Очищаем ListView
        addWelcomeMessage(); // Добавляем приветствие
    }

    // Загружает историю сообщений
    private void loadHistory() {
        chatBotLogic.loadHistoryFromFile(messageListView);
    }

    // Прокручивает чат вниз
    private void scrollToBottom() {
        int lastIndex = messageListView.getItems().size() - 1;
        if (lastIndex >= 0) {
            messageListView.scrollTo(lastIndex);
        }
    }

    // Завершает работу
    public void shutdown() {
        chatBotLogic.saveHistoryToFile(); // Сохраняем историю
        primaryStage.close(); // Закрываем окно
    }

    // Внутренний класс для отображения сообщений
    private static class MessageCell extends ListCell<Message> {
        // Обновляет содержимое ячейки
        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null); // Очищаем, если пусто
            } else {
                // Создаем элементы интерфейса для сообщения
                Text time = new Text("[" + item.getTime() + "] ");
                time.setStyle("-fx-fill: #666666; -fx-font-size: 10px;");

                Text author = new Text(item.getAuthor() + ": ");
                author.setStyle(item.isUser() ?
                        "-fx-fill: #2b5278; -fx-font-weight: bold;" : // Стиль для пользователя
                        "-fx-fill: #784e2b; -fx-font-weight: bold;"); // Стиль для бота

                Text content = new Text(item.getText());
                content.setStyle("-fx-fill: #333333;");

                HBox container = new HBox(5); // Контейнер для элементов
                container.setStyle(item.isUser() ?
                        "-fx-background-color: #e3f2fd; -fx-background-radius: 5; -fx-padding: 5;" : // Фон для пользователя
                        "-fx-background-color: #f5f5f5; -fx-background-radius: 5; -fx-padding: 5;"); // Фон для бота

                container.getChildren().addAll(time, author, content);
                setGraphic(container); // Устанавливаем контейнер в ячейку
            }
        }
    }
}