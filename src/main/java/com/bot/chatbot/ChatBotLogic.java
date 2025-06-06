package com.bot.chatbot;

import javafx.scene.control.ListView;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBotLogic implements IBot {
    private static final String HISTORY_DIR = "user_histories"; // Директория для хранения истории
    private static final String CURRENCY_API_URL = "https://api.exchangerate-api.com/v4/latest/USD"; // API курсов валют
    private static final String API_KEY = ""; // Ключ API

    // Популярные валюты для отображения
    private static final String[] POPULAR_CURRENCIES = {
            "USD", "EUR", "GBP", "JPY", "CNY",
            "CHF", "CAD", "AUD", "NZD", "TRY"
    };

    private String userName; // Имя пользователя
    private List<Message> messageHistory; // История сообщений
    private Pattern currencyPattern; // Регулярное выражение для запроса курса валют

    public ChatBotLogic(String userName) {
        this.userName = userName;
        this.messageHistory = new ArrayList<>();
        // Паттерн для распознавания запроса курса валют (регистронезависимый)
        this.currencyPattern = Pattern.compile("(?i)(курс валют|exchange rates)");
        createHistoryDirectory(); // Создаем директорию для истории
    }

    // Создает директорию для хранения истории чатов
    private void createHistoryDirectory() {
        try {
            Path path = Paths.get(HISTORY_DIR);
            if (!Files.exists(path)) {
                Files.createDirectory(path); // Создаем, если не существует
            }
        } catch (IOException e) {
            System.err.println("Ошибка при создании директории истории: " + e.getMessage());
        }
    }

    // Основной метод обработки сообщений
    @Override
    public String getResponse(String message) {
        // Проверяем команды и возвращаем соответствующий ответ
        if (message.equalsIgnoreCase("/help")) {
            return getHelpCommands();
        } else if (message.equalsIgnoreCase("Который час?")) {
            return getCurrentTime();
        } else if (message.matches("\\d+\\s*\\*\\s*\\d+")) { // Проверка на умножение
            return handleMultiplication(message);
        } else if (message.equalsIgnoreCase("привет") || message.equalsIgnoreCase("здравствуйте")) {
            return getGreeting();
        } else if (currencyPattern.matcher(message).matches()) { // Проверка запроса курса валют
            return handleCurrencyRequest();
        }
        return getDefaultResponse(); // Ответ по умолчанию
    }

    // Возвращает текущее время
    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return "Сейчас " + LocalTime.now().format(formatter);
    }

    // Обрабатывает операцию умножения
    private String handleMultiplication(String equation) {
        try {
            String[] parts = equation.split("\\s*\\*\\s*"); // Разделяем по *
            int num1 = Integer.parseInt(parts[0].trim());
            int num2 = Integer.parseInt(parts[1].trim());
            int result = num1 * num2;
            return String.format("%d * %d = %d", num1, num2, result);
        } catch (Exception e) {
            return "Ошибка: введите уравнение в формате 'число * число' (например: 12 * 4)";
        }
    }

    // Возвращает приветственное сообщение
    private String getGreeting() {
        return "Привет, " + userName + "! Напишите /help для списка команд.";
    }

    // Ответ по умолчанию
    private String getDefaultResponse() {
        return "Не понимаю. Напишите /help для списка команд.";
    }

    // Обрабатывает запрос курса валют
    private String handleCurrencyRequest() {
        try {
            String response = getCurrencyRates(); // Получаем данные от API
            if (response != null) {
                return buildCurrencyRatesMessage(response); // Форматируем ответ
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении курса валют: " + e.getMessage());
        }
        return "Не удалось получить курс валют. Попробуйте позже.";
    }

    // Формирует сообщение с курсами валют
    private String buildCurrencyRatesMessage(String json) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 Курсы валют к RUB:\n\n");

        // Получаем курс USD к RUB
        double usdToRub = parseCurrencyRate(json, "RUB");
        if (usdToRub == 0) {
            return "⚠️ Не удалось получить курс USD/RUB";
        }

        // Формируем строки для каждой валюты
        for (String currency : POPULAR_CURRENCIES) {
            if (currency.equals("USD")) {
                sb.append(String.format("🇺🇸 USD/RUB: %.2f\n", usdToRub));
                continue;
            }

            double currencyToUsd = parseCurrencyRate(json, currency);
            if (currencyToUsd > 0) {
                double currencyToRub = usdToRub / currencyToUsd;
                sb.append(String.format(getCurrencyFlag(currency) + " %s/RUB: %.2f\n",
                        currency, currencyToRub));
            }
        }

        // Добавляем время обновления
        sb.append("\n⏳ Данные на " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        return sb.toString();
    }

    // Возвращает флаг страны для валюты
    String getCurrencyFlag(String currency) {
        switch (currency) {
            case "EUR": return "🇪🇺";
            case "GBP": return "🇬🇧";
            case "JPY": return "🇯🇵";
            case "CNY": return "🇨🇳";
            case "CHF": return "🇨🇭";
            case "CAD": return "🇨🇦";
            case "AUD": return "🇦🇺";
            case "NZD": return "🇳🇿";
            case "TRY": return "🇹🇷";
            default: return "";
        }
    }

    // Получает данные о курсах валют от API
    private String getCurrencyRates() throws IOException {
        URL url = new URL(CURRENCY_API_URL + "?apikey=" + API_KEY);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // Таймаут подключения 5 сек
        connection.setReadTimeout(5000); // Таймаут чтения 5 сек

        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine); // Читаем ответ
                    }
                }
                return response.toString();
            }
        } finally {
            connection.disconnect(); // Закрываем соединение
        }
        return null;
    }

    // Парсит курс валюты из JSON
    double parseCurrencyRate(String json, String currency) {
        Pattern pattern = Pattern.compile("\"" + currency + "\":(\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;
    }

    // Возвращает список доступных команд
    @Override
    public String getHelpCommands() {
        return """
            📋 Доступные команды:
            
            Основные:
            • /help - показать это сообщение
            • Привет - поздороваться с ботом
            • Который час? - текущее время
            
            Калькулятор:
            • число * число - умножение (напр: 5 * 3)
            
            Финансы:
            • курс валют - курсы 10 валют к рублю
            
            Для выхода закройте окно чата.""";
    }

    // Сохраняет сообщение в историю
    @Override
    public void saveMessage(Message message) {
        messageHistory.add(message);
    }

    // Загружает историю сообщений из файла
    @Override
    public void loadHistoryFromFile(ListView<Message> messageListView) {
        String filename = HISTORY_DIR + File.separator + "history_" + userName + ".dat";
        File file = new File(filename);

        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Message> history = (List<Message>) ois.readObject();
                messageHistory = history;
                messageListView.getItems().addAll(history); // Добавляем в ListView
                return;
            } catch (Exception e) {
                System.err.println("Ошибка загрузки истории: " + e.getMessage());
            }
        }

        // Если файла нет или ошибка чтения - создаем новую историю
        messageHistory = new ArrayList<>();
        Message welcomeMessage = new Message("Бот",
                "Привет, " + userName + "! Я чат-бот.\nНапишите /help для списка команд.",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), false);
        messageHistory.add(welcomeMessage);
        messageListView.getItems().add(welcomeMessage);
    }

    // Сохраняет историю сообщений в файл
    @Override
    public void saveHistoryToFile() {
        String filename = HISTORY_DIR + File.separator + "history_" + userName + ".dat";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(messageHistory); // Сериализуем историю
        } catch (IOException e) {
            System.err.println("Ошибка сохранения истории: " + e.getMessage());
        }
    }
}