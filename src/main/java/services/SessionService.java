package services;

import models.User;

public class SessionService {
    private static User currentUser;
    private static boolean missingTrainerAccountNoticeShown;

    public static void setCurrentUser(User user) {
        currentUser = user;
        missingTrainerAccountNoticeShown = false;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isStaff() {
        return currentUser != null && "STAFF".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isTrainer() {
        return currentUser != null && "TRAINER".equalsIgnoreCase(currentUser.getRole());
    }

    public static int getCurrentUserId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    public static String getCurrentRole() {
        return currentUser == null ? "GUEST" : currentUser.getRole();
    }

    public static boolean wasMissingTrainerAccountNoticeShown() {
        return missingTrainerAccountNoticeShown;
    }

    public static void markMissingTrainerAccountNoticeShown() {
        missingTrainerAccountNoticeShown = true;
    }

    public static void clear() {
        currentUser = null;
        missingTrainerAccountNoticeShown = false;
    }
}
