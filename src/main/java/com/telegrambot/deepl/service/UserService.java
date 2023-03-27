/*
- Copyright 2023 Aleksandr Shabalin
-
- Licensed under the Apache License, Version 2.0 (the "License");
- you may not use this file except in compliance with the License.
- You may obtain a copy of the License at
-
- `<http://www.apache.org/licenses/LICENSE-2.0>`
-
- Unless required by applicable law or agreed to in writing, software
- distributed under the License is distributed on an "AS IS" BASIS,
- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
- See the License for the specific language governing permissions and
- limitations under the License.
*/

package com.telegrambot.deepl.service;

import com.telegrambot.deepl.model.LanguagePair;
import com.telegrambot.deepl.repository.UserRepository;
import com.telegrambot.deepl.repository.UserRepositoryInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Service
public class UserService {

    @Autowired
    private UserRepositoryInterface userRepositoryInterface;

    private final Map<Integer, LanguagePair> userLanguagePreferences = new ConcurrentHashMap<>();

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

    public LanguagePair getUserLanguages(int userId) {
        return userLanguagePreferences.get(userId);
    }

    public void setUserLanguages(int userId, String sourceLanguage, String targetLanguage) {
        userLanguagePreferences.put(userId, new LanguagePair(sourceLanguage, targetLanguage));
    }

    public boolean isLanguagePairSet(Long userId) {
        LanguagePair languagePair = getUserLanguages(Math.toIntExact(userId));
        return languagePair != null;
    }
}
