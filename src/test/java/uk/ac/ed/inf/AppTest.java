package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void outsidenotInCentral()
    {
        assertFalse(new LngLat(-4.188, 55.944).inCentralArea());
    }

    @Test
    public void cornerNotInCentral()
    {
        assertFalse(new LngLat(-3.192473, 55.946233).inCentralArea());
    }

    @Test
    public void edgeNotInCentral()
    {
        assertFalse(new LngLat(-3.188, 55.946233).inCentralArea());
    }

    @Test
    public void insideInCentral()
    {
        assertTrue(new LngLat(-3.188, 55.944).inCentralArea());
    }

    @Test
    public void checkRestaurantSerialisation() throws IOException { // also tests Menu serialisation
        assertTrue(Restaurant.getRestaurantsFromRestServer(Constants.DEFAULT_BASE_ADDRESS).length > 0);
    }

    @Test
    public void checkOrderSerialisation() throws IOException {
        assertTrue(new ObjectMapper().readValue(new URL(Constants.DEFAULT_BASE_ADDRESS + "orders/"), Order[].class).length > 0);
    }

    @Test
    public void checkOrderSerialisationAndDeliveryCost() throws IOException, InvalidPizzaCombination {
        Order[] orders = new ObjectMapper().readValue(new URL(Constants.DEFAULT_BASE_ADDRESS + "orders/"), Order[].class);
        assertEquals(2600, orders[0].getDeliveryCost(Restaurant.getRestaurantsFromRestServer(Constants.DEFAULT_BASE_ADDRESS), orders[0].getOrderItems()));
    }

    @Test
    public void checkDistanceTo() {
        assertTrue(new LngLat(-3.192473, 55.946233).distanceTo(new LngLat(-3.184319, 55.942617)) > 3.192473 - 3.184319);
    }

//    @Test // we want the pizza combination method private, so we exclude this test after running it once
//    public void checkOrderSerialisationAndCombination() throws IOException, InvalidPizzaCombination {
//        Order[] orders = new ObjectMapper().readValue(new URL(Constants.DEFAULT_BASE_ADDRESS + "orders/"), Order[].class);
//        assertTrue(orders[0].checkPizzaCombination(Restaurant.getRestaurantsFromRestServer(Constants.DEFAULT_BASE_ADDRESS), orders[0].getOrderItems()));
//    }
}
