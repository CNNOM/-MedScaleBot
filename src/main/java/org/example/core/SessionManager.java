
package org.example.core;

import org.example.model.DiagnosticTest;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private final Map<String, DiagnosisSession> userSessions = new HashMap<>();

    public void startNewSession(String chatId, DiagnosticTest test) {
        userSessions.put(chatId, new DiagnosisSession(test));
    }

    public DiagnosisSession getSession(String chatId) {
        return userSessions.get(chatId);
    }

    public void cancelSession(String chatId) {
        userSessions.remove(chatId);
    }

    public boolean hasActiveSession(String chatId) {
        return userSessions.containsKey(chatId);
    }
}