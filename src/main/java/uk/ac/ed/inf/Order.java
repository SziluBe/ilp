package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.*;

public class Order {
    /**
     * Constructor annotated with @JsonCreator to enable Jackson de-serialisation
     * @param orderNo               The order's identifier
     * @param orderDate             The order's date
     * @param customer              The customer's name
     * @param creditCardNumber      The credit card number used to place the order
     * @param creditCardExpiry      The expiry date of the credit card used to place the order
     * @param cvv                   The CVV code of the credit card used to place the order
     * @param priceTotalInPence     The total price of the order, plus the 1 pound delivery charge, in pence
     * @param orderItems            The names of the pizzas included in the order
     */
    @JsonCreator
    public Order(@JsonProperty("orderNo") String orderNo, @JsonProperty("orderDate") String orderDate, @JsonProperty("customer") String customer,
                 @JsonProperty("creditCardNumber") String creditCardNumber, @JsonProperty("creditCardExpiry") String creditCardExpiry, @JsonProperty("cvv") String cvv,
                 @JsonProperty("priceTotalInPence") int priceTotalInPence, @JsonProperty("orderItems") String[] orderItems) {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.customer = customer;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
        this.priceTotalInPence = priceTotalInPence;
        this.orderItems = orderItems;
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
    public String[] getOrderItems() {
        return orderItems;
    }

    private final String orderNo;
    private final String orderDate;
    private final String customer;
    private final String creditCardNumber;
    private final String creditCardExpiry;
    private final String cvv;
    private final int priceTotalInPence;
    private final String[] orderItems;

    /**
     * Calculates the delivery cost given a list of restaurants and pizzas.
     * @param restaurants   The restaurants considered
     * @param pizzaNames    The names of the pizzas included
     * @return              The sum of the prices of the pizzas, plus 1 pound delivery charge, in pence
     * @throws InvalidPizzaCombination Thrown if invalid combination of pizzas/restaurants provided.
     */
    public int getDeliveryCost(Restaurant[] restaurants, String... pizzaNames) throws InvalidPizzaCombination {
        if (!checkPizzaCombination(restaurants, pizzaNames)) {
            throw new InvalidPizzaCombination();
        } // no need for `else` since we are throwing an exception in the body of the if statement

        return calcTotal(restaurants, pizzaNames) + Constants.DELIVERY_CHARGE;
    }

    private boolean checkPizzaCombination(Restaurant[] restaurants, String[] pizzaNames) {
        Set<String> orderItemsSet = new HashSet<>(Arrays.asList(pizzaNames));

        return Arrays.stream(restaurants).anyMatch(restaurant -> {
                    Set<String> menuItemNames = new HashSet<>(Arrays.stream(restaurant.getMenu())
                            .map(Menu::getName)
                            .toList()); // create set of pizza names available at each restaurant

                    menuItemNames.retainAll(orderItemsSet); // find intersection between order and pizzas available (names)

                    return menuItemNames.size() == orderItemsSet.size(); // if the order is valid, all pizzas must come from the same restaurant
                });

    }

    private int calcTotal(Restaurant[] restaurants, String[] pizzaNames) {
        List<Menu> allMenuItems = getAllMenuItems(restaurants);

        Set<String> pizzaNamesSet = new HashSet<>(Arrays.asList(pizzaNames)); // for faster lookup

        return allMenuItems.stream()
                .filter(menuItem -> { // filter for pizzas whose name appears in the list of names
                    return pizzaNamesSet.contains(menuItem.getName());
                })
                .map(Menu::getPriceInPence) // get their prices
                .reduce(0, Integer::sum);
    }






    // stuff that'll probably be useful later
    private boolean checkCardNumber() {
        boolean checkOnlyDigits = this.creditCardNumber.matches("[0-9]+");
        boolean checkLength = this.creditCardNumber.length() == 16;

        return checkOnlyDigits && checkLength;
    }

    private boolean checkExpiryDate() {
        boolean checkLength = this.creditCardExpiry.length() == 5;
        if (checkLength) {
            return this.creditCardExpiry.matches("(10|11|12|0[1-9])\\/(2[3-9]|[3-9][0-9])");  // Technically this will become wrong if we change centuries, but for the time being it is safer.
                                                                                                    // This is a reminder to change it if we get to that. Sorry 2100s developers, and hello from the past :)
            // No need to check for the edge case where we are in 2023 but before the service's starting date since it starts on January 1st :)
        } else return false;
    }

    private boolean checkCVV() {
        return this.cvv.matches("[0-9]{3}");
    }

    private boolean checkPizzaCount() {
        int l = this.orderItems.length;
        return 0 < l && l < 5;
    }

    private List<Menu> getAllMenuItems(Restaurant[] restaurants) {
        return Arrays.stream(restaurants)
                .map(Restaurant::getMenu) // map to Arrays
                .map(Arrays::asList) // then to Lists
                .reduce(new ArrayList<Menu>(), (runningList, newMenuArray) -> { // concatenate
                    runningList.addAll(newMenuArray);
                    return runningList;
                });
    }

    private boolean checkPizzasDefined() throws IOException {
        // TODO: change address in CW2
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(Constants.DEFAULT_BASE_ADDRESS);
        try { // Make sure we don't crash if we somehow get a null value in restaurants
            assert restaurants != null;
        } catch (AssertionError e) {
            return false;
        }

        List<Menu> allMenuItems = getAllMenuItems(restaurants);

        HashSet<String> allMenuNames = new HashSet<>(allMenuItems.stream() // we want a HashSet for fast lookup
                .map(Menu::getName) // map List<Menu>
                .toList()); // to List<String>

        return Arrays.stream(this.orderItems)
                .allMatch(allMenuNames::contains); // ensure every pizza name in the order is present in at least one restaurant's menu
    }

    private boolean checkTotal() throws IOException {
        // difficult to remove code duplication and also retain readability due to the try-catch, I think the code is least confusing kept this way
        // TODO: change address in CW2
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(Constants.DEFAULT_BASE_ADDRESS);
        try { // Make sure we don't crash if we somehow get a null value in restaurants
            assert restaurants != null;
        } catch (AssertionError e) {
            return false;
        }

        return calcTotal(restaurants, this.orderItems) + 100 == this.priceTotalInPence;
    }

}

