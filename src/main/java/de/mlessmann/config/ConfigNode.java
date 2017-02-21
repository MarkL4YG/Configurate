package de.mlessmann.config;

import de.mlessmann.config.except.RootMustStayHubException;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Life4YourGames on 18.07.16.
 */
public class ConfigNode {

    private String key;
    private Object value;
    private ConfigNode parent;

    public ConfigNode() {
        //New empty root node
        value = new HashMap<String, ConfigNode>();
    }

    public ConfigNode(ConfigNode parent, String key) {
        this.parent = parent;
        this.key = key;
    }

    public ConfigNode(ConfigNode parent, String key, Object value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    public boolean isHub() { return value instanceof Map; }

    public Boolean isVirtual() { return value == null; }

    public Boolean isType(Class<?> cls) { return cls.isInstance(value); }

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
        if (!isHub()) {
            return value == null;
        } else {

            if (value == null)
                return true;
            Map<?, ?> m = (Map<?, ?>) value;

            if (m.isEmpty())
                return true;

            boolean toBeDeleted;

            int i;
            Object[] keys = m.keySet().toArray();
            for (i = m.size()-1; i>=0; i--) {
                toBeDeleted = false;
                Object k = keys[i];
                Object o = m.get(k);

                if (o instanceof ConfigNode) {
                    toBeDeleted = ((ConfigNode) o).clean();
                } else {
                    toBeDeleted = true;
                }

                if (toBeDeleted) {
                    m.remove(k);
                }
            }
            return m.isEmpty();
        }
    }

    public void addNode(ConfigNode node) {
        if (!(value instanceof Map))
            value = new HashMap<String, ConfigNode>();

        // Unless another dev poorly used #setValue this should never be a problem.
        //noinspection unchecked
        Map<Object, Object> m = (Map<Object, Object>) value;
        node.setParent(this);
        m.put(node.getKey(), node);
    }

    /**
     * Returns the deleted node
     * @param name Name of the node to delete
     * @return Nullable - Detached ConfigNode (aka. the node that has been deleted)
     */
    public ConfigNode delNode(String name) {
        if (hasNode(name)) {
            ConfigNode node = getNode(name);
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
        if (!isHub()) return;

        // Unless another dev poorly used #setValue this should never be a problem.
        //noinspection unchecked
        Map<Object, Object> m = (Map<Object, Object>) value;
        if (m.containsValue(node))
            m.remove(node.getKey());
    }

    public boolean hasNode(String node) {
        return isHub() && ((Map) value).containsKey(node);
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
        if (isHub()) {
            Map m = (Map) value;

            if (m.containsKey(key)) {
                if (m.get(key) instanceof ConfigNode) {
                    //Do nothing
                } else {
                    m.remove(key);
                    addNode(new ConfigNode(this, key, null));
                }
                return (ConfigNode) m.get(key);
            }
        }

        ConfigNode n = new ConfigNode(this, key, null);
        addNode(n);

        return n;
    }

    public Optional<List<String>> getKeys() {
        List<String> r = null;

        if (isHub()) {
            Map<?, ?> m = (Map<?, ?>) value;
            Set<?> s = m.keySet();
            r = new ArrayList<String>();
            final List<String> finalR = r;
            s.stream().filter(e1 -> e1 instanceof String).forEach(e2 -> finalR.add((String) e2));
        }

        return Optional.ofNullable(r);
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * USE THIS WITH CAUTION! IT CAN BREAK YOUR WHOLE CONFIG!
     * Rename the node to be a different one.
     * This unregisters itself from its parent, changes the key and then adds itself again.
     * @param key The new name of the node
     * @throws RootMustStayHubException Cannot rename root hub node
     * @deprecated just to make sure you know what you're doing
     */
    @Deprecated
    public void setKey(String key) throws RootMustStayHubException {
        if (parent == null) throw new RootMustStayHubException("Cannot set a key for a root node!");
        ConfigNode savedParent = parent;
        setParent(null);
        this.key = key;
        savedParent.addNode(this);
    }

    public String getKey() { return key; }

    public Object getValue() { return value; }

    public void setValue(Object value) throws RootMustStayHubException {
        if (key == null)
            throw new RootMustStayHubException();
        else
            this.value = value;
    }

    public String getString() { return (String) value; }
    public String optString(String def) { return value != null && (value instanceof String) ? getString() : def; }
    public void setString(String s) { value = s; }

    public Integer getInt() { return (Integer) value; }
    public Integer optInt(Integer def) { return value != null && (value instanceof Integer) ? getInt() : def; }
    public void setInt(Integer i) { value = i; }

    public Long getLong() { return (Long) value; }
    public Long optLong(Long def) { return value != null && (value instanceof Long) ? getLong() : def; }
    public void setLong(Long l) { value = l; }

    public Double getDouble() { return (Double) value; }
    public Double optDouble(Double def) { return value != null && (value instanceof Double) ? getDouble() : def; }
    public void setDouble(Double d) { value = d; }

    public Boolean getBoolean() { return (Boolean) value; }
    public Boolean optBoolean(Boolean def) { return value != null && (value instanceof Boolean) ? getBoolean() : def; }
    public void setBoolean(Boolean b) { value = b; }

    public BigInteger getBigInt() { return (BigInteger) value; }
    public BigInteger optBigInt(BigInteger def) { return value != null && (value instanceof BigInteger) ? getBigInt() : def; }
    public void setBigInt(BigInteger b) { value = b; }

    public <T> List<T> getList() { return (List<T>) value; }
    public <T> List<T> optList(List<T> def) {
        try {
            return value != null && (value instanceof List) ? getList() : def;
        } catch (ClassCastException e) {
            return def;
        }
    }
    public void setList(List<?> l) { value = l; }


    //------------------------------------------------------------------------------------------------------------------

    @Override
    public ConfigNode clone() {
        ConfigNode node = new ConfigNode(null, key);
        try {
            if (value instanceof Map<?, ?>) {
                Map<?, ?> m = ((Map<?, ?>) value);
                Exception[] es = new Exception[]{null};
                m.forEach((k, v) -> {
                    if (es[0]!=null)return;
                    if (v instanceof ConfigNode) {
                        node.addNode(((ConfigNode) v).clone());
                    } else {
                        try {
                            node.getNode((String) k).setValue(v);
                        } catch (Exception e) {
                            es[0] = e;
                        }
                    }
                });
                if (es[0] != null) throw new RuntimeException("Failed to clone node! Encountered unexpected Exception!", es[0]);
            } else {
                node.setValue(value);
            }
        } catch (RootMustStayHubException e) {
            throw new RuntimeException("Failed to clone node! Encountered unexpected RMSHException!", e);
        }
        return node;
    }
}
