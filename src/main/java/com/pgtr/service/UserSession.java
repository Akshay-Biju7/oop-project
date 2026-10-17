package com.pgtr.service;

import com.pgtr.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Thread-safe singleton holding the currently authenticated user session.
 */
public class UserSession {

    private static UserSession instance;
    private User currentUser;
    private final List<Consumer<User>> sessionListeners = new ArrayList<>();

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
        notifyListeners();
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized void logout() {
        this.currentUser = null;
        notifyListeners();
    }

    public synchronized void addSessionListener(Consumer<User> listener) {
        if (listener != null) {
            sessionListeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (Consumer<User> l : sessionListeners) {
            try {
                l.accept(currentUser);
            } catch (Exception ignored) {}
        }
    }
}
