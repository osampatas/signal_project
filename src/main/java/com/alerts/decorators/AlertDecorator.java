package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator Pattern: wraps an alert so extra behavior can be added without
 * changing the original alert object.
 */
public class AlertDecorator extends Alert {
    protected final Alert wrappedAlert;

    /**
     * Creates a decorator around an existing alert.
     *
     * @param wrappedAlert the alert being decorated
     */
    public AlertDecorator(Alert wrappedAlert) {
        super(wrappedAlert.getPatientId(), wrappedAlert.getCondition(), wrappedAlert.getTimestamp());
        this.wrappedAlert = wrappedAlert;
    }

    /**
     * Returns the original alert.
     *
     * @return the wrapped alert
     */
    public Alert getWrappedAlert() {
        return wrappedAlert;
    }
}
