package com.abodebase.api.auth.service;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSessionService {

    private final SessionRegistry sessionRegistry;

    public UserSessionService(
        SessionRegistry sessionRegistry
    ) {
        this.sessionRegistry = sessionRegistry;
    }

    public void expireUserSessions(String username) {

        List<Object> principals =
            sessionRegistry.getAllPrincipals();

        for (Object principal : principals) {

            if (principal instanceof org.springframework.security.core.userdetails.User user) {

                if (user.getUsername().equalsIgnoreCase(username)) {

                    List<SessionInformation> sessions =
                        sessionRegistry.getAllSessions(
                            principal,
                            false
                        );

                    for (SessionInformation session : sessions) {
                        session.expireNow();
                    }
                }
            }
        }
    }
}
