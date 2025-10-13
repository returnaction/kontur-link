package com.nikita.linkservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LinkServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkServiceApplication.class, args);
    }

}
//TODO сделать что бы генерировался новая short link пока мы не сохраним ее в бд
// TODO проверять если у нас уже есть такая ссылка перед сохранением и если есть возвращать ее копию
// TODO  сделать что бы генерирование случайной ссылки было макс 10 символов большие маленькие + числа
// TODO exception Handler
