package de.mlessmann.config;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Life4YourGames on 18.07.16.
 */
public class ConfigNode {

    private Map<String, ConfigNode> hub;
    private Object value;
    private ConfigNode parent;

    public ConfigNode() {
        //New empty root node
        hub = new HashMap<String, ConfigNode>();
        value = null;
    }

    public ConfigNode(ConfigNode parent, String key) {
        this();
        this.parent = parent;
        parent.addNode(key, this);
    }

    public ConfigNode(ConfigNode parent, String key, Object value) {
        this(parent, key);
        this.value = value;
        this.hub = null;
    }

    public boolean hasMapChildren() { return hub != null; }

    public Boolean isVirtual() { return hub == null && value == null; }

    public Boolean isType(Class<?> cls) {
        return value != null && !hasMapChildren() && cls.isInstance(value);
    }

    public Boolean defaultValue(Object o) {
        if (o == null || (isType(o.getClass()) && !isType(List.class))) {
            return false;
        }
        if (isType(List.class)) {
            if (List.class.isAssignableFrom(o.getClass()))
                return false;
        }
        setValue(o);
        return true;
    }

    public ConfigNode getParent() {
        return parent;
    }

    public ConfigNode getRoot() {
        if (parent!=null)
            return getParent().getRoot();
        return this;
    }

    //------------------------------------------------------------------------------------------------------------------

    public boolean clean() {
        if (!hasMapChildren()) {
            return isVirtual();
        } else {
            if (hub.isEmpty())
                return true;

            int i;
            String[] keys = hub.keySet().toArray(new String[hub.keySet().size()]);
            for (i = hub.size()-1; i>=0; i--) {
                String k = keys[i];
                ConfigNode node = hub.get(k);
                if (node.clean()) {
                    hub.remove(k);
                }
            }
            return hub.isEmpty();
        }
    }

    public void addNode(String key, ConfigNode node) {
        if (hub == null) {
            hub = new HashMap<String, ConfigNode>();
            value = null;
        }
        //Attach node
        node.setParent(this);
        hub.put(key, node);
    }

    /**
     * Returns the deleted node
     * @param name Name of the node to delete
     * @return Nullable - Detached ConfigNode (aka. the node that has been deleted)
     */
    public ConfigNode delNode(String name) {
        if (hasNode(name)) {
            ConfigNode node = getNode(name);
            //Detach node so GC can collect it
            node.setParent(null);
            return node;
        }
        return null;
    }

    protected void setParent(ConfigNode parent) {
        if (this.parent!=null)
            this.parent.unregisterNode(this);
        this.parent = parent;
    }

    protected void unregisterNode(ConfigNode node) {
        if (!hasMapChildren()) return;
        if (hub.containsValue(node))
            hub.remove(node.getKey().get());
    }

    public boolean hasNode(String key) {
        return hasMapChildren() && hub.containsKey(key);
    }

    public ConfigNode getNode(String... keys) {
        if (keys.length < 1) return this;

        ConfigNode node = getOrCreateNode(keys[0]);

        if (keys.length > 1) {
            String[] newKeys = Arrays.copyOfRange(keys, 1, keys.length);

            return node.getNode(newKeys);
        } else {
            return node;
        }
    }

    public ConfigNode getNode(String path, char divider) {
        return getNode(path.split("["+divider+"]"));
    }

    private ConfigNode getOrCreateNode(String key) {
        if (!hasMapChildren()) {
            hub = new HashMap<String, ConfigNode>();
            value = null;
        }
        if (!hub.containsKey(key)) {
            //Node will attach itself
            new ConfigNode(this, key, null);
            return getOrCreateNode(key);
        } else {
            return hub.get(key);
        }
    }

    public Optional<List<String>> getKeys() {
        List<String> r = null;

        if (hasMapChildren()) {
            Set<String> s = hub.keySet();
            r = new ArrayList<String>();
            r.addAll(s);
        }

        return Optional.ofNullable(r);
    }

    //------------------------------------------------------------------------------------------------------------------


    public Optional<String> getKey(ConfigNode node) {
        if (hasMapChildren()) {
            Set<String> keys = hub.keySet();
            for (String key : keys) {
                if (hub.get(key) == node) return Optional.of(key);
            }
        }
        return Optional.empty();
    }

    public Optional<String> getKey() {
        if (parent!=null) {
            return parent.getKey(this);
        }
        return Optional.empty();
    }

    public Optional<Object> getValue() { return Optional.ofNullable(value); }
    public Optional<Map<String, ConfigNode>> getHub() { return Optional.ofNullable(hub); }

    public void setValue(Object value) {
        if (hub != null) {
            //Prevent CME when changing from hub to endNode
            Map<String, ConfigNode> oHub = hub;
            hub = null;
            oHub.forEach((k, v) -> v.setParent(null));
        }
        this.value = value;
    }

    public String getString() { return (String) value; }
    public String optString(String def) { return value != null && (value instanceof String) ? getString() : def; }
    public void setString(String s) { setValue(s); }

    public Integer getInt() { return (Integer) value; }
    public Integer optInt(Integer def) { return value != null && (value instanceof Integer) ? getInt() : def; }
    public void setInt(Integer i) { setValue(i);}

    public Long getLong() { return (Long) value; }
    public Long optLong(Long def) { return value != null && (value instanceof Long) ? getLong() : def; }
    public void setLong(Long l) { setValue(l); }

    public Double getDouble() { return (Double) value; }
    public Double optDouble(Double def) { return value != null && (value instanceof Double) ? getDouble() : def; }
    public void setDouble(Double d) { setValue(d); }

    public Boolean getBoolean() { return (Boolean) value; }
    public Boolean optBoolean(Boolean def) { return value != null && (value instanceof Boolean) ? getBoolean() : def; }
    public void setBoolean(Boolean b) { setValue(b); }

    public BigInteger getBigInt() { return (BigInteger) value; }
    public BigInteger optBigInt(BigInteger def) { return value != null && (value instanceof BigInteger) ? getBigInt() : def; }
    public void setBigInt(BigInteger b) { setValue(b); }

    public <T> List<T> getList() { return (List<T>) value; }
    public <T> List<T> optList(List<T> def) {
        try {
            return value != null && (value instanceof List) ? getList() : def;
        } catch (ClassCastException e) {
            return def;
        }
    }
    public void setList(List<?> l) { setValue(l); }


    //------------------------------------------------------------------------------------------------------------------

    @Override
    public ConfigNode clone() {
        ConfigNode node = new ConfigNode();

        if (hasMapChildren()) {
            Exception[] es = new Exception[]{null};
            hub.forEach((k, v) -> {
                if (es[0] != null) return;
                node.addNode(k, v.clone());
            });
            if (es[0] != null)
                throw new RuntimeException("Failed to clone node! Encountered unexpected Exception!", es[0]);
        } else {
            node.setValue(value);
        }
        return node;
    }
}
