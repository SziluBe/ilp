package uk.ac.ed.inf.Stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.Models.Input.Area;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Input.Restaurant;
import uk.ac.ed.inf.Models.LngLat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class to hold the data we get from the API
 */
public record ApplicationData(Restaurant[] restaurants, Order[] orders, Area[] noFlyZones, Area centralArea,
                              LngLat deliveryOrigin, String date) {
    /**
     * Default record constructor.
     *
     * @param Restaurant[] restaurants The restaurants.
     * @param Order[] orders The orders.
     * @param Area[] noFlyZones The no-fly zones.
     * @param Area centralArea The central area.
     * @param LngLat deliveryOrigin The delivery origin.
     * @param String date The date related to the data.
     */

    /**
     * The location of Appleton Tower.
     */
    public static final LngLat AT = new LngLat(-3.186874, 55.944494);

    /**
     * Constructor for ApplicationData to use for testing
     *
     * @param baseAddress    The base address of the API
     * @param date           The date to get data for
     * @param deliveryOrigin The place we are delivering to (AT)
     * @param objectMapper   The object mapper to use
     */
    public ApplicationData(@NotNull URL baseAddress, @NotNull String date, @NotNull LngLat deliveryOrigin, @NotNull ObjectMapper objectMapper) throws IOException {
        this(
                objectMapper.readValue(new URL(baseAddress + "restaurants/"), Restaurant[].class), // restaurants
                objectMapper.readValue(new URL(baseAddress + "orders/" + date), Order[].class), // orders
                Arrays.stream((objectMapper.readValue(new URL(baseAddress + "noFlyZones"), Area[].class))).toArray(Area[]::new), // noFlyZones
                new Area(Arrays.stream(objectMapper.readValue(new URL(baseAddress + "centralArea"), LngLat[].class)).toArray(LngLat[]::new)), // centralArea
                deliveryOrigin, // deliveryOrigin
                date // date
        );
    }

    /**
     * Constructor for ApplicationData to use in the main method
     *
     * @param args         The command line arguments
     * @param objectMapper The object mapper to use
     */
    public ApplicationData(@NotNull String[] args, ObjectMapper objectMapper) throws IOException, URISyntaxException {
        this(
                objectMapper.readValue(ApplicationData.urlFromArgs(args, "restaurants/"), Restaurant[].class), // restaurants
                objectMapper.readValue(ApplicationData.urlFromArgs(args, "orders/" + args[0]), Order[].class), // orders
                Arrays.stream((objectMapper.readValue(ApplicationData.urlFromArgs(args, "noFlyZones"), Area[].class))) // noFlyZones
                        .toArray(Area[]::new),
                // the Central Area does not conform to the GeoJSON spec on the server, so we need to
                // add the first point to the end of the array to make it a closed polygon
                readCentralArea(objectMapper, ApplicationData.urlFromArgs(args, "centralArea")), // centralArea
                AT, // deliveryOrigin
                args[0] // date
        );
    }

    @NotNull
    private static URL urlFromArgs(@NotNull String[] args, @NotNull String path) throws URISyntaxException, MalformedURLException {
        String baseAddress = args[1];
        if (!baseAddress.endsWith("/")) {
            baseAddress += "/";
        }
        if (baseAddress.startsWith("http://")) {
            // remove the http:// from the base address
            baseAddress = baseAddress.substring(7);
        }
        if (!baseAddress.startsWith("https://")) {
            baseAddress = "https://" + baseAddress;
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        return new URI(baseAddress + path).toURL();
    }

    private static Area readCentralArea(ObjectMapper objectMapper, URL url) throws IOException {
        ArrayList<LngLat> verticesList = Arrays.stream(objectMapper.readValue(url, LngLat[].class)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        verticesList.add(verticesList.get(0));
        return new Area(verticesList.toArray(new LngLat[0]));
    }
}
