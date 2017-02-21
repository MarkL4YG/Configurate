package de.mlessmann.config;

import de.mlessmann.config.api.ConfigLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Life4YourGames on 21.08.16.
 */
public class JSONConfigLoader implements ConfigLoader {

    private Exception error;
    private File file;

    public boolean hasError() { return error != null; }

    public Exception getError() { return error; }

    public void resetError() { error = null; }

    public void setFile(File file) { this.file = file; }

    public File getFile() { return file; }

    @Override
    public ConfigNode loadFromFile(String filePath) {
        return loadFromFile(new File(filePath));
    }

    @Override
    public ConfigNode loadFromFile(File file) {
        this.file = file;
        return load();
    }

    public ConfigNode load() {
        //New root node when the config does not exist
        if (this.file == null || !this.file.isFile()) return new ConfigNode();

        StringBuilder c = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            reader.lines()
                    .forEach(
                            l -> c.append(l.endsWith("\n") ? l : l + "\n" )
                    );
        } catch (IOException e) {
            error = e;
        }
        try {
            String s = c.toString().trim();
            JSONObject o;

            if (s.startsWith("[") && s.endsWith("]")) {
                JSONArray a = new JSONArray(s);
                o = new JSONObject();

                int i = 0;
                for (Object j : a) {
                    if (j instanceof JSONObject) {
                        o.put("obj_" + i++, j);
                    }
                }

            } else {
                o = new JSONObject(c.toString());
            }
            return fromJSON(o, null);

        } catch (JSONException e) {
            error = e;
        }
        return new ConfigNode();
    }

    public ConfigNode fromJSON(JSONObject o, ConfigNode parent) {
        ConfigNode node = parent;
        if (parent == null)
            node = new ConfigNode();

        for (String k : o.keySet()) {

            Object v = o.get(k);
            if (v instanceof JSONObject) {
                ConfigNode confNode = fromJSON((JSONObject) v, new ConfigNode(node, k));
            } else if (v instanceof JSONArray) {

                JSONArray arr = (JSONArray) v;
                ConfigNode confNode = new ConfigNode(node, k);
                ArrayList<Object> l = new ArrayList<Object>();

                for (int i = 0; i < arr.length(); i++) {
                    l.add(arr.get(i));
                }

                confNode.setList(l);
            } else {

                ConfigNode confNode = new ConfigNode(node, k);
                confNode.setValue(v);
            }
        }
        return node;
    }

    public void save(ConfigNode node) {
        saveTo(node, file);
    }

    public void saveTo(ConfigNode node, File file) {
        JSONObject j;
        if (!node.clean())
            j = nodeToJSON(node, null);
        else
            j = new JSONObject();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(j.toString(4));
            writer.flush();
        } catch (IOException e) {
            error = e;
        }
    }

    private JSONObject nodeToJSON(ConfigNode n, JSONObject parent) {

        if (parent == null)
            parent = new JSONObject();

        if (n.isHub()) {
            final JSONObject finalParent = parent;
            n.getHub().get().forEach((k, node) -> nodeToJSON(node, finalParent));
        } else if (n.isType(List.class)) {
            if (n.getParent() == null) throw new RuntimeException("Cannot serialize root nodes that aren't hubs");
            parent.put(n.getKey().get(), listToJSON(n.getList()));
        } else {
            if (n.getParent() == null) throw new RuntimeException("Cannot serialize root nodes that aren't hubs");
            parent.put(n.getKey().get(), n.getValue().get());
        }
        return parent;
    }

    private JSONArray listToJSON(List l) {
        JSONArray arr = new JSONArray();
        for (Object o : l) {
            arr.put(o);
        }
        return arr;
    }


}
