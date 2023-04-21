package com.octavemc.eventgame;

import com.octavemc.Apollo;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Class that can handle schedules for game events.
 */
public final class EventScheduler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy,MM,dd,hh,mm,a", Locale.ENGLISH);
    private static final String FILE_NAME = "event-schedules.txt";

    @Getter
    private final Map<LocalDateTime, String> schedule;

    @SneakyThrows
    public EventScheduler() {
        this.schedule = new LinkedHashMap<>();
        /*Files.readAllLines(new File(Apollo.getInstance().getDataFolder(), FILE_NAME).toPath()).stream()
                .filter(line -> !line.startsWith("#"))
                .forEach(line -> {
                    var arguments = line.split(":");
                    if (arguments.length == 2) this.schedule.put(LocalDateTime.parse(arguments[0], DATE_TIME_FORMATTER), arguments[1]);
                });*/
    }
}
