package org.example.utils;

import org.example.core.DiagnosisSession;
import org.example.model.DiagnosticQuestion;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public class ResponseBuilder {
    public SendMessage buildWelcomeMessage(String chatId) {
        String text = "Добро пожаловать в медицинский диагностический бот!\n\n" +
                "Доступные тесты:\n" +
                "/mosf - Оценка множественной органной дисфункции по Маршаллу\n" +
                "/sofa - Последовательная оценка органной недостаточности (SOFA)\n\n" +
                "Для отмены текущего теста используйте /cancel";
        return createMessage(chatId, text);
    }

    public SendMessage buildCancellationMessage(String chatId) {
        String text = "Текущий тест отменен. Вы можете начать новый тест:\n" +
                "/mosf - Оценка множественной органной дисфункции по Маршаллу\n" +
                "/sofa - Последовательная оценка органной недостаточности (SOFA)\n\n";
        return createMessage(chatId, text);
    }

    public SendMessage buildTestCompletionMessage(String chatId, String diagnosis) {
        String text = "Диагностика завершена.\n\n" +
                "Результат: " + diagnosis + "\n\n" +
                "Для нового теста используйте команды:\n" +
                "/mosf - Оценка множественной органной дисфункции по Маршаллу\n" +
                "/sofa - Последовательная оценка органной недостаточности (SOFA)\n\n";
        return createMessage(chatId, text);
    }

    public SendMessage buildQuestionMessage(String chatId, DiagnosisSession session, DiagnosticQuestion question) {
        StringBuilder text = new StringBuilder();
        text.append("Вопрос ").append(session.getCurrentQuestionNumber())
                .append(" из ").append(session.getTotalQuestions()).append(":\n")
                .append(question.getQuestionText()).append("\n\n");

        List<String> possibleAnswers = question.getPossibleAnswers();
        for (int i = 0; i < possibleAnswers.size(); i++) {
            text.append(i + 1).append(". ").append(possibleAnswers.get(i)).append("\n");
        }

        return createMessage(chatId, text.toString());
    }

    public SendMessage buildTestNotAvailableMessage(String chatId) {
        return createMessage(chatId, "Тест не доступен");
    }

    public SendMessage buildInvalidAnswerMessage(String chatId) {
        return createMessage(chatId, "Пожалуйста, выберите номер ответа из предложенных");
    }

    public SendMessage buildInvalidInputMessage(String chatId) {
        return createMessage(chatId, "Пожалуйста, введите номер ответа (1, 2, 3 и т.д.)");
    }

    public SendMessage buildTestError(String chatId) {
        return createMessage(chatId, "Произошла ошибка: нет вопросов в тесте");
    }

    public SendMessage buildUnknownCommandMessage(String chatId) {
        return createMessage(chatId, "У вас нет активного теста. Начните тест с помощью команд:\n" +
                "/mosf - Оценка множественной органной дисфункции по Маршаллу\n" +
                "/sofa - Последовательная оценка органной недостаточности (SOFA)\n\n");
    }

    private SendMessage createMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }
}