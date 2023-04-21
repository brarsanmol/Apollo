package com.octavemc.user;

import com.octavemc.Apollo;
import com.octavemc.deathban.Deathban;
import dev.morphia.annotations.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;

@NoArgsConstructor
@Data
@Entity
public class User {

    @Id private UUID identifier;
    private Deathban deathban;
    private int balance;
    private int lives;
    @Transient private long lastFactionLeaveMillis;
    @Transient private boolean showClaimMap;

    public User(UUID identifier) {
        this.identifier = identifier;
    }

    public Player getPlayer() {
        return Apollo.getInstance().getServer().getPlayer(this.identifier);
    }

    public boolean hasDeathban() {
        return this.deathban != null && this.deathban.isActive();
    }
}
