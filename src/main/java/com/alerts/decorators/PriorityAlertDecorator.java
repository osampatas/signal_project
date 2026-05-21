package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator Pattern: adds simple priority metadata to an alert.
 */
public class PriorityAlertDecorator extends AlertDecorator {
    private final String priority;

    /**
     * Adds a priority label such as HIGH or MEDIUM.
     *
     * @param wrappedAlert the alert being decorated
     * @param priority the priority label
     */
    public PriorityAlertDecorator(Alert wrappedAlert, String priority) {
        super(wrappedAlert);
        this.priority = priority;
    }

    /**
     * Returns the priority label.
     *
     * @return the priority label
     */
    public String getPriority() {
        return priority;
    }

    @Override
    public String getCondition() {
        return priority + " priority: " + wrappedAlert.getCondition();
    }
}
