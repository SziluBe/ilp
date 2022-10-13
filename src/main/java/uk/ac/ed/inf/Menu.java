package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Menu {
    @JsonCreator
    public Menu(@JsonProperty("name") String name, @JsonProperty("priceInPence") int priceInPence){
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
