package com.octavemc.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;
import com.octavemc.Apollo;
import com.octavemc.crates.CrateDao;
import com.octavemc.economy.merchants.MerchantDao;
import com.octavemc.faction.FactionDao;
import com.octavemc.guice.providers.YamlConfigurationProvider;
import com.octavemc.guice.providers.DatastoreProvider;
import com.octavemc.user.UserDao;
import dev.morphia.Datastore;
import org.bukkit.configuration.file.YamlConfiguration;

public class DaoModule extends AbstractModule {

    @Inject
    private Apollo instance;

    @Override
    protected void configure() {
        bind(Datastore.class).toProvider(DatastoreProvider.class).asEagerSingleton();
        bind(YamlConfiguration.class)
                .annotatedWith(Names.named("CratesYamlConfiguration"))
                .toProvider(new YamlConfigurationProvider(instance.getDataFolder(), "crates"))
                .asEagerSingleton();
        bind(YamlConfiguration.class)
                .annotatedWith(Names.named("MerchantsYamlConfiguration"))
                .toProvider(new YamlConfigurationProvider(instance.getDataFolder(), "merchants"))
                .asEagerSingleton();
        bind(UserDao.class).to(UserDao.class).asEagerSingleton();
        bind(FactionDao.class).to(FactionDao.class).asEagerSingleton();
        bind(CrateDao.class).to(CrateDao.class).asEagerSingleton();
        bind(MerchantDao.class).to(MerchantDao.class).asEagerSingleton();
    }
}
