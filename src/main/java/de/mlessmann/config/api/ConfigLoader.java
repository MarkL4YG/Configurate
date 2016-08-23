package de.mlessmann.config.api;

import de.mlessmann.config.ConfigNode;

import java.io.File;

/**
 * Created by Life4YourGames on 18.08.16.
 */
public interface ConfigLoader {

    ConfigNode loadFromFile(String filePath);

    ConfigNode loadFromFile(File file);

    void save(ConfigNode node);

    File getFile();

    boolean hasError();

    Exception getError();

    void resetError();

}
