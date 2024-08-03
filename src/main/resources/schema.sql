--Комaнда создания таблицы для PostgeSQL
CREATE TABLE translation_requests (
    id BIGSERIAL PRIMARY KEY,
    ip_address VARCHAR(255) NOT NULL,
    input_string TEXT NOT NULL,
    translated_string TEXT NOT NULL,
    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);