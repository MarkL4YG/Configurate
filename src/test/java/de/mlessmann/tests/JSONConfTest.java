package de.mlessmann.tests;

import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.MD5;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Created by Life4YourGames on 22.08.16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JSONConfTest {

    ConfigNode rootNode;
    JSONConfigLoader loader;
    File file;
    byte[] originMD5;
    byte[] originMD5_2;

    @Before
    public void setUp() throws IOException {

        ClassLoader cLoader = this.getClass().getClassLoader();
        String file = cLoader.getResource("conf/test.json").getFile();
        originMD5 = MD5.md5File(file);
        originMD5_2 = MD5.md5File(cLoader.getResource("conf/test2.json").getFile());

        this.file = new File(file);

        loader = new JSONConfigLoader();

        rootNode = loader.loadFromFile(file);
    }

    @Test
    public void test_1_Name() {
        assertEquals("hi", rootNode.getNode("name").getString());
    }

    @Test
    public void test_2_Node() {

        ConfigNode testNode = rootNode.getNode("testNode");

        List l = testNode.getNode("array").getList();
        assertArrayEquals(new String[]{"hi", "h2", "h3"}, l.toArray());

        assertEquals((Integer) 256, testNode.getNode("int").getInt());
    }

    @Test
    public void test_3_Misc() {

        assertEquals(Boolean.TRUE, rootNode.getNode("bool").getBoolean());
        assertEquals(Double.valueOf(230394.4329), rootNode.getNode("double").getDouble());
    }

    @Test
    public void test_4_Virtual() {

        assertFalse(rootNode.hasNode("test"));
        assertTrue(rootNode.getNode("test").isVirtual());

        Boolean t = rootNode.getNode("test").optBoolean(true);
        assertTrue(t);

        rootNode.clean();
        assertFalse(rootNode.hasNode("test"));
    }

    @Test
    public void test_5_newVirtual() throws IOException {

        ConfigNode n = rootNode.getNode("lol", "this", "is", "a", "tree", "bool");

        n.setBoolean(Boolean.TRUE);

        n.getParent().getNode("int").setInt(2);
        n.getParent().getParent().getNode("meow").setString("~nya");

        assertTrue(rootNode.getNode("lol", "this", "is", "a", "tree", "bool").optBoolean(false));

        loader.saveTo(rootNode, new File("test_out_2.json"));

        byte[] resMD5 = MD5.md5File("test_out_2.json");

        Logger.getGlobal().log(Level.INFO, "2_Orig MD5: " + MD5.digestToString(originMD5_2));
        Logger.getGlobal().log(Level.INFO, "2_Outp MD5: " + MD5.digestToString(resMD5));
    }

    @Test
    public void test_6_Save() throws IOException {

        loader.saveTo(rootNode, new File("test_out.json"));

        byte[] resMD5 = MD5.md5File("test_out.json");

        Logger.getGlobal().log(Level.INFO, "Orig MD5: " + MD5.digestToString(originMD5));
        Logger.getGlobal().log(Level.INFO, "Outp MD5: " + MD5.digestToString(resMD5));

        //assertArrayEquals(originMD5, resMD5);
    }


}
