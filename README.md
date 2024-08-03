# Translator Application

## Описание
Это приложение выполняет перевод набора слов с одного языка на другой с использованием внешнего сервиса перевода.В качестве сервиса используется Google Translate через Rapid API

## Запуск приложения

### Требования
- JDK 21
- Maven

### Шаги для запуска
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

## Использование
Для выполнения запроса на перевод, отправьте POST-запрос на `/translate`с телом вида
{
  "q": "Hello world, this is my first program",
  "source": "en",
  "target": "ru",
  "format": "text"
}

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

curl --location 'http://localhost:8080/languages'
