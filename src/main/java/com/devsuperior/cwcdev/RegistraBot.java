package com.devsuperior.cwcdev;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class RegistraBot {

    private final EchoBot echoBot;

    @Autowired
    public RegistraBot(EchoBot echoBot) {
        this.echoBot = echoBot;
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(echoBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
