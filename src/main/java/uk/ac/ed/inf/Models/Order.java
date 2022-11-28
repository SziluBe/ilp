package uk.ac.ed.inf.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Order {
    private final String orderNo;
    private final String orderDate;
    private final String customer;
    private final String creditCardNumber;
    private final String creditCardExpiry;
    private final String cvv;
    private final int priceTotalInPence;
    private final List<String> orderItems;
    private OrderOutcome outcome;
    private Restaurant restaurant;
    private int requiredSteps;

    /**
     * Constructor annotated with @JsonCreator to enable Jackson de-serialisation
     *
     * @param orderNo           The order's identifier
     * @param orderDate         The order's date
     * @param customer          The customer's name
     * @param creditCardNumber  The credit card number used to place the order
     * @param creditCardExpiry  The expiry date of the credit card used to place the order
     * @param cvv               The CVV code of the credit card used to place the order
     * @param priceTotalInPence The total price of the order, plus the 1 pound delivery charge, in pence
     * @param orderItems        The names of the pizzas included in the order
     */
    @JsonCreator
    private Order(@JsonProperty("orderNo") String orderNo, @JsonProperty("orderDate") String orderDate, @JsonProperty("customer") String customer,
                 @JsonProperty("creditCardNumber") String creditCardNumber, @JsonProperty("creditCardExpiry") String creditCardExpiry, @JsonProperty("cvv") String cvv,
                 @JsonProperty("priceTotalInPence") int priceTotalInPence, @JsonProperty("orderItems") String[] orderItems) throws IOException {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.customer = customer;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
        this.priceTotalInPence = priceTotalInPence;
        this.orderItems = List.of(orderItems);
        this.outcome = OrderOutcome.Undecided;
        this.restaurant = null;
        this.requiredSteps = -1;
    }


    /**
     * @return orderNo
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * @return orderDate
     */
    public String getOrderDate() {
        return orderDate;
    }

    /**
     * @return customer
     */
    public String getCustomer() {
        return customer;
    }

    /**
     * @return creditCardNumber
     */
    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    /**
     * @return creditCardExpiry
     */
    public String getCreditCardExpiry() {
        return creditCardExpiry;
    }

    /**
     * @return cvv
     */
    public String getCvv() {
        return cvv;
    }

    /**
     * @return priceTotalInPence
     */
    public int getPriceTotalInPence() {
        return priceTotalInPence;
    }

    /**
     * @return orderItems
     */
    public List<String> getOrderItems() {
        return orderItems;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public int getRequiredSteps() {
        return requiredSteps;
    }

    public void setRequiredSteps(int requiredSteps) {
        this.requiredSteps = requiredSteps;
    }

    public OrderOutcome getOutcome() {
        return outcome;
    }
    public void setOutcome(OrderOutcome outcome) {
        this.outcome = outcome;
    }

    public OrderOutcome validateOrder(Restaurant[] restaurants) {
        if (!this.checkCardNumber()) {
            return OrderOutcome.InvalidCardNumber;
        }
        if (!this.checkExpiryDate()) {
            return OrderOutcome.InvalidExpiryDate;
        }
        if (!this.checkCvv()) {
            return OrderOutcome.InvalidCvv;
        }
        if (!this.checkTotal(restaurants)) {
            return OrderOutcome.InvalidTotal;
        }
        if (!this.checkPizzasDefined(restaurants)) {
            return OrderOutcome.InvalidPizzaNotDefined;
        }
        if (!this.checkPizzaCount()) {
            return OrderOutcome.InvalidPizzaCount;
        }
        if (!this.checkPizzaCombination(restaurants)) {
            return OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
        }
        return OrderOutcome.ValidButNotDelivered;
    }

    /**
     * Calculates the delivery cost given a list of restaurants and pizzas.
     *
     * @param restaurants The restaurants considered
     * @return The sum of the prices of the pizzas, plus 1 pound delivery charge, in pence
     */
    public int getDeliveryCost(Restaurant[] restaurants) {
        try { // Make sure we don't crash if we somehow get a null value in restaurants
            assert restaurants != null;
        } catch (AssertionError e) {
            return -1;
        }

        return calcTotal(restaurants) + Constants.DELIVERY_CHARGE;
    }

    private boolean checkPizzaCombination(Restaurant[] restaurants) {
        try { // Make sure we don't crash if we somehow get a null value in restaurants
            assert restaurants != null;
        } catch (AssertionError e) {
            return false;
        }

        return Arrays.stream(restaurants).anyMatch(restaurant -> {
            Set<String> menuItemNames = new HashSet<>(Arrays.stream(restaurant.getMenu())
                    .map(MenuItem::getName)
                    .toList()); // create set of pizza names available at each restaurant

            menuItemNames.retainAll(this.orderItems); // find intersection between order and pizzas available (names)

            return menuItemNames.size() == this.orderItems.size(); // if the order is valid, all pizzas must come from the same restaurant
        });

    }

    private int calcTotal(Restaurant[] restaurants) {
        List<MenuItem> allMenuItems = getAllMenuItems(restaurants);

        return allMenuItems.stream()
                .filter(menuItem -> { // filter for pizzas whose name appears in the list of names
                    return this.orderItems.contains(menuItem.getName());
                })
                .map(MenuItem::getPriceInPence) // get their prices
                .reduce(0, Integer::sum);
    }

    private List<MenuItem> getAllMenuItems(Restaurant[] restaurants) {
        return Arrays.stream(restaurants)
                .map(Restaurant::getMenu) // map to Arrays
                .map(Arrays::asList) // then to Lists
                // TODO: decide if we want explicit type here
                .reduce(new ArrayList<MenuItem>(), (runningList, newMenuArray) -> { // concatenate
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
            return this.creditCardExpiry.matches("(10|11|12|0[1-9])\\/(2[3-9]|[3-9][0-9])");  // Technically this will become wrong if we change centuries, but for the time being it is safer.
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
        try { // Make sure we don't crash if we somehow get a null value in restaurants
            assert restaurants != null;
        } catch (AssertionError e) {
            return false;
        }

        List<MenuItem> allMenuItems = getAllMenuItems(restaurants);

        HashSet<String> allMenuNames = allMenuItems.stream() // we want a HashSet for fast lookup
                .map(MenuItem::getName).collect(Collectors.toCollection(HashSet::new)); // to Set<String>

        return allMenuNames.containsAll(this.orderItems); // ensure every pizza name in the order is present in at least one restaurant's menu
    }

    private boolean checkTotal(Restaurant[] restaurants) {
        // difficult to remove code duplication and also retain readability due to the try-catch, I think the code is least confusing kept this way
        try { // Make sure we don't crash if we somehow get a null value in restaurants
            assert restaurants != null;
        } catch (AssertionError e) {
            return false;
        }

        return calcTotal(restaurants) + 100 == this.priceTotalInPence;
    }

}

