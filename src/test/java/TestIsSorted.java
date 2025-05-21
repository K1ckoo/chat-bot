package com.bot.chatbot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class TestIsSorted {
    private ChatBotLogic bot;
    private static final String TEST_USER = "TestUser";
    private static final String TEST_HISTORY_FILE = "user_histories/history_" + TEST_USER + ".dat";

    @BeforeEach
    void setUp() {
        bot = new ChatBotLogic(TEST_USER);
        // Удаляем тестовый файл истории перед каждым тестом
        try {
            Files.deleteIfExists(Paths.get(TEST_HISTORY_FILE));
        } catch (IOException e) {
            System.err.println("Ошибка удаления тестового файла: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // Удаляем тестовый файл истории после каждого теста
        try {
            Files.deleteIfExists(Paths.get(TEST_HISTORY_FILE));
        } catch (IOException e) {
            System.err.println("Ошибка удаления тестового файла: " + e.getMessage());
        }
    }

    // Тесты для ChatBotLogic
    @Test
    void testGetResponse_HelpCommand() {
        String response = bot.getResponse("/help");
        assertTrue(response.contains("Доступные команды"));
        assertTrue(response.contains("/help"));
        assertTrue(response.contains("курс валют"));
    }

    @Test
    void testGetResponse_Multiplication() {
        assertEquals("5 * 3 = 15", bot.getResponse("5 * 3"));
        assertEquals("10 * 10 = 100", bot.getResponse("10*10"));
        assertTrue(bot.getResponse("5 * abc").contains("Ошибка"));
    }

    @Test
    void testGetResponse_TimeRequest() {
        String response = bot.getResponse("Который час?");
        assertTrue(response.startsWith("Сейчас"));
        // Проверяем формат времени (HH:mm)
        assertTrue(response.matches("Сейчас \\d{2}:\\d{2}"));
    }

    // Тесты для работы с историей сообщений
    @Test
    void testSaveAndLoadHistory() {
        ListView<Message> listView = new ListView<>();

        // Проверяем загрузку пустой истории (должно создать приветственное сообщение)
        bot.loadHistoryFromFile(listView);
        ObservableList<Message> messages = listView.getItems();
        assertEquals(1, messages.size());
        assertFalse(messages.get(0).isUser());
        assertTrue(messages.get(0).getText().contains("Привет"));

        // Добавляем тестовые сообщения
        Message userMsg = new Message(TEST_USER, "Тест", "12:00", true);
        Message botMsg = new Message("Бот", "Ответ", "12:01", false);

        bot.saveMessage(userMsg);
        bot.saveMessage(botMsg);
        bot.saveHistoryToFile();

        // Очищаем и загружаем снова
        listView.getItems().clear();
        bot.loadHistoryFromFile(listView);

        assertEquals(3, listView.getItems().size()); // Приветствие + 2 сообщения
        assertEquals("Тест", listView.getItems().get(1).getText());
        assertEquals("Ответ", listView.getItems().get(2).getText());
    }

    @Test
    void testHistoryFileOperations() {
        // Проверяем создание директории
        Path dirPath = Paths.get("user_histories");
        assertTrue(Files.exists(dirPath));
        assertTrue(Files.isDirectory(dirPath));

        // Проверяем создание файла истории
        bot.saveHistoryToFile();
        assertTrue(Files.exists(Paths.get(TEST_HISTORY_FILE)));
    }

    // Тесты для Message
    @Test
    void testMessageCreation() {
        Message msg = new Message("User", "Hello", "12:00", true);

        assertEquals("User", msg.getAuthor());
        assertEquals("Hello", msg.getText());
        assertEquals("12:00", msg.getTime());
        assertTrue(msg.isUser());

        Message botMsg = new Message("Bot", "Hi", "12:01", false);
        assertFalse(botMsg.isUser());
    }

    @Test
    void testMessageToString() {
        Message msg = new Message("User", "Test", "12:00", true);
        assertEquals("[12:00] User: Test", msg.toString());
    }

    // Тесты для валютных функций (моки)
    @Test
    void testCurrencyFunctions() {
        // Тестовый JSON ответ от API
        String testJson = "{\"USD\":1.0,\"EUR\":0.85,\"GBP\":0.75,\"RUB\":75.5}";

        // Проверяем парсинг курсов
        assertEquals(0.85, bot.parseCurrencyRate(testJson, "EUR"), 0.001);
        assertEquals(75.5, bot.parseCurrencyRate(testJson, "RUB"), 0.001);
        assertEquals(0.0, bot.parseCurrencyRate(testJson, "JPY"), 0.001); // Несуществующая валюта

        // Проверяем флаги валют
        assertEquals("🇪🇺", bot.getCurrencyFlag("EUR"));
        assertEquals("🇬🇧", bot.getCurrencyFlag("GBP"));
        assertEquals("", bot.getCurrencyFlag("USD")); // Для USD флага нет
    }

    // Тесты для LoginController (имитация)
    @Test
    void testLoginController() {
        MainApp mainApp = new MainApp();
        LoginController controller = new LoginController();
        controller.setMainApp(mainApp);

        // Имитация ввода имени пользователя
        controller.userNameField = new javafx.scene.control.TextField();
        controller.userNameField.setText(TEST_USER);

        // Проверяем обработку входа
        assertDoesNotThrow(() -> controller.handleLogin());
        assertEquals(TEST_USER, mainApp.getUserName());

        // Проверяем обработку пустого имени
        controller.userNameField.setText("");
        assertDoesNotThrow(() -> controller.handleLogin());
    }

    // Тесты для ChatController (имитация)
    @Test
    void testChatController() {
        MainApp mainApp = new MainApp();
        mainApp.login(TEST_USER);

        ChatController controller = new ChatController();
        controller.setMainApp(mainApp);
        controller.setUserName(TEST_USER);
        controller.messageListView = new ListView<>();
        controller.inputTextArea = new javafx.scene.control.TextArea();

        // Имитация инициализации чата
        assertDoesNotThrow(() -> controller.initChat());
        assertFalse(controller.messageListView.getItems().isEmpty());

        // Имитация отправки сообщения
        controller.inputTextArea.setText("Привет");
        assertDoesNotThrow(() -> controller.sendMessage());
        assertEquals(2, controller.messageListView.getItems().size()); // Приветствие + ответ

        // Имитация очистки чата
        assertDoesNotThrow(() -> controller.clearChat());
        assertEquals(1, controller.messageListView.getItems().size()); // Только приветствие
    }
}