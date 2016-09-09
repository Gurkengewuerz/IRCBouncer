package org.jibble.bouncer.ircbouncer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author gurkengewuerz.de
 */
public class Saver {

    String username = "UNKNOWN";
    JBouncer userBouncer = null;
    File f;

    public Saver(String username, JBouncer bouncer) {
        this.username = username;
        this.userBouncer = bouncer;
        File dir = new File("./saves");
        f = new File("./saves/" + username + ".dat");
        dir.mkdirs();
    }

    public String getUsername() {
        return username;
    }
    
    public String[] load() throws IOException {
        List<String> list = new ArrayList<>();
        if (f.exists()) {
            for (String line : Files.readAllLines(Paths.get(f.getPath()))) {
                list.add(line);
            }
        }
        return list.toArray(new String[0]);
    }

    public boolean override() throws IOException {
        if (f.exists()) {
            if (!f.delete()) {
                return false;
            }
        }

        FileWriter writer = new FileWriter(f);
        for (String str : userBouncer.getCSVs()) {
            writer.write(str + "\r\n");
        }
        writer.close();

        return true;
    }

    public boolean override(String[] list) throws IOException {
        if (f.exists()) {
            if (!f.delete()) {
                return false;
            }
        }

        FileWriter writer = new FileWriter(f);
        for (String str : list) {
            writer.write(str + "\r\n");
        }
        writer.close();

        return true;
    }

    public boolean remove(String where) throws IOException {
        String[] list = load();
        for (String s : list.clone()) {
            if (s.startsWith(where)) {
                list = (String[]) ArrayUtils.removeElement(list, s);
            }
        }
        override(list);
        return true;
    }

}
