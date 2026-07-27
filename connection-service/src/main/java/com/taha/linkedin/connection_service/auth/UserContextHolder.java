package com.taha.linkedin.connection_service.auth;

public class UserContextHolder {


    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    static void setCurrentUser(Long userId) {
        currentUserId.set(userId);
    }

    static void clear(){
        currentUserId.remove();
    }
}
