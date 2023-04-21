package com.octavemc.guice;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.octavemc.Apollo;
import org.bukkit.event.Listener;

public class ListenerRegistrar implements TypeListener {

    @Inject
    private Apollo instance;

    @Override
    public <I> void hear(TypeLiteral<I> literal, TypeEncounter<I> encounter) {
        encounter.register((InjectionListener<? super I>) i -> {
            if (i.getClass().isAssignableFrom(Listener.class))
                instance.getServer().getPluginManager().registerEvents((Listener) i, instance);
        });
    }
}
