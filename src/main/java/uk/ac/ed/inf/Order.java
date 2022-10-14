package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    /**
     * Calculates the delivery cost given a list of restaurants and pizzas.
     *
     * @param restaurants The restaurants considered
     * @param pizzaNames  The names of the pizzas included
     * @return The sum of the prices of the pizzas, plus 1 pound delivery charge, in pence
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

    private List<Menu> getAllMenuItems(Restaurant[] restaurants) {
        return Arrays.stream(restaurants)
                .map(Restaurant::getMenu) // map to Arrays
                .map(Arrays::asList) // then to Lists
                .reduce(new ArrayList<Menu>(), (runningList, newMenuArray) -> { // concatenate
                    runningList.addAll(newMenuArray);
                    return runningList;
                });
    }

}

