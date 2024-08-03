# Translator Application

## Описание
Это приложение выполняет перевод набора слов с одного языка на другой с использованием внешнего сервиса перевода. В качестве сервиса используется Google Translate через Rapid API https://rapidapi.com/IRCTCAPI/api/google-translator9/playground/
Если ключ потребуется заменить - просто зарегестрируйтесь на Rapid API и подписаться бесплатно на  Google Translate API  и задайте его в `rapidapi.key` в `application.properties`. Пример заполнения можно увидеть в `application.properties.example`
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

2. Соберите проект и запустите:
    ```sh
    ./mvnw spring-boot:run
    ```

3. Приложение будет доступно по адресу `http://localhost:8080`.
4. Комaнда создания таблицы для PostgeSQL. В эту таблицу будет записываться история переводов
```sql
CREATE TABLE translation_requests (
    id BIGSERIAL PRIMARY KEY,
    ip_address VARCHAR(255) NOT NULL,
    input_string TEXT NOT NULL,
    translated_string TEXT NOT NULL,
    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
```properties
# Подключение подпишите в файле application.properties:
spring.datasource.url=jdbc:postgresql://localhost:5432/YOUR_DB
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```
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

curl --location 'http://localhost:8080/languages'
