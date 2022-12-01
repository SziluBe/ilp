package uk.ac.ed.inf.Stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.Constants;
import uk.ac.ed.inf.Models.*;
import uk.ac.ed.inf.Models.Input.Area;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Input.Restaurant;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

public record ApplicationData(Restaurant[] restaurants, Order[] orders, Area[] noFlyZones, Area centralArea, LngLat deliveryOrigin) {
    // TODO: talk about how we use this instead of singletons in the report
    // TODO: validate input data, throw exceptions if invalid
    // TODO: mention that we use this for testing in the report
    public ApplicationData(@NotNull URL baseAddress, @NotNull String date, @NotNull LngLat deliveryOrigin, @NotNull ObjectMapper objectMapper) throws IOException {
        this(
                objectMapper.readValue(new URL(baseAddress + "restaurants/"), Restaurant[].class), // restaurants
                objectMapper.readValue(new URL(baseAddress + "orders/" + date), Order[].class), // orders
                Arrays.stream((objectMapper.readValue(new URL(baseAddress + "noFlyZones"), Area[].class))).toArray(Area[]::new), // noFlyZones
                new Area(Arrays.stream(objectMapper.readValue(new URL(baseAddress + "centralArea"), LngLat[].class)).toArray(LngLat[]::new)), // centralArea
                deliveryOrigin // deliveryOrigin
        );
    }

    public ApplicationData(@NotNull String[] args, ObjectMapper objectMapper) throws IOException, URISyntaxException {
        this(
                objectMapper.readValue(ApplicationData.urlFromArgs(args, "restaurants/"), Restaurant[].class), // restaurants
                objectMapper.readValue(ApplicationData.urlFromArgs(args, "orders/" + args[0]), Order[].class), // orders
                Arrays.stream((objectMapper.readValue(ApplicationData.urlFromArgs(args, "noFlyZones"), Area[].class))) // noFlyZones
                        .toArray(Area[]::new),
                new Area(
                        Arrays.stream(objectMapper.readValue(ApplicationData.urlFromArgs(args, "centralArea"), LngLat[].class)) // centralArea
                                .toArray(LngLat[]::new)
                ),
                Constants.AT // deliveryOrigin
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
        if(!baseAddress.startsWith("https://")) {
            baseAddress = "https://" + baseAddress;
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        return new URI(baseAddress + path).toURL();
    }
}
