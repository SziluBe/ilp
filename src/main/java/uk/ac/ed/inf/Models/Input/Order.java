package uk.ac.ed.inf.Models.Input;

import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.Models.Output.OrderOutcome;

import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents an order.
 */
public record Order(String orderNo,
                    String orderDate,
                    String customer,
                    String creditCardNumber,
                    String creditCardExpiry,
                    String cvv,
                    int priceTotalInPence,
                    String[] orderItems) {
    /**
     * Default record constructor.
     *
     * @param String orderNo The order number.
     * @param String orderDate The date of the order.
     * @param String customer The customer.
     * @param String creditCardNumber The credit card number.
     * @param String creditCardExpiry The credit card expiry.
     * @param String cvv The cvv.
     * @param int priceTotalInPence The total price of the order in pence.
     * @param String[] orderItems The order items.
     */

    /**
     * The delivery charge in pence; should be added to the price of the order calculated from the menu items.
     */
    public static final int DELIVERY_CHARGE = 100;

    /**
     * Validates the order.
     *
     * @param restaurant The restaurant which the order is for.
     * @param menuItems  The names of the pizzas available from all restaurants. (Used to check that all pizza
     *                   names in the order are valid.)
     * @return The tentative outcome of the order, before delivery.
     */
    @NotNull
    public OrderOutcome validateOrder(Restaurant restaurant, MenuItem[] menuItems, String inputDate) {
        if (restaurant == null || menuItems == null) {
            System.out.println("Invalid validation call: restaurant or menuItems is null");
            return OrderOutcome.Invalid;
        }
        if (!this.checkOrderDate(inputDate)) {
            System.out.println("Invalid order date: " + this.orderDate());
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
                .sum() + DELIVERY_CHARGE;
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

    private boolean checkOrderDate(String inputDate) {
        // order needs to be on the day we are asking for
        if (!this.orderDate().equals(inputDate)) {
            return false;
        }
        // order needs to be in the right format
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // this will throw an exception if the date is not in the right format
            formatter.parse(this.orderDate());
            // otherwise, the date is valid
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    private boolean checkExpiryDate() {
        var orderDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        var expiryDateFormatter = DateTimeFormatter.ofPattern("MM/yy");
        try {
            // this will throw a DateTimeException if the expiry date is invalid
            TemporalAccessor expiryDate = expiryDateFormatter.parse(this.creditCardExpiry());
            // this will throw a DateTimeException if the order date is invalid
            // although this should never happen as it is checked in checkOrderDate(),
            // which should be called before this method to ensure the correct priority of order outcomes
            TemporalAccessor orderDateAccessor = orderDateFormatter.parse(this.orderDate());
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
