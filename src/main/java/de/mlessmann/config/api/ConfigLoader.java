package de.mlessmann.config.api;

import de.mlessmann.config.ConfigNode;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Created by Life4YourGames on 18.08.16.
 */
public interface ConfigLoader {

    ConfigNode load();
    ConfigNode loadFromFile(File file);
    ConfigNode loadFromFile(String filePath);

    void save(ConfigNode node);

    void setFile(File file);
    File getFile();

    boolean hasError();

    Exception getError();

    void resetError();

    Charset getEncoding();
    void setEncoding(Charset set);
}
