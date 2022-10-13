package uk.ac.ed.inf;

import java.io.IOException;
import java.util.*;

public class Order {
    public Order(String orderNo, String orderDate, String customer, String creditCardNumber, String creditCardExpiry, String cvv, int priceTotalInPence, String[] orderItems) {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.customer = customer;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
        this.priceTotalInPence = priceTotalInPence;
        this.orderItems = orderItems;
    }

    private final String orderNo;
    private final String orderDate;
    private final String customer;
    private final String creditCardNumber;
    private final String creditCardExpiry;
    private final String cvv;
    private final int priceTotalInPence;
    private final String[] orderItems;

    public int getDeliveryCost(Restaurant[] restaurants, String... pizzaNames) throws InvalidPizzaCombination {
        if (!checkPizzaCombination(restaurants, pizzaNames)) {
            throw new InvalidPizzaCombination();
        } // no need for `else` since we are throwing an exception in the body of the if statement

        return calcTotal(restaurants, pizzaNames) + Constants.DELIVERY_CHARGE;
    }

    private boolean checkPizzaCombination(Restaurant[] restaurants, String[] pizzaNames) {
        Set<String> orderItemsSet = new HashSet<>(Arrays.asList(pizzaNames));

        List<Restaurant> restaurantList = Arrays.stream(restaurants)
                .filter(restaurant -> {
                    Set<String> menuItemNames = new HashSet<>(Arrays.stream(restaurant.getMenu())
                            .map(Menu::getName)
                            .toList()); // create set of pizza names available at each restaurant

                    menuItemNames.retainAll(orderItemsSet); // find intersection between order and pizzas available (names)

                    return menuItemNames.size() > 0; // discard any restaurant that we haven't ordered pizzas from
                })
                .toList();

        return restaurantList.size() == 1; // make sure the order only has pizzas from exactly one restaurant
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
            boolean checkFormat = this.creditCardExpiry.matches("(10|11|12|0[1-9])\\/(2[3-9]|[3-9][0-9])");  // Technically this will become wrong if we change centuries, but for the time being it is safer.
                                                                                                                   // This is a reminder to change it if we get to that. Sorry 2300s developers, and hello from the past :)
            // No need to check for the edge case where we are in 2023 but before the service's starting date since it starts on January 1st :)
            return checkFormat;
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

        return calcTotal(restaurants, this.orderItems) == this.priceTotalInPence;
    }

}

