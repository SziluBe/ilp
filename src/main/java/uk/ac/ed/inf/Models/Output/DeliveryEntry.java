package uk.ac.ed.inf.Models.Output;

import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.Models.OrderOutcome;

public record DeliveryEntry(@NotNull String orderNo, @NotNull OrderOutcome outcome, int costInPence) {
}
