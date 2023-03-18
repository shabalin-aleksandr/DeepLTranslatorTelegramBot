package com.telegrambot.deepl.service;

import com.telegrambot.deepl.repository.UserRepository;
import com.telegrambot.deepl.repository.UserRepositoryInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;

@Slf4j
@Component
public class UserService {

    @Autowired
    private UserRepositoryInterface userRepositoryInterface;

    public void registerUser(Message msg) {
        if (userRepositoryInterface.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            UserRepository userRepository = new UserRepository();

            userRepository.setChatId(chatId);
            userRepository.setFirstName(chat.getFirstName());
            userRepository.setLastName(chat.getLastName());
            userRepository.setUserName(chat.getUserName());
            userRepository.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepositoryInterface.save(userRepository);
            log.info("User saved: " + userRepository);
        }
    }

    public void deleteUser(Message msg) {
        var chatId = msg.getChatId();

        UserRepository userRepository = new UserRepository();
        userRepository.setChatId(chatId);

        userRepositoryInterface.delete(userRepository);
        log.info("User deleted: " + userRepository);

    }
}