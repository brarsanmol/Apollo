package com.octavemc.tablist;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class TablistKeyGenerator {

    public final static char[] ALPHABET = new char[] {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T'
    };

    private final List<String> keys;

    public TablistKeyGenerator() {
        this.keys = new ArrayList<>();
        IntStream.range(0, 80).forEach(index -> {
            if (index < 20) this.keys.add("A-" + ALPHABET[index]);
            else if (index < 40) this.keys.add("B-" + ALPHABET[index - 20]);
            else if (index < 60) this.keys.add("C-" + ALPHABET[index - 40]);
            else if (index < 80) this.keys.add("D-" + ALPHABET[index - 60]);
        });
    }

    public String getNextKey(int index) {
        Preconditions.checkNotNull(index, "Index cannot be null");
        Preconditions.checkArgument(index > -1, "Index cannot be negative.");
        return this.keys.get(index);
    }

    public void clearKeys() {
        this.keys.clear();
    }
}
