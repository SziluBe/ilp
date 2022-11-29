package uk.ac.ed.inf.Models;

public record DeliveryEntry(String orderNo, OrderOutcome outcome, int costInPence) {
}
