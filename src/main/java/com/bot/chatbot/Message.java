package com.bot.chatbot;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L; // Для сериализации

    private String author; // Автор сообщения
    private String text; // Текст сообщения
    private String time; // Время сообщения
    private boolean isUser; // Флаг (пользователь/бот)

    /**
     * Конструктор сообщения
     * @param author - автор сообщения
     * @param text - текст сообщения
     * @param time - время сообщения
     * @param isUser - флаг (пользователь/бот)
     */
    public Message(String author, String text, String time, boolean isUser) {
        this.author = author;
        this.text = text;
        this.time = time;
        this.isUser = isUser;
    }

    // Геттеры
    public String getAuthor() { return author; }
    public String getText() { return text; }
    public String getTime() { return time; }
    public boolean isUser() { return isUser; }

    // Строковое представление сообщения
    @Override
    public String toString() {
        return "[" + time + "] " + author + ": " + text;
    }
}