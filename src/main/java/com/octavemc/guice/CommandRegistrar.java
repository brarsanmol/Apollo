package com.octavemc.guice;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class CommandRegistrar implements TypeListener {

    @Inject
    private PaperCommandManager manager;

    @Override
    public <I> void hear(TypeLiteral<I> literal, TypeEncounter<I> encounter) {
        encounter.register((InjectionListener<? super I>) i -> {
            if (i.getClass().isAssignableFrom(BaseCommand.class))
                manager.registerCommand((BaseCommand) i);
        });
    }
}
