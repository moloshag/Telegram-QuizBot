package ru.frank.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class BotInitilizer {

    private RussianQuizBot russianQuizBot;
    private TelegramBotsApi telegramBotsApi;

    @Autowired
    public BotInitilizer(RussianQuizBot russianQuizBot, TelegramBotsApi telegramBotsApi){
        try{
            telegramBotsApi.registerBot(russianQuizBot);
        } catch (TelegramApiException ex){
            ex.printStackTrace();
        }

    }
}
