package com.octavemc.guice.providers;

import com.google.inject.Provider;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@AllArgsConstructor
public class YamlConfigurationProvider implements Provider<YamlConfiguration> {

    private File folder;
    private String name;

    @Override
    public YamlConfiguration get() {
        return YamlConfiguration.loadConfiguration(new File(this.folder, name + ".yml"));
    }
}
