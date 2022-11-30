package uk.ac.ed.inf.Models.Input;

import uk.ac.ed.inf.Constants;
import uk.ac.ed.inf.Models.OrderOutcome;

import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public record Order(String orderNo,
                    String orderDate,
                    String customer,
                    String creditCardNumber,
                    String creditCardExpiry,
                    String cvv,
                    int priceTotalInPence,
                    String[] orderItems) {

    public OrderOutcome validateOrder(Restaurant restaurant, MenuItem[] menuItems) {
        if (restaurant == null || menuItems == null) {
            System.out.println("Invalid validation call: restaurant or menuItems is null"); // TODO: make this an exception?
            return OrderOutcome.Invalid;
        }
        if (!this.checkCardNumber()) {
            System.out.println("Invalid card number: " + this.creditCardNumber());
            return OrderOutcome.InvalidCardNumber;
        }
        if (!this.checkExpiryDate()) {
            System.out.println("Invalid expiry date: " + this.creditCardExpiry());
            return OrderOutcome.InvalidExpiryDate;
        }
        if (!this.checkCvv()) {
            System.out.println("Invalid CVV: " + this.cvv());
            return OrderOutcome.InvalidCvv;
        }
        if (!this.checkPizzasDefined(menuItems)) {
            System.out.println("Invalid pizza not defined: " + Arrays.toString(this.orderItems()));
            return OrderOutcome.InvalidPizzaNotDefined;
        }
        if (!this.checkPizzaCombination(restaurant)) {
            System.out.println("Invalid pizza combination: " + Arrays.toString(this.orderItems()));
            return OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
        }
        if (!this.checkPizzaCount()) {
            System.out.println("Invalid pizza count: " + Arrays.toString(this.orderItems()));
            return OrderOutcome.InvalidPizzaCount;
        }
        if (!this.checkTotal(restaurant)) {
            System.out.println("Invalid total: " + this.priceTotalInPence());
            return OrderOutcome.InvalidTotal;
        }
        return OrderOutcome.ValidButNotDelivered;
    }

    private boolean checkPizzaCombination(Restaurant restaurant) {
        return Arrays.stream(restaurant.menuItems())
                .map(MenuItem::name)
                .collect(Collectors.toSet())
                .containsAll(new HashSet<>(List.of(this.orderItems)));
    }

    private int calcTotal(Restaurant restaurant) {
        return Arrays.stream(this.orderItems())
                .mapToInt(item -> Arrays.stream(restaurant.menuItems())
                        .filter(menuItem -> menuItem.name().equals(item)) // each restaurant should only have
                                                                          // one menu item with a given name
                        .mapToInt(MenuItem::priceInPence)
                        .sum())
                .sum() + Constants.DELIVERY_CHARGE;
    }


    private boolean checkCardNumber() {
        if (this.creditCardNumber().length() != 16) {
            return false;
        }
        for (char c : this.creditCardNumber().toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        // 16 for Visa, 16 for Mastercard -> no 13 digit Visa, 15 digit Amex, or any other
        // first digit 2 or 5 for Mastercard, 4 for Visa
        if (!((this.creditCardNumber.startsWith("4") || this.creditCardNumber.startsWith("5")) ||
                (Integer.parseInt(this.creditCardNumber.substring(0, 6)) >= 222100
                        && Integer.parseInt(this.creditCardNumber.substring(0, 6)) <= 272099))) {
            return false;
        }

        // card checking using Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        for (int i = this.creditCardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(this.creditCardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private boolean checkExpiryDate() throws DateTimeException {
        DateTimeFormatter orderDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter expiryDateFormatter = DateTimeFormatter.ofPattern("MM/yy");
        TemporalAccessor orderDateAccessor = orderDateFormatter.parse(this.orderDate());
        try {
            // this will throw a DateTimeException if the expiry date is invalid
            TemporalAccessor expiryDate = expiryDateFormatter.parse(this.creditCardExpiry());
            if (expiryDate.get(ChronoField.YEAR) > orderDateAccessor.get(ChronoField.YEAR)) {
                return true;
            }
            return expiryDate.get(ChronoField.MONTH_OF_YEAR) >= orderDateAccessor.get(ChronoField.MONTH_OF_YEAR) &&
                    expiryDate.get(ChronoField.YEAR) == orderDateAccessor.get(ChronoField.YEAR);
        // if the expiry date is invalid, we return false
        } catch (DateTimeException e) {
            return false;
        }
    }

    private boolean checkCvv() {
        return this.cvv.matches("[0-9]{3}");
    }

    private boolean checkPizzaCount() {
        int l = this.orderItems.length;
        return 0 < l && l < 5;
    }

    private boolean checkPizzasDefined(MenuItem[] menuItems) {
        Set<String> allItemNames = Arrays.stream(menuItems) // we want a HashSet for fast lookup
                .map(MenuItem::name).collect(Collectors.toCollection(HashSet::new)); // to Set<String>

        // for containsAll only "this" needs to be a Set
        return allItemNames.containsAll(List.of(this.orderItems)); // ensure every pizza name in the order is present in at least one restaurant's menu
    }

    private boolean checkTotal(Restaurant restaurant) {
        return calcTotal(restaurant) == this.priceTotalInPence;
    }

}
