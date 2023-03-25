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

package com.telegrambot.deepl.command;

import com.telegrambot.deepl.service.SendMessageServiceInterface;
import org.telegram.telegrambots.meta.api.objects.Update;

public class LanguagesCommand implements CommandInterface {

    private final SendMessageServiceInterface sendMessageServiceInterface;

    public final static String LIST_OF_LANGUAGES_MESSAGE = """
            🇺🇸🇬🇧🇨🇿🇩🇪🇮🇹🇫🇷
            Here is a list of available languages:
            
            BG - Bulgarian
            CS - Czech
            DA - Danish
            DE - German
            EL - Greek
            EN - English
            ES - Spanish
            ET - Estonian
            FI - Finnish
            FR - French
            HU - Hungarian
            ID - Indonesian
            IT - Italian
            JA - Japanese
            KO - Korean
            LT - Lithuanian
            LV - Latvian
            NB - Norwegian (Bokmål)
            NL - Dutch
            PL - Polish
            PT - Portuguese (all Portuguese varieties mixed)
            RO - Romanian
            RU - Russian
            SK - Slovak
            SL - Slovenian
            SV - Swedish
            TR - Turkish
            UK - Ukrainian
            ZH - Chinese
            """;

    public LanguagesCommand(SendMessageServiceInterface sendMessageServiceInterface) {
        this.sendMessageServiceInterface = sendMessageServiceInterface;
    }

    @Override
    public void execute(Update update) throws InterruptedException {
        Long chatId = update.getMessage().getChatId();

        sendMessageServiceInterface.sendMessage(chatId, LIST_OF_LANGUAGES_MESSAGE);
    }
}
