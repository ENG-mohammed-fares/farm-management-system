package com.smartfarm.util;

public class SessionManager {

    private static int currentUserId = -1;
    private static int currentFwId = -1;
    private static String currentUserName = "";
    private static boolean isAdmin = false;

    public static void login(int userId, String name, boolean admin) {
        currentUserId = userId;
        currentUserName = name;
        isAdmin = admin;
    }

    public static void setFwId(int fwId) {
        currentFwId = fwId;
    }

    public static int getUserId() { return currentUserId; }
    public static int getFwId() { return currentFwId; }
    public static String getUserName() { return currentUserName; }
    public static boolean isAdmin() { return isAdmin; }

    public static void logout() {
        currentUserId = -1;
        currentFwId = -1;
        currentUserName = "";
        isAdmin = false;
    }
}