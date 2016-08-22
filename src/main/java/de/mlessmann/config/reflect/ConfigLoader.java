package de.mlessmann.config.reflect;

import de.mlessmann.config.ConfigNode;

import java.io.File;
import java.io.IOException;

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
