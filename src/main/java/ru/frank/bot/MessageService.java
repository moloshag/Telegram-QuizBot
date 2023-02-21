package ru.frank.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.frank.bot.botUtils.QuestionAnswerGenerator;
import ru.frank.bot.botUtils.UserScoreHandler;
import ru.frank.bot.botUtils.UserSessionHandler;
import ru.frank.dataBaseUtil.UserQuestionAndAnswerDao;
import ru.frank.model.UserQuestionAndAnswer;
import ru.frank.model.UserScore;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class MessageService {
    private static final String WIN = "Поздравляю! Ответ правильный!";
    private static final String FAIL = "Не верно! Правильный ответ:";
    private static final String NEXT_QUESTION = "Поздравляю! Правильный ответ!";
    @Autowired
    QuestionAnswerGenerator questionAnswerGenerator;

    @Autowired
    UserSessionHandler userSessionHandler;

    @Autowired
    UserScoreHandler userScoreHandler;

    @Autowired
    UserQuestionAndAnswerDao userQuestionAndAnswerDao;

    public void processMessage(Update update, SendMessage sendMessage, SendMessage sendMessage2,SendPhoto sendPhoto) {
        Message message = update.getMessage();
        String chatId = update.getMessage().getChatId().toString();
        Long userId = message.getFrom().getId();
        String userName = message.getFrom().getUserName();
        String userMessageText = message.getText().toLowerCase();

        if (message.getText().equals("/start")) {
            if (!userScoreHandler.userAlreadyInChart(userId)) {
                userScoreHandler.addNewUserInChart(userId, userName);
            }

            sendMessage.setChatId(chatId);
            sendMessage.setText("Добро пожаловать в игру! Если готов, пристегивайся и жми кнопку поехали!");
            sendMessage.setReplyMarkup(getMainBotMarkup());
            return;
        }
        checkAnswer(userMessageText, sendMessage, sendMessage2, userId, chatId, sendPhoto);

    }

    private InlineKeyboardMarkup getMainBotMarkup() {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        InlineKeyboardButton first = new InlineKeyboardButton();
        first.setText("Поехали!");
        first.setCallbackData("/go");
        firstRow.add(first);
        rowsInLine.add(firstRow);
        markupInline.setKeyboard(rowsInLine);
        return markupInline;
    }

    private InlineKeyboardMarkup getScoreBotMarkup() {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        InlineKeyboardButton first = new InlineKeyboardButton();
        first.setText("Посмотреть результат!");
        first.setCallbackData("/score");
        firstRow.add(first);
        rowsInLine.add(firstRow);
        markupInline.setKeyboard(rowsInLine);
        return markupInline;
    }


    private void checkAnswer(String userMessageText, SendMessage sendMessage, SendMessage sendMessage2,Long userId, String chatId, SendPhoto sendPhoto) {
        if (userSessionHandler.getQuestionFromSession(userId) != null) {
            deleteUserQuestionAndAnswer(userSessionHandler.getQuestionFromSession(userId), userId);
        } else {
            sendMessage.setChatId(chatId);
            sendMessage.setText("Поздравляю! Ты дошел до конца!");
            sendMessage.setReplyMarkup(getScoreBotMarkup());
            userScoreHandler.updateFinishTime(userId);
            return;
        }

        // Правильный ответ на вопрос викторины
        String rightAnswer = userSessionHandler.getAnswerFromSession(userId);

        // Получаем новый вопрос + ответ из генератора в виде одной строки.
        String questionAndAnswer = questionAnswerGenerator.getRandomUserQuestion(userId);
        String[] questionAndAnswerArray = questionAndAnswer.split("\\|");
        String question = questionAndAnswerArray[0];

        if (rightAnswer.equalsIgnoreCase(userMessageText.toLowerCase())) {
            if (question.contains("D:/")) {
                sendMessage.setText("Поздравляю! Ответ правильный!");
                sendMessage.setChatId(chatId);
                createPhotoAnswer(questionAndAnswer, sendPhoto, userId, chatId);
                return;
            }
            if (questionAndAnswer.isEmpty()) {
                sendMessage.setText("Поздравляю! Ответ правильный! \nТы дошел до конца!");
                sendMessage.setReplyMarkup(getScoreBotMarkup());
                userScoreHandler.updateFinishTime(userId);
            } else {
                sendMessage.setText("Поздравляю! Ответ правильный!");
                sendMessage2.setText("Следующий вопрос: " + question);
            }
            // Увеличиваем счет пользователя на 1.
            userScoreHandler.incrementUserScore(userId);
        } else {
            if (question.contains("D:/")) {
                sendMessage.setText("Не верно! Правильный ответ: " + rightAnswer);
                sendMessage.setChatId(chatId);
                createPhotoForWrongAnswer(questionAndAnswer, sendPhoto, userId, chatId);
                return;
            }
            if (questionAndAnswer.isEmpty()) {
                sendMessage.setText("Не верно! Правильный ответ: " + rightAnswer + " \nТы дошел до конца!");
                sendMessage.setReplyMarkup(getScoreBotMarkup());
                userScoreHandler.updateFinishTime(userId);
            } else {
                sendMessage.setText("Не верно! Правильный ответ: " + rightAnswer);
                sendMessage2.setText("Следующий вопрос: " + question);
            }
        }
        userSessionHandler.deleteUserSession(userId);
        if (!questionAndAnswer.isEmpty()) {
            userSessionHandler.createUserSession(userId, questionAndAnswer);
        }
        sendMessage.setChatId(chatId);
        sendMessage2.setChatId(chatId);
    }

    private void createPhotoAnswer(String questionAndAnswer, SendPhoto sendPhoto, Long userId, String chatId) {
        String[] questionAndAnswerArray = questionAndAnswer.split("\\|");
        String question = questionAndAnswerArray[0];
        String comment = questionAndAnswerArray[2];
        sendPhoto.setCaption(comment);
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(new File(question)));
        userScoreHandler.incrementUserScore(userId);
        userSessionHandler.deleteUserSession(userId);
        if (!questionAndAnswer.isEmpty()) {
            userSessionHandler.createUserSession(userId, questionAndAnswer);
        }
    }

    private void createPhotoForWrongAnswer(String questionAndAnswer, SendPhoto sendPhoto, Long userId, String chatId) {
        String[] questionAndAnswerArray = questionAndAnswer.split("\\|");
        String question = questionAndAnswerArray[0];
        String comment = questionAndAnswerArray[2];
        sendPhoto.setCaption(comment);
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(new File(question)));
        userSessionHandler.deleteUserSession(userId);
        if (!questionAndAnswer.isEmpty()) {
            userSessionHandler.createUserSession(userId, questionAndAnswer);
        }
    }


    private void deleteUserQuestionAndAnswer(String question, long userId) {
        UserQuestionAndAnswer userQuestionAndAnswer = userQuestionAndAnswerDao.getByQuestion(question, userId);
        if (userQuestionAndAnswer != null) {
            userQuestionAndAnswerDao.delete(userQuestionAndAnswer);
        }
    }
}


