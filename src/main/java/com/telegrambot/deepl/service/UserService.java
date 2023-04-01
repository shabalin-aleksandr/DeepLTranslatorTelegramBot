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

import com.telegrambot.deepl.config.ChatIdHolder;
import com.telegrambot.deepl.model.LanguagePairSelection;
import com.telegrambot.deepl.model.LanguageSelection;
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

    private final Map<Long, LanguagePairSelection> userLanguagePairPreferences = new ConcurrentHashMap<>();
    private final Map<Long, LanguageSelection> userLanguagePreferences = new ConcurrentHashMap<>();
    private final Map<Long, String> lastCommandByUser = new ConcurrentHashMap<>();
    private final Map<Long, UserLanguageData> userLanguageDataMap = new ConcurrentHashMap<>();


    public void registerUser(Message msg) {
        if (userRepositoryInterface.findById(msg.getChatId()).isEmpty()) {
            UserRepository userRepository = new UserRepository();
            getUserInfo(msg, userRepository);

            userRepositoryInterface.save(userRepository);
            log.info("User saved: " + userRepository);
        }
    }

    public void deleteUser(ChatIdHolder chatIdHolder) {
        Optional<UserRepository> optionalUserRepository = userRepositoryInterface.findById(chatIdHolder.chatId());
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

    public boolean isUserExists(Long chatId) {
        return userRepositoryInterface.findById(chatId).isPresent();
    }

    public LanguagePairSelection getUserLanguagePair(Long userId) {
        return userLanguagePairPreferences.get(userId);
    }

    public LanguageSelection getUserLanguage(Long userId) {
        return userLanguagePreferences.get(userId);
    }

    public void setUserLanguagePair(Long userId, String sourceLanguage, String targetLanguage) {
        userLanguagePairPreferences.put(userId, new LanguagePairSelection(sourceLanguage, targetLanguage));
    }

    public void setUserLanguage(Long userId, String targetLanguage) {
        userLanguagePreferences.put(userId, new LanguageSelection(targetLanguage));
    }

    public void removeUserLanguagePair(Long chatId) {
        userLanguagePairPreferences.remove(chatId);
    }

    public void removeUserLanguage(Long chatId) {
        userLanguagePreferences.remove(chatId);
    }

    public boolean isLanguagePairSet(Long userId) {
        LanguagePairSelection languagePair = getUserLanguagePair(userId);
        return languagePair != null;
    }

    public boolean isLanguageSet(Long userId) {
        LanguageSelection languageSelection = getUserLanguage(userId);
        return languageSelection != null;
    }

    public void setLastCommandForUser(Long userId, String command) {
        lastCommandByUser.put(userId, command);
    }

    public String getLastCommandForUser(Long userId) {
        return lastCommandByUser.get(userId);
    }

    private static class UserLanguageData {
        private boolean selectedSourceLanguage;
        private String sourceLanguage;

        public boolean isSelectedSourceLanguage() {
            return selectedSourceLanguage;
        }

        public void setSelectedSourceLanguage(boolean selectedSourceLanguage) {
            this.selectedSourceLanguage = selectedSourceLanguage;
        }

        public String getSourceLanguage() {
            return sourceLanguage;
        }

        public void setSourceLanguage(String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
        }
    }

    public boolean hasSelectedSourceLanguage(Long userId) {
        return userLanguageDataMap.containsKey(userId) && userLanguageDataMap.get(userId).isSelectedSourceLanguage();
    }

    public void setSelectedSourceLanguage(Long userId, boolean isSelected) {
        UserLanguageData userLanguageData = userLanguageDataMap.computeIfAbsent(userId, id -> new UserLanguageData());
        userLanguageData.setSelectedSourceLanguage(isSelected);
    }

    public String getUserSourceLanguage(Long userId) {
        return userLanguageDataMap.containsKey(userId) ? userLanguageDataMap.get(userId).getSourceLanguage() : null;
    }

    public void setUserSourceLanguage(Long userId, String sourceLanguage) {
        UserLanguageData userLanguageData = userLanguageDataMap.computeIfAbsent(userId, id -> new UserLanguageData());
        userLanguageData.setSourceLanguage(sourceLanguage);
    }


}
