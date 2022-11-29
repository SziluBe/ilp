package uk.ac.ed.inf.Models.Input;

import uk.ac.ed.inf.Constants;
import uk.ac.ed.inf.Models.OrderOutcome;

import java.util.*;
import java.util.stream.Collectors;

public record Order(String orderNo,
                    String orderDate,
                    String customer,
                    String creditCardNumber,
                    String creditCardExpiry,
                    String cvv,
                    int priceTotalInPence,
                    Set<String> orderItems) {

    public OrderOutcome validateOrder(Restaurant restaurant, Restaurant[] restaurants) {
        if (!this.checkCardNumber()) {
            return OrderOutcome.InvalidCardNumber;
        }
        if (!this.checkExpiryDate()) {
            return OrderOutcome.InvalidExpiryDate;
        }
        if (!this.checkCvv()) {
            return OrderOutcome.InvalidCvv;
        }
        if (!this.checkTotal(restaurant)) {
            return OrderOutcome.InvalidTotal;
        }
        if (!this.checkPizzasDefined(restaurants)) {
            return OrderOutcome.InvalidPizzaNotDefined;
        }
        if (!this.checkPizzaCount()) {
            return OrderOutcome.InvalidPizzaCount;
        }
        if (!this.checkPizzaCombination(restaurant)) {
            return OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
        }
        return OrderOutcome.ValidButNotDelivered;
    }

    private boolean checkPizzaCombination(Restaurant restaurant) {
        try { // Make sure we don't crash if we somehow get a null value in restaurant
            assert restaurant != null;
        } catch (AssertionError e) {
            return false;
        }

        return Arrays.stream(restaurant.menuItems())
                .map(MenuItem::name)
                .collect(Collectors.toSet())
                .containsAll(this.orderItems);
    }

    private int calcTotal(Restaurant restaurant) {
        return Arrays.stream(restaurant.menuItems())
                .filter(menuItem -> { // filter for pizzas whose name appears in the list of names
                    return this.orderItems.contains(menuItem.name());
                })
                .map(MenuItem::priceInPence) // get their prices
                .reduce(0, Integer::sum);
    }

    private List<MenuItem> getAllMenuItems(Restaurant[] restaurants) {
        return Arrays.stream(restaurants)
                .map(Restaurant::menuItems) // map to Arrays
                .map(Arrays::asList) // then to Lists
                .reduce(new ArrayList<>(), (runningList, newMenuArray) -> { // concatenate
                    runningList.addAll(newMenuArray);
                    return runningList;
                });
    }


    // stuff that'll probably be useful later
    // card checking using Luhn algorithm
    private boolean checkCardNumber() {
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
        // 13/16 for Visa, 15 for Amex, 16 for Mastercard
        return (sum % 10 == 0) && (this.creditCardNumber.length() == 16 || this.creditCardNumber.length() == 13 || this.creditCardNumber.length() == 15);
    }

    private boolean checkExpiryDate() { // TODO: this should validate with the day of the order
        boolean checkLength = this.creditCardExpiry.length() == 5;
        if (checkLength) {
            return this.creditCardExpiry.matches("(10|11|12|0[1-9])/(2[3-9]|[3-9][0-9])");  // Technically this will become wrong if we change centuries, but for the time being it is safer.
            // This is a reminder to change it if we get to that. Sorry 2100s developers, and hello from the past :)
            // No need to check for the edge case where we are in 2023 but before the service's starting date since it starts on January 1st :)
        } else return false;
    }

    private boolean checkCvv() {
        return this.cvv.matches("[0-9]{3}");
    }

    private boolean checkPizzaCount() {
        int l = this.orderItems.size();
        return 0 < l && l < 5;
    }

    private boolean checkPizzasDefined(Restaurant[] restaurants) {
        try { // Make sure we don't crash if we somehow get null input
            assert restaurants != null;
        } catch (AssertionError e) {
            return false;
        }

        List<MenuItem> allMenuItems = getAllMenuItems(restaurants);

        HashSet<String> allMenuNames = allMenuItems.stream() // we want a HashSet for fast lookup
                .map(MenuItem::name).collect(Collectors.toCollection(HashSet::new)); // to Set<String>

        return allMenuNames.containsAll(this.orderItems); // ensure every pizza name in the order is present in at least one restaurant's menu
    }

    private boolean checkTotal(Restaurant restaurant) {
        // difficult to remove code duplication and also retain readability due to the try-catch, I think the code is least confusing kept this way
        try { // Make sure we don't crash if we somehow get a null value in restaurant
            assert restaurant != null;
        } catch (AssertionError e) {
            return false;
        }

        return calcTotal(restaurant) + Constants.DELIVERY_CHARGE == this.priceTotalInPence;
    }

}

