package com.octavemc.guice;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class CommandsModule extends AbstractModule {

    @Inject
    private PaperCommandManager manager;

    @Override
    protected void configure() {
    }
}
