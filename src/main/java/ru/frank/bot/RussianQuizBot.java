package ru.frank.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.frank.bot.botUtils.QuestionAnswerGenerator;
import ru.frank.bot.botUtils.UserScoreHandler;
import ru.frank.bot.botUtils.UserSessionHandler;

@Component
public class RussianQuizBot extends TelegramLongPollingBot {
    private final String BOT_USER_NAME = "fiji_23_feb_bot";
    private final String TOKEN = "6057660314:AAFTTpTrvYnANb1WTlDXqR1kt3HGixxnPyk";

    @Autowired
    QuestionAnswerGenerator questionAnswerGenerator;

    @Autowired
    UserSessionHandler userSessionHandler;

    @Autowired
    UserScoreHandler userScoreHandler;

    @Autowired
    MessageService messageService;

    @Autowired
    CallbackService callbackService;

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();
        SendMessage sendMessage2 = new SendMessage();
        SendPhoto sendPhoto = new SendPhoto();


        if (update.hasMessage() && update.getMessage().hasText()) {
            messageService.processMessage(update, sendMessage, sendMessage2, sendPhoto); // текстовые команды
        } else if (update.hasCallbackQuery()) {
            callbackService.processCallbackQuery(update, sendMessage, sendPhoto);// нажатие кнопок
        }

        try {
            if (sendMessage.getChatId() != null) {
                execute(sendMessage);
            }
            if (sendMessage2.getChatId() != null) {
                execute(sendMessage2);
            }
            if (sendPhoto.getChatId() != null) {
                execute(sendPhoto);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return this.BOT_USER_NAME;
    }

    @Override
    public String getBotToken() {
        return this.TOKEN;
    }
}
