package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.lifx;

import com.github.besherman.lifx.LFXClient;
import com.github.besherman.lifx.LFXLight;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LiFXShared {
    private static final Logger log = LoggerFactory.getLogger(LiFXShared.class);
    public static final LFXClient client = new LFXClient();

    public static void initializeLiFXClient() {
        // Open a blocking LiFX client and fail immediately if this blows up
        Try.run(() -> client.open(true)).get();
    }
    public static void requireAtLeastOneLight() {
        // Make sure there are some lights. If not give the user some hints on what might be wrong.
        if (getLightCount() == 0) {
            log.error("No LiFX lights detected. If you have LiFX lights on your network try turning off your firewall and run this program again.");
            log.error("");
            log.error("On MacOS you can disable your firewall by running this command: sudo pfctl -F all");
            System.exit(1);
        }
    }

    private static long getLightCount() {
        return getLightStream().count();
    }

    public static Stream<LFXLight> getLightStream() {
        Iterator<LFXLight> lightIterator = client.getLights().iterator();
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(lightIterator, Spliterator.ORDERED), false);
    }
}