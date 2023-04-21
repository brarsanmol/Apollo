package com.octavemc.visualise;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.bukkit.Location;

@Value
@AllArgsConstructor
public class VisualBlock {

    private final VisualType visualType;
    private final VisualBlockData blockData;
    private final Location location;
}
