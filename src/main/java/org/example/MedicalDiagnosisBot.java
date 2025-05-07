package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.*;

class MedicalDiagnosisBot extends TelegramLongPollingBot {
    private Map<String, DiagnosisSession> userSessions = new HashMap<>();
    private List<DiagnosticTest> availableTests;

    public MedicalDiagnosisBot() {
        try {
            this.availableTests = TestConfigLoader.loadTests();
            System.out.println("Loaded tests: " + this.availableTests); // Debug statement
        } catch (IOException e) {
            e.printStackTrace();
            this.availableTests = new ArrayList<>();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String chatId = update.getMessage().getChatId().toString();
        String messageText = update.getMessage().getText();

        try {
            SendMessage response = processMessage(chatId, messageText);
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage processMessage(String chatId, String message) {
        if (message.startsWith("/start")) {
            return startCommand(chatId);
        } else if (message.startsWith("/mosf")) {
            return startMOSFTest(chatId);
        } else if (message.startsWith("/cancel")) {
            return cancelSession(chatId);
        } else {
            return handleUserResponse(chatId, message);
        }
    }

    private SendMessage startCommand(String chatId) {
        return createMessage(chatId,
                "Добро пожаловать в медицинский диагностический бот!\n\n" +
                        "Доступные тесты:\n" +
                        "/mosf - Оценка множественной органной дисфункции по Маршаллу\n\n" +
                        "Для отмены текущего теста используйте /cancel");
    }

    private SendMessage startMOSFTest(String chatId) {
        DiagnosticTest mosfTest = availableTests.stream()
                .filter(t -> t.getTestName().contains("Marshall Multiple Organ Dysfunction Score"))
                .findFirst()
                .orElse(null);

        if (mosfTest == null) {
            System.out.println("Тест MOSF не найден в доступных тестах."); // Debug statement
            return createMessage(chatId, "Тест не доступен");
        }

        DiagnosisSession session = new DiagnosisSession(mosfTest);
        userSessions.put(chatId, session);

        return askNextQuestion(chatId, session);
    }

    private SendMessage askNextQuestion(String chatId, DiagnosisSession session) {
        DiagnosticQuestion nextQuestion = session.getNextQuestion();
        if (nextQuestion == null) {
            return createMessage(chatId, "Произошла ошибка: нет вопросов в тесте");
        }

        StringBuilder messageText = new StringBuilder();
        messageText.append("Вопрос ").append(session.getCurrentQuestionNumber())
                .append(" из ").append(session.getTotalQuestions()).append(":\n")
                .append(nextQuestion.getQuestionText()).append("\n\n");

        List<String> possibleAnswers = nextQuestion.getPossibleAnswers();
        for (int i = 0; i < possibleAnswers.size(); i++) {
            messageText.append(i + 1).append(". ").append(possibleAnswers.get(i)).append("\n");
        }

        return createMessage(chatId, messageText.toString());
    }

    private SendMessage handleUserResponse(String chatId, String message) {
        DiagnosisSession session = userSessions.get(chatId);
        if (session == null) {
            return createMessage(chatId,
                    "У вас нет активного теста. Начните тест с помощью команд:\n" +
                            "/mosf - Marshall Multiple Organ Dysfunction Score (MOSF)");
        }

        try {
            DiagnosticQuestion currentQuestion = session.getCurrentQuestion();
            if (currentQuestion == null) {
                return createMessage(chatId, "Ошибка: текущий вопрос не найден");
            }

            List<String> possibleAnswers = currentQuestion.getPossibleAnswers();
            int answerIndex = Integer.parseInt(message) - 1;

            if (answerIndex < 0 || answerIndex >= possibleAnswers.size()) {
                return createMessage(chatId, "Пожалуйста, выберите номер ответа из предложенных");
            }

            String selectedAnswer = possibleAnswers.get(answerIndex);
            Integer answerValue = currentQuestion.getValueForAnswer(selectedAnswer);
            session.recordAnswer(currentQuestion.getParameterName(), answerValue);

            if (session.isComplete()) {
                String diagnosis = session.getDiagnosisResult();
                userSessions.remove(chatId);
                return createMessage(chatId,
                        "Диагностика завершена.\n\n" +
                                "Результат: " + diagnosis + "\n\n" +
                                "Для нового теста используйте команды:\n" +
                                "/mosf - Marshall Multiple Organ Dysfunction Score (MOSF)");
            } else {
                return askNextQuestion(chatId, session);
            }
        } catch (NumberFormatException e) {
            return createMessage(chatId, "Пожалуйста, введите номер ответа (1, 2, 3 и т.д.)");
        }
    }

    private SendMessage cancelSession(String chatId) {
        userSessions.remove(chatId);
        return createMessage(chatId,
                "Текущий тест отменен. Вы можете начать новый тест:\n" +
                        "/mosf - Marshall Multiple Organ Dysfunction Score (MOSF)");
    }

    private SendMessage createMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    @Override
    public String getBotUsername() {
        return "VSTU_tg_bot";
    }

    @Override
    public String getBotToken() {
        return "8168124481:AAH5ur4GGb2Lb_4M3x7ourzmUjeHYp7Kdd8";
    }
}
