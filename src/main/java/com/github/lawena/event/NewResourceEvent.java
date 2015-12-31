package com.github.lawena.event;

import com.github.lawena.domain.Resource;
import org.springframework.context.ApplicationEvent;

public class NewResourceEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public NewResourceEvent(Resource source) {
        super(source);
    }

    @Override
    public Resource getSource() {
        return (Resource) super.getSource();
    }
}
