# Создание установщика для JavaFX приложения с использованием Inno Setup

## 1. Инструкция по созданию установщика JavaFX приложения через Inno Setup

### 1.1. Настройка проекта
1. Создайте класс Launcher для запуска приложения:
   ```java
    package com.bot.chatbot;
    
    import javafx.application.Application;
    public class Launcher {
        public static void main(String[] args){
            Application.launch(MainApp.class, args);
        }
    }

2. Установите главный класс:

Откройте File → Project Structure → Project Settings → Artifacts

В поле "Main Class" укажите созданный класс Launcher

### 1.2. Формирование артефакта
Перейдите в File → Project Structure → Artifacts

Добавьте новый артефакт:

Нажмите + → JAR → From modules with dependencies

Выберите главный класс

Активируйте Include dependencies при необходимости

### 1.3. Генерация JAR-файла

В главном меню выберите Build → Build Artifacts

Запустите сборку для вашего артефакта

Собранный JAR будет расположен в папке out/artifacts/<Название_проекта>_jar

## 2. Генерация установочного пакета в Inno Setup
### 2.1. Установка Inno Setup

Загрузите Inno Setup с официального ресурса

Выполните инсталляцию, следуя указаниям установщика

### 2.2. Создание скрипта установщика

Запустите Inno Setup и выберите Create a new script file using the Script Wizard

### 2.3. Компиляция установщика

Сохраните подготовленный скрипт (файл .iss)

Инициируйте сборку через Build → Compile

Готовый установочный файл .exe появится в директории Output