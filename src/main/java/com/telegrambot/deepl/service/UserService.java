package com.telegrambot.deepl.service;

import com.telegrambot.deepl.repository.UserRepository;
import com.telegrambot.deepl.repository.UserRepositoryInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;
import java.util.Optional;


@Slf4j
@Component
public class UserService {

    @Autowired
    private UserRepositoryInterface userRepositoryInterface;

    public void registerUser(Message msg) {
        if (userRepositoryInterface.findById(msg.getChatId()).isEmpty()) {
            UserRepository userRepository = new UserRepository();
            getUserInfo(msg, userRepository);

            userRepositoryInterface.save(userRepository);
            log.info("User saved: " + userRepository);
        }
    }

    public void deleteUser(Message msg) {
        Optional<UserRepository> optionalUserRepository = userRepositoryInterface.findById(msg.getChatId());
        if (optionalUserRepository.isPresent()) {
            UserRepository userRepository = optionalUserRepository.get();

            userRepositoryInterface.delete(userRepository);
            log.info("User deleted: " + userRepository);
        }
    }

    private void getUserInfo(Message msg, UserRepository userRepository) {
        var chatId = msg.getChatId();
        var chat = msg.getChat();

        userRepository.setChatId(chatId);
        userRepository.setFirstName(chat.getFirstName());
        userRepository.setLastName(chat.getLastName());
        userRepository.setUserName(chat.getUserName());
        userRepository.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
    }
}
