package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue;

import com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.Shared;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.Room;
import io.github.zeroone3010.yahueapi.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class HueRainbow {
    private static final Logger log = LoggerFactory.getLogger(HueRainbow.class);
    private static final float HUE_INCREMENT = (float) 0.001;
    private static final float FIXED_BRIGHTNESS = 1;
    private static final float FIXED_SATURATION = 1;
    private static float currentHueNumber = 0;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        if (args.length < 2) {
            log.error("You must specify the following parameters (in order):");
            log.error("  - The Hue bridge's username/API key");
            log.error("  - The Hue bridge's light names and/or room names (in quotes if the name contains spaces)");
            log.error("");
            log.error("Example: ./hue-rainbow.sh USERNAME \"Light 1\" \"Light 2\" Basement");
            log.error("");
            log.error("You must specify at least one light but may specify any number of lights by adding more parameters");
            log.error("");
            log.error("NOTE: The Hue bridge's IP address is automatically discovered");
            log.error("NOTE: Light names and room names can be mixed");
            log.error("NOTE: Light names and room names are case-insensitive");

            System.exit(1);
        }

        HueShared.setUsername(args[0]);

        List<String> originalLightAndRoomList = Arrays.stream(args)
                // Skip the username argument
                .skip(1)
                .collect(Collectors.toList());

        List<String> caseInsensitiveLightAndRoomList = originalLightAndRoomList.stream()
                // Make all strings lowercase for case insensitive matching and return a list
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<Light> lightsByName = HueShared.getLights().stream()
                // Find all the matching lights
                .filter(light -> caseInsensitiveLightAndRoomList.contains(light.getName().toLowerCase()))
                .collect(Collectors.toList());

        List<Light> lightsByRoomName = HueShared.getRooms().stream()
                // Find all the matching rooms
                .filter(room -> caseInsensitiveLightAndRoomList.contains(room.getName().toLowerCase()))
                // Extract all of the lights from those rooms
                .map(Room::getLights)
                // Flatten them into a single stream and grab the entire list
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Light> lights = new ArrayList<>();
        lights.addAll(lightsByName);
        lights.addAll(lightsByRoomName);

        if (lights.size() == 0) {
            log.error("No lights or rooms found that matched the specified names [" + String.join(", ", originalLightAndRoomList) + "]");
            System.exit(1);
        }

        log.info("Rainbow with " + lights.size() + " light(s) starting now. Press CTRL-C to stop.");

        while (true) {
            Color currentColor = getNextColor();
            lights.forEach(light -> HueRainbow.setNextColor(light, currentColor));
            Shared.sleep(250);
        }
    }

    private static Color getNextColor() {
        currentHueNumber += HUE_INCREMENT;

        if (currentHueNumber > 1) {
            currentHueNumber = 0;
        }

        log.info("A: " + currentHueNumber);

        return Color.getHSBColor(currentHueNumber, FIXED_SATURATION, FIXED_BRIGHTNESS);
    }

    private static void setNextColor(Light light, Color color) {
        light.setState(State.builder().color(color).keepCurrentState());
    }
}
