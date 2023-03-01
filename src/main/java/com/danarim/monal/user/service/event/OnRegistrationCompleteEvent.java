package com.danarim.monal.user.service.event;

import com.danarim.monal.user.persistence.model.User;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * Event that is fired when a user is registered.
 */
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 6538004361049763191L;

    private final User user;

    public OnRegistrationCompleteEvent(User user) {
        super(user);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

}
