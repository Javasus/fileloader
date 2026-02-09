# FileLoader REST API

REST API для управления файловым хранилищем с историей загрузок.

## Технологии
- Java 24
- Jakarta Servlet 6.0
- Hibernate 6.6.7
- MySQL 9.2.0
- Flyway 11.14.1 (миграции БД)
- Maven (сборка)
- Embedded Tomcat 11.0.12
- Jackson (JSON-сериализация)
- HikariCP (пул соединений)

## Сущности
### User
- `id` (Integer) - идентификатор
- `name` (String) - имя пользователя
- `events` (List<Event>) - список событий

### File
- `id` (Integer) - идентификатор
- `name` (String) - оригинальное имя файла
- `filePath` (String) - путь к физическому файлу

### Event
- `id` (Integer) - идентификатор
- `user` (User) - пользователь
- `file` (File) - файл

## API Endpoints

### Users
- `GET /api/users` - список всех пользователей
- `GET /api/users/{id}` - пользователь по ID
- `POST /api/users` - создать пользователя (JSON: `{"name": "string"}`)
- `PUT /api/users/{id}` - обновить пользователя
- `DELETE /api/users/{id}` - удалить пользователя

### Files
- `GET /api/files` - список всех файлов
- `GET /api/files/{id}` - скачать файл по ID
- `POST /api/files` - загрузить файл (multipart/form-data: `file`, `userId`(опционально))
- `PUT /api/files/{id}` - обновить файл
- `DELETE /api/files/{id}` - удалить файл

### Events
- `GET /api/events` - список всех событий
- `GET /api/events/{id}` - событие по ID

## Запуск проекта

### Требования
- Java 24
- MySQL 8+
- Maven 3.9+

### Настройка БД
1. Создать базу данных:
```sql
CREATE DATABASE fileloader CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'fileloader_user'@'localhost' IDENTIFIED BY 'fileloader_password';
GRANT ALL PRIVILEGES ON fileloader.* TO 'fileloader_user'@'localhost';
```

2. Настроить подключение в `src/main/resources/hibernate.properties`

### Сборка и запуск
```bash
mvn clean package
java -cp "target/fileloader-1.0.jar:target/lib/*" org.nosulkora.fileloader.AppRunner
```

Приложение будет доступно по адресу: `http://localhost:8080/fileloader`

## Структура проекта
```
src/main/java/org/nosulkora/fileloader/
├── controller/      # Сервисный слой (UserController, FileController, EventController)
├── servlet/         # HTTP-обработчики (UserServlet, UploadServlet, EventServlet)
├── repository/      # Паттерн Repository (интерфейсы + реализации)
├── entity/          # JPA-сущности (User, File, Event)
├── database/        # DatabaseManager, FlywayManager
├── filter/          # CorsFilter
├── utils/           # SessionManager, ServletUtils
└── AppRunner.java   # Точка входа
```

## Особенности реализации
- Использование Hibernate с аннотациями JPA
- Программная конфигурация HikariCP
- Автоматические миграции Flyway при старте
- Обработка кириллических имен файлов в заголовках
- CORS-фильтр для кросс-доменных запросов
- Единая точка управления сессиями Hibernate

## Примеры запросов (Postman)

### Создание пользователя
```http
POST http://localhost:8080/fileloader/api/users
Content-Type: application/json

{"name": "Иван Петров"}
```

### Загрузка файла
```http
POST http://localhost:8080/fileloader/api/files
Content-Type: multipart/form-data

form-data:
- file: [выбрать файл]
- userId: 1
```

### Получение истории загрузок
```http
GET http://localhost:8080/fileloader/api/events
```

## Автор
Nosulko Roman
Проект разработан в рамках обучения Java-разработке.
