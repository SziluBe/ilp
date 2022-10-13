package uk.ac.ed.inf;

public class Menu {
    public Menu(String name, int priceInPence){
        this.name = name;
        this.priceInPence = priceInPence;
    }

    private final String name;
    private final int priceInPence;

    public String getName() {
        return name;
    }

    public int getPriceInPence() {
        return priceInPence;
    }
}
