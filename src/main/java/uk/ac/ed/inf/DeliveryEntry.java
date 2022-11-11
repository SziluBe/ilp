package uk.ac.ed.inf;

public record DeliveryEntry(String orderNo, OrderOutcome outcome, int costInPence) {
}
