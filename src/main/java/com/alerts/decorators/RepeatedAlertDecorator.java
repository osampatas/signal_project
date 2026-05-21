package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator Pattern: marks an alert as repeated without changing Alert itself.
 */
public class RepeatedAlertDecorator extends AlertDecorator {
    private final int repeatCount;

    /**
     * Adds a repeat count to an alert.
     *
     * @param wrappedAlert the alert being decorated
     * @param repeatCount how many times the alert has repeated
     */
    public RepeatedAlertDecorator(Alert wrappedAlert, int repeatCount) {
        super(wrappedAlert);
        this.repeatCount = repeatCount;
    }

    /**
     * Returns how many times the alert has repeated.
     *
     * @return repeat count
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    @Override
    public String getCondition() {
        return wrappedAlert.getCondition() + " (repeated " + repeatCount + " times)";
    }
}
