package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.MalformedURLException;
import java.io.IOException;
import java.net.URL;

public class RESTClient {
    static Restaurant[] getRestaurantFromURL(URL baseURL) {
        try {
            return new ObjectMapper().readValue(new URL(baseURL + "restaurants/"), Restaurant[].class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
