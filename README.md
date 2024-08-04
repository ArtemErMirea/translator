# Translator Application

## Описание
Это приложение выполняет перевод набора слов с одного языка на другой с использованием внешнего сервиса перевода. В качестве сервиса используется Google Translate через Rapid API https://rapidapi.com/IRCTCAPI/api/google-translator9/playground/
Если ключ потребуется заменить - нужно просто зарегистрироваться на Rapid API и подписаться бесплатно на Google Translate API  и задайте его в `rapidapi.key` в `application.properties`. Пример заполнения можно увидеть в `application.properties.example`
## Запуск приложения

### Требования
- JDK 21
- Maven

### Запуск
1. Клонируйте репозиторий:
    ```sh
    git clone <URL>
    cd translator
    ```
2. Настройте подключение к API:
   Получите ключ (см. описание) и внестие его в файл `application.properties`:
    ```properties
    spring.application.name=translator
    
    rapidapi.key=YOUR_KEY
    baseUrl = https://google-translator9.p.rapidapi.com/v2
    languageUrl = https://google-translator9.p.rapidapi.com/v2/languages
    ```
4. Настройте подключение к базе данных PostgreSQL:
    - Создайте таблицу для хранения истории переводов:
        ```sql
        CREATE TABLE translation_requests (
            id BIGSERIAL PRIMARY KEY,
            ip_address VARCHAR(255) NOT NULL,
            input_string TEXT NOT NULL,
            translated_string TEXT NOT NULL,
            request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        ```
    - Заполните файл `application.properties`.:
        ```properties
        
        spring.datasource.url=jdbc:postgresql://localhost:5432/YOUR_DB
        spring.datasource.username=YOUR_USERNAME
        spring.datasource.password=YOUR_PASSWORD
        spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
        ```

5. Соберите проект и запустите:
    ```sh
    ./mvnw spring-boot:run
    ```

6. Приложение будет доступно по адресу `http://localhost:8080`.
## Использование
Для выполнения запроса на перевод, отправьте POST-запрос на `/translate`с телом вида
```sh
{
  "q": "Hello world, this is my first program",
  "source": "en",
  "target": "ru",
  "format": "text"
}
```
Чтобы получить список доступных языков, отправьте Get-запрос на `/languages`

Пример запроса:
```sh
curl --location 'http://localhost:8080/translate' \
--header 'Content-Type: application/json' \
--data '{
  "q": "один два три четыре пять шесть семь восемь девять десять одинадцать двенадцать тринадцать четырнадцать пятнадцать шеснадцать семнадцать восемнадцать девятнадцать двадцать",
  "source": "ru",
  "target": "de",
  "format": "text"
}'
```
```sh
curl --location 'http://localhost:8080/languages'
```
## Примеры работы:
### Перевод на Русский, как видите, каждое слово переведено отдельно

![image](https://github.com/user-attachments/assets/0162836b-e4cf-4e9e-9e30-be782ebb4343)

### Перевод на Китайский

![image](https://github.com/user-attachments/assets/1863bfb7-887a-4000-9680-56bb4a798856)

### Если ввести неправильный исходный язык:

![image](https://github.com/user-attachments/assets/495ec2fc-9407-41db-95c9-76bb738d3c94)

###  Если ввести неправильный язык перевода:

![image](https://github.com/user-attachments/assets/2a7b3ce5-f0e5-40c9-a323-00b55ddde171)

### Get списка доступных языков
![image](https://github.com/user-attachments/assets/67f5535b-e73a-456f-a763-9f3b749cc125)


### Таблица:
![image](https://github.com/user-attachments/assets/dae03cd5-735a-466c-a1cd-9dcc50b50bee)





