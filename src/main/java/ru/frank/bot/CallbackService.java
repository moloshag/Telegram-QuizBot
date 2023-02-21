package ru.frank.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.frank.bot.botUtils.QuestionAnswerGenerator;
import ru.frank.bot.botUtils.UserScoreHandler;
import ru.frank.bot.botUtils.UserSessionHandler;
import ru.frank.dataBaseUtil.UserQuestionAndAnswerDao;
import ru.frank.model.QuestionAndAnswer;
import ru.frank.model.UserQuestionAndAnswer;
import ru.frank.model.UserScore;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CallbackService {
    @Autowired
    QuestionAnswerGenerator questionAnswerGenerator;

    @Autowired
    UserSessionHandler userSessionHandler;

    @Autowired
    UserScoreHandler userScoreHandler;

    @Autowired
    UserQuestionAndAnswerDao userQuestionAndAnswerDao;

    public void processCallbackQuery(Update update, SendMessage sendMessage, SendPhoto sendPhoto) {
        CallbackQuery query = update.getCallbackQuery();
        String chatId = query.getMessage().getChatId().toString();
        Long userId = query.getFrom().getId();
        String userName = query.getFrom().getUserName();

        if (query.getData().contains("/go")) {
            userScoreHandler.nullingScore(userId);
            deleteQuestionByUser(userId);
            createQuestionList(userId);
            if (userSessionHandler.get(userId) != null) {
                userSessionHandler.deleteUserSession(userId);
            }

            // Получаем новый вопрос + ответ из генератора в виде одной строки.
            String questionAndAnswer = questionAnswerGenerator.getRandomUserQuestion(userId);

            String[] questionAndAnswerArray = questionAndAnswer.split("\\|");
            String question = questionAndAnswerArray[0];
            String comment = questionAndAnswerArray[2];

            // Создаем сессию с текущим пользователем
            userSessionHandler.createUserSession(userId, questionAndAnswer);

            // Проверяем наличие текущего пользователя в таблице БД "score",
            // при отсутствии - добавляем пользователя в таблицу со счетом 0.
            if (!userScoreHandler.userAlreadyInChart(userId)) {
                userScoreHandler.addNewUserInChart(userId, userName);
            }
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(new InputFile(new File(question)));
            sendPhoto.setCaption(comment);
            return;
        }
        if (query.getData().equals("/score")) {
            if (userScoreHandler.userAlreadyInChart(userId)) {
                List<UserScore> topUsersScoreList = userScoreHandler.getUserScore();
                String topUsersScoreString = topUsersScoreList.stream()
                        .map(userScore -> userScore.getUserName() + " - "
                                + userScore.getScore() + " б.; время: " + userScore.getEndTime())
                        .collect(Collectors.joining("\n"));
                sendMessage.setChatId(chatId);
                sendMessage.setText(topUsersScoreString);
                sendMessage.setReplyMarkup(getScoreBotMarkup());
            } else {
                sendMessage.setChatId(chatId);
                sendMessage.setText("Не можем найти ваш результат");
            }
        }
    }

    private void createQuestionList(Long userId) {
        List<QuestionAndAnswer> questionAndAnswers = questionAnswerGenerator.getAll();
        for (QuestionAndAnswer questionAndAnswer : questionAndAnswers) {
            UserQuestionAndAnswer usqa = new UserQuestionAndAnswer();
            usqa.setUuid(UUID.randomUUID().toString());
            usqa.setUserId(userId);
            usqa.setQuestion(questionAndAnswer.getQuestion());
            usqa.setAnswer(questionAndAnswer.getAnswer());
            usqa.setComment(questionAndAnswer.getComment());
            usqa.setStartTime(LocalDateTime.now().toString());
            userQuestionAndAnswerDao.save(usqa);
        }
    }

    private void deleteQuestionByUser(Long userId) {
        List<UserQuestionAndAnswer> list = userQuestionAndAnswerDao.getAllByUser(userId);
        for (UserQuestionAndAnswer userQuestionAndAnswer : list) {
            userQuestionAndAnswerDao.delete(userQuestionAndAnswer);
        }
    }


    private InlineKeyboardMarkup getScoreBotMarkup() {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        InlineKeyboardButton first = new InlineKeyboardButton();
        first.setText("Обновить результаты!");
        first.setCallbackData("/score");
        firstRow.add(first);
        rowsInLine.add(firstRow);
        markupInline.setKeyboard(rowsInLine);
        return markupInline;
    }

}
