package uk.ac.ed.inf.Models.Output;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an entry in the delivery that is output to the user (as a JSON).
 */
public record DeliveryEntry(@NotNull String orderNo, @NotNull OrderOutcome outcome, int costInPence) {
    /**
     * Default record constructor.
     *
     * @param String orderNo The order number.
     * @param OrderOutcome outcome The outcome of the order.
     * @param int costInPence The cost of the order in pence.
     */
}
