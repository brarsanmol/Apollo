package com.octavemc.timer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a {@link Timer}, used to manage cooldowns.
 */
@AllArgsConstructor
public abstract class Timer {

    @Getter
    protected final String name;
    protected final long defaultCooldown;

}