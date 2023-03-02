package com.danarim.monal.user.service.event;

import com.danarim.monal.user.persistence.model.User;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * Event that is fired when a user changes his password.
 */
public class OnPasswordUpdatedEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = -5143600406935561769L;

    private final User user;

    public OnPasswordUpdatedEvent(User user) {
        super(user);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

}
