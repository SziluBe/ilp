package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Menu {
    private final String name;
    private final int priceInPence;

    /**
     * Constructor annotated with @JsonCreator to enable Jackson de-serialisation
     *
     * @param name         The name of the menu item (pizza)
     * @param priceInPence The price of the menu item (pizza) in pence
     */
    @JsonCreator
    public Menu(@JsonProperty("name") String name, @JsonProperty("priceInPence") int priceInPence) {
        this.name = name;
        this.priceInPence = priceInPence;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @return priceInPence
     */
    public int getPriceInPence() {
        return priceInPence;
    }
}
