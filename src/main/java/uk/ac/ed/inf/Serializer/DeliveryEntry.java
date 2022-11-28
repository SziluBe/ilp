package uk.ac.ed.inf.Serializer;

import uk.ac.ed.inf.Models.OrderOutcome;

public record DeliveryEntry(String orderNo, OrderOutcome outcome, int costInPence) {
}
