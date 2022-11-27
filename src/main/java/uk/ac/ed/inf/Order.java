package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Order {
    private final String orderNo;
    private final String orderDate;
    private final String customer;
    private final String creditCardNumber;
    private final String creditCardExpiry;
    private final String cvv;
    private final int priceTotalInPence;
    private final String[] orderItems;
    private boolean delivered;

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
        this.orderItems = orderItems;
        this.delivered = false;
    }

    /**
     * @return Whether the order has been delivered
     */
    public boolean isDelivered() {
        return delivered;
    }

    /**
     * @param delivered Whether the order has been delivered
     */
    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
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

    public OrderOutcome validateOrder(URL serverBaseAddress) throws IOException {
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(serverBaseAddress);

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

        if (this.delivered) {
            return OrderOutcome.Delivered;
        }
        return OrderOutcome.ValidButNotDelivered;
    }

    /**
     * Calculates the delivery cost given a list of restaurants and pizzas.
     *
     * @param restaurants The restaurants considered
     * @return The sum of the prices of the pizzas, plus 1 pound delivery charge, in pence
     * @throws InvalidPizzaCombination Thrown if invalid combination of pizzas/restaurants provided.
     */
    public int getDeliveryCost(Restaurant[] restaurants) throws InvalidPizzaCombination {
        try { // Make sure we don't crash if we somehow get a null value in restaurants
            assert restaurants != null;
        } catch (AssertionError e) {
            return -1;
        }

        if (!checkPizzaCombination(restaurants)) {
            throw new InvalidPizzaCombination();
        } // no need for `else` since we are throwing an exception in the body of the if statement

        return calcTotal(restaurants) + Constants.DELIVERY_CHARGE;
    }

    private boolean checkPizzaCombination(Restaurant[] restaurants) {
        Set<String> orderItemsSet = new HashSet<>(Arrays.asList(this.orderItems));

        try { // Make sure we don't crash if we somehow get a null value in restaurants
            assert restaurants != null;
        } catch (AssertionError e) {
            return false;
        }

        return Arrays.stream(restaurants).anyMatch(restaurant -> {
            Set<String> menuItemNames = new HashSet<>(Arrays.stream(restaurant.getMenu())
                    .map(Menu::getName)
                    .toList()); // create set of pizza names available at each restaurant

            menuItemNames.retainAll(orderItemsSet); // find intersection between order and pizzas available (names)

            return menuItemNames.size() == orderItemsSet.size(); // if the order is valid, all pizzas must come from the same restaurant
        });

    }

    private int calcTotal(Restaurant[] restaurants) {
        List<Menu> allMenuItems = getAllMenuItems(restaurants);

        Set<String> pizzaNamesSet = new HashSet<>(Arrays.asList(this.orderItems)); // for faster lookup

        return allMenuItems.stream()
                .filter(menuItem -> { // filter for pizzas whose name appears in the list of names
                    return pizzaNamesSet.contains(menuItem.getName());
                })
                .map(Menu::getPriceInPence) // get their prices
                .reduce(0, Integer::sum);
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

    /**
     * Returns the current array of available orders from the 'orders/' endpoint of the given base address
     *
     * @param serverBaseAddress The base URL of the REST endpoint
     * @return The existing orders de-serialised as an array of Order objects
     * @throws IOException In case there is an issue retrieving the data
     */
//    public static Order[] getOrdersFromRestServer(URL serverBaseAddress, String date) throws IOException {
//        return
//    }

//    /**
//     * Returns the current array of available orders on a given date from the 'orders/YYYY-MM-DD' endpoint of the given base address
//     *
//     * @param serverBaseAddress The base URL of the REST endpoint
//     * @return The existing orders de-serialised as an array of Order objects
//     * @throws IOException In case there is an issue retrieving the data
//     */
//    static Order[] getOrdersFromRestServerByDate(URL serverBaseAddress, String date) throws IOException {
//        return Constants.MAPPER.readValue(new URL(serverBaseAddress + "orders/" + date), Order[].class);
//    }


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

    private boolean checkExpiryDate() {
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
        int l = this.orderItems.length;
        return 0 < l && l < 5;
    }

    private boolean checkPizzasDefined(Restaurant[] restaurants) throws IOException {
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

    private boolean checkTotal(Restaurant[] restaurants) throws IOException {
        // difficult to remove code duplication and also retain readability due to the try-catch, I think the code is least confusing kept this way
        try { // Make sure we don't crash if we somehow get a null value in restaurants
            assert restaurants != null;
        } catch (AssertionError e) {
            return false;
        }

        return calcTotal(restaurants) + 100 == this.priceTotalInPence;
    }

}

