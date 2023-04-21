package com.octavemc.tablist;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class Tablist {

    private final Player player;
    private final List<TablistSlot> slots;

    public Tablist(@NonNull Player player) {
        this.player = player;
        this.slots = new ArrayList<>(80);
        IntStream.range(0, 80).forEach(slot -> this.slots.add(new TablistSlot(slot)));
    }

    @Deprecated
    public TablistSlot getSlot(int index) {
        Preconditions.checkArgument(index > -1 && index < 80, "Index out of bounds, the range is 0-79");
        return this.slots.get(index);
    }

    public TablistSlot getSlot(int column, int row) {
        return this.slots.get(column * 20 + row);
    }
}
