package ru.frank.bot.botUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.frank.dataBaseUtil.userScore.UserScoreDao;
import ru.frank.exceptions.UserScoreListIsEmptyException;
import ru.frank.model.UserScore;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс для обработки событий связанных с чтением, изменением, дополнением
 * счета пользователя в таблице базы данных.
 */
@Component
public class UserScoreHandler {

    @Autowired
    UserScoreDao userScoreDao;

    /**
     * Метод проверяет наличие пользователя в таблице базы данных "user_score";
     * @param userId
     * @return true - если пользователь уже есть в таблице, false - если нет.
     */
    public boolean userAlreadyInChart(long userId){
        return userScoreDao.get(userId) != null;
    }

    public UserScore get(Long userId) {
        return userScoreDao.get(userId);
    }

    public void updateFinishTime(Long userId) {
        UserScore userScore = userScoreDao.get(userId);
        if (Objects.nonNull(userScore.getEndTime())) {
            return;
        }
        LocalDateTime startTime = userScore.getStartTime();
        LocalDateTime finishTime = LocalDateTime.now();
        int startMinute = startTime.getMinute();
        int startSec = startTime.getSecond();
        int finishMinute = finishTime.getMinute();
        int finishSecond = finishTime.getSecond();

        userScore.setEndTime((finishMinute-startMinute) + " мин., " + Math.abs(finishSecond-startSec) + " сек.");
        userScoreDao.update(userScore);
    }

    /**
     * Метод добавляет новую запись в таблицу
     * @param userId
     */
    public void addNewUserInChart(long userId, String userName){
        userScoreDao.save(new UserScore(userId, userName, 0, LocalDateTime.now()));
    }

    public long incrementUserScore(long userId){
        UserScore userScore = userScoreDao.get(userId);
        userScore.setScore(userScore.getScore() + 1);
        userScoreDao.update(userScore);
        return userScore.getScore();
    }

    public void nullingScore(long userId) {
        UserScore userScore = userScoreDao.get(userId);
        userScore.setScore(0);
        userScore.setStartTime(LocalDateTime.now());
        userScoreDao.update(userScore);
    }

    public long getUserScoreById(long userId){
        return userScoreDao.get(userId).getScore();
    }

    public List<UserScore> getUserScore() {
        return userScoreDao.getAll().stream()
                .sorted(Comparator.comparing(UserScore::getScore).reversed())
                .collect(Collectors.toList());
    }

}
