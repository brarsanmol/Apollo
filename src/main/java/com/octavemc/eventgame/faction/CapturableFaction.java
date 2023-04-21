package com.octavemc.eventgame.faction;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class CapturableFaction extends EventFaction {


    public CapturableFaction(String name, String crate) {
        super(name, crate);
    }

}
