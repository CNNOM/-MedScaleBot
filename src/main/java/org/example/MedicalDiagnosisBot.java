// MedicalDiagnosisBot.java (основной класс)
package org.example;

import org.example.core.MessageHandler;
import org.example.core.SessionManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MedicalDiagnosisBot extends TelegramLongPollingBot {
    private final SessionManager sessionManager;
    private final MessageHandler messageHandler;

    public MedicalDiagnosisBot() {
        this.sessionManager = new SessionManager();
        this.messageHandler = new MessageHandler(sessionManager);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String chatId = update.getMessage().getChatId().toString();
        String messageText = update.getMessage().getText();

        try {
            SendMessage response = messageHandler.handleMessage(chatId, messageText);
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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