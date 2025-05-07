package org.example.core;

import org.example.model.DiagnosticQuestion;
import org.example.model.DiagnosticTest;
import org.example.model.TestLoader;
import org.example.utils.ResponseBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public class TestProcessor {
    private final TestLoader testLoader;
    private final ResponseBuilder responseBuilder;

    public TestProcessor() {
        this.testLoader = new TestLoader();
        this.responseBuilder = new ResponseBuilder();
    }

    public SendMessage startTest(String chatId, String testName, SessionManager sessionManager) {
        DiagnosticTest test = testLoader.loadTest(testName);
        if (test == null) {
            return responseBuilder.buildTestNotAvailableMessage(chatId);
        }

        sessionManager.startNewSession(chatId, test);
        DiagnosisSession session = sessionManager.getSession(chatId);
        return askNextQuestion(chatId, session);
    }

    public SendMessage processUserResponse(String chatId, String message, SessionManager sessionManager) {
        DiagnosisSession session = sessionManager.getSession(chatId);

        try {
            DiagnosticQuestion currentQuestion = session.getCurrentQuestion();
            int answerIndex = Integer.parseInt(message) - 1;
            List<String> possibleAnswers = currentQuestion.getPossibleAnswers();

            if (answerIndex < 0 || answerIndex >= possibleAnswers.size()) {
                return responseBuilder.buildInvalidAnswerMessage(chatId);
            }

            String selectedAnswer = possibleAnswers.get(answerIndex);
            Integer answerValue = currentQuestion.getValueForAnswer(selectedAnswer);
            session.recordAnswer(currentQuestion.getParameterName(), answerValue);

            if (session.isComplete()) {
                String diagnosis = session.getDiagnosisResult();
                sessionManager.cancelSession(chatId);
                return responseBuilder.buildTestCompletionMessage(chatId, diagnosis);
            } else {
                return askNextQuestion(chatId, session);
            }
        } catch (NumberFormatException e) {
            return responseBuilder.buildInvalidInputMessage(chatId);
        }
    }

    private SendMessage askNextQuestion(String chatId, DiagnosisSession session) {
        DiagnosticQuestion nextQuestion = session.getNextQuestion();
        if (nextQuestion == null) {
            return responseBuilder.buildTestError(chatId);
        }
        return responseBuilder.buildQuestionMessage(chatId, session, nextQuestion);
    }
}