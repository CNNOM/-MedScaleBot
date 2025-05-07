package org.example.core;

import org.example.utils.ResponseBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class MessageHandler {
    private final SessionManager sessionManager;
    private final TestProcessor testProcessor;
    private final ResponseBuilder responseBuilder;

    public MessageHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.testProcessor = new TestProcessor();
        this.responseBuilder = new ResponseBuilder();
    }

    public SendMessage handleMessage(String chatId, String message) {
        if (message.startsWith("/start")) {
            return responseBuilder.buildWelcomeMessage(chatId);
        } else if (message.startsWith("/mosf")) {
            return testProcessor.startTest(chatId, "Marshall Multiple Organ Dysfunction Score", sessionManager);
        } else if (message.startsWith("/sofa")) {
            return testProcessor.startTest(chatId, "SOFA (Sequential Organ Failure Assessment)", sessionManager);
        } else if (message.startsWith("/cancel")) {
            sessionManager.cancelSession(chatId);
            return responseBuilder.buildCancellationMessage(chatId);
        } else if (sessionManager.hasActiveSession(chatId)) {
            return testProcessor.processUserResponse(chatId, message, sessionManager);
        } else {
            return responseBuilder.buildUnknownCommandMessage(chatId);
        }
    }
}