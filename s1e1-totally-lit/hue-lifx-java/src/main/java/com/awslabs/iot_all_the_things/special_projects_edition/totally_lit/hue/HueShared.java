package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue;

import io.github.zeroone3010.yahueapi.Hue;
import io.github.zeroone3010.yahueapi.HueBridge;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.Room;
import io.github.zeroone3010.yahueapi.discovery.HueBridgeDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class HueShared {
    private static String username;
    private static Logger log = LoggerFactory.getLogger(HueShared.class);
    private static Optional<Hue> optionalHue = Optional.empty();

    public static void setUsername(String username) {
        HueShared.username = username;
    }

    public static Hue getHue() throws ExecutionException, InterruptedException {
        if (!optionalHue.isPresent()) {
            log.info("Discovering Hue bridge, this may take a moment...");
            optionalHue = Optional.of(new Hue(getHueBridgeIp(), username));
        }

        return optionalHue.get();
    }

    public static String getHueBridgeIp() throws ExecutionException, InterruptedException {
        Future<List<HueBridge>> bridgesFuture = new HueBridgeDiscoveryService()
                .discoverBridges(bridge -> log.info("Bridge found: " + bridge));

        final List<HueBridge> bridges = bridgesFuture.get();

        if (bridges.isEmpty()) {
            log.error("No Hue bridge found with auto-discovery");
            System.exit(1);
        } else if (bridges.size() > 1) {
            log.error("Multiple Hue bridges found with auto-discovery, this is currently unsupported!");
            System.exit(1);
        }

        return bridges.get(0).getIp();
    }

    public static List<Room> getRooms() throws ExecutionException, InterruptedException {
        return new ArrayList<>(getHue().getRooms());
    }

    public static List<Light> getLights() throws ExecutionException, InterruptedException {
        return getHue()
                // Get all of the rooms and turn it into a stream
                .getRooms().stream()
                // Get all of the lights for each room
                .map(Room::getLights)
                // Turn all of the lists of lights into a single stream of lights
                .flatMap(Collection::stream)
                // Collect them into a list
                .collect(Collectors.toList());
    }

    public static Map<String, List<String>> getLightMap() throws ExecutionException, InterruptedException {
        return getHue()
                // Get all of the rooms and turn it into a stream
                .getRooms().stream()
                // Create a map of the room name with each room's list of lights
                .collect(Collectors.toMap(Room::getName,
                        room -> new ArrayList<String>(room.getLights().stream().map(Light::getName).collect(Collectors.toList()))));
    }
}
