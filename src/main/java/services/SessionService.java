package services;

import models.User;

public class SessionService {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public static String getCurrentRole() {
        return currentUser == null ? "GUEST" : currentUser.getRole();
    }

    public static void clear() {
        currentUser = null;
    }
}
