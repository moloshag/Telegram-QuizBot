package ru.frank.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "userQuestionAndAnswer")
public class UserQuestionAndAnswer implements Serializable {

    private static final long serialVersionUID = -2851601477146557967L;

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "startTime")
    private String startTime;

    @Column(name = "question")
    private String question;

    @Column(name = "answer")
    private String answer;

    @Column(name = "comment")
    private String comment;

    public UserQuestionAndAnswer(String uuid, Long userId, String startTime, String question, String answer, String comment) {
        this.uuid = uuid;
        this.userId = userId;
        this.startTime = startTime;
        this.question = question;
        this.answer = answer;
        this.comment = comment;
    }

    public UserQuestionAndAnswer() {
    }

    public String getUuid() {
        return uuid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
