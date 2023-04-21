package com.octavemc.guice;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.octavemc.Apollo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PluginModule extends AbstractModule {

    private final Apollo instance;

    @Override
    protected void configure() {
        super.bindListener(Matchers.any(), new AfterInitializationListener());
        bind(Apollo.class).toInstance(instance);
        bind(PaperCommandManager.class).toInstance(new PaperCommandManager(instance));
    }
}
