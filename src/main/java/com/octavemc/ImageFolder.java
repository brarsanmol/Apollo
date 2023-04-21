package com.octavemc;

import lombok.Getter;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;

//TODO: Create images manager, store a set of files, and use this as a base?
public class ImageFolder {

    @Getter
    private BufferedImage gopple;

    public ImageFolder() {
        this.directory = new File(Apollo.getInstance().getDataFolder(), "images");
        if (!this.directory.exists() && this.directory.mkdir()) Apollo.getInstance().getLogger().log(Level.INFO, "Created images directory");

        gopple = load("gapple.png");
    }

    private final File directory;

    @SneakyThrows
    public BufferedImage load(String identifier) {
        var file = new File(directory, identifier);
        if (file.exists()) return ImageIO.read(file);
        return ImageIO.read(Apollo.getInstance().getResource(directory.getName() + "/" + file.getName()));
    }
}
