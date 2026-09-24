package com.taha.linkedin.notification_service.auth;

import com.taha.linkedin.notification_service.auth.UserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeinClientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId != null) {
        requestTemplate.header("X-User-Id", userId.toString());
        }
    }
}
