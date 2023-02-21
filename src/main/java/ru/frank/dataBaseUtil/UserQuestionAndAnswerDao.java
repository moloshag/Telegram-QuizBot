package ru.frank.dataBaseUtil;

import org.springframework.stereotype.Repository;
import ru.frank.model.UserQuestionAndAnswer;
import ru.frank.model.UserSession;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class UserQuestionAndAnswerDao {
    @PersistenceContext
    private EntityManager entityManager;

    public UserQuestionAndAnswer getByAnswer(String answer) {
        return (UserQuestionAndAnswer) entityManager
                .createQuery("from UserQuestionAndAnswer where lower(answer) like lower(" + answer + ")")
                .getSingleResult();
    }

    public UserQuestionAndAnswer getByQuestion(String question, Long userId) {
        return (UserQuestionAndAnswer) entityManager
                .createQuery("from UserQuestionAndAnswer where question = '" + question +"' and userId = " + userId)
                .getSingleResult();
    }

    public String save(UserQuestionAndAnswer userQuestionAndAnswer) {
        entityManager.persist(userQuestionAndAnswer);
        return userQuestionAndAnswer.getUuid();
    }

    public List<UserQuestionAndAnswer> getAllByUser(Long userId) {
        return entityManager.createQuery("from UserQuestionAndAnswer where userId =" + userId).getResultList();
    }

    public void delete(UserQuestionAndAnswer userSession) {
        if(entityManager.contains(userSession)){
            entityManager.remove(userSession);
        } else{
            entityManager.remove(entityManager.merge(userSession));
        }
    }
}
