/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of JBouncer.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: JBouncerMain.java,v 1.3 2004/03/01 19:13:37 pjm2 Exp $

 */
package org.jibble.bouncer.ircbouncer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JBouncerMain {

    public static int HISTORY_LIMIT = 10;
    public static boolean TAKE_LOGS = false;
    public static String prefix = "Jibber";
    public static String realname = "Modified by Gurkengewuerz";
    static String tmp;

    public static void main(String[] args) throws Exception {

        Properties p = new Properties();
        try {
            p.load(new FileInputStream("./config.ini"));
        } catch (IOException e) {
            JBouncerManager.log("Could not read the config file.");
            System.exit(1);
        }

        String portStr = p.getProperty("Port");
        int port = 6667;
        if (portStr != null) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                // Keep the default value;
            }
        }

        String historyStr = p.getProperty("HistoryLimit");
        if (historyStr != null) {
            try {
                HISTORY_LIMIT = Integer.parseInt(historyStr);
            } catch (NumberFormatException e) {
                // Keep the default value;
            }
        }
        JBouncerManager.log("The message history limit for each session is " + HISTORY_LIMIT);

        String logStr = p.getProperty("TakeLogs");
        if (logStr != null) {
            TAKE_LOGS = Boolean.valueOf(logStr).booleanValue();
        }
        if (TAKE_LOGS) {
            JBouncerManager.log("Logging activated.");
        }

        tmp = p.getProperty("Prefix");
        if (tmp != null) {
            try {
                prefix = tmp;
            } catch (Exception e) {
                // Keep the default value;
            }
        }

        tmp = p.getProperty("RealName");
        if (tmp != null) {
            try {
                realname = tmp;
            } catch (Exception e) {
                // Keep the default value;
            }
        }

        // Populate the HashMap of bouncers (one per user).
        HashMap<User, JBouncer> bouncers = new HashMap();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./accounts.ini"));
            String line = null;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    String login = parts[0];
                    String password = parts[1];
                    User user = new User(login, password);
                    if (!login.startsWith("#") && !bouncers.containsKey(user)) {
                        JBouncer bouncer = new JBouncer(user);
                        bouncers.put(user, bouncer);
                        user.setSaver(bouncer);
                        JBouncerManager.log("Created bouncer for " + login);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Unable to process accounts.ini.");
            System.exit(1);
        }

        JBouncerManager manager = new JBouncerManager(bouncers);

        ClientListener listener = new ClientListener(manager, port);
        listener.start();

        JBouncerManager.log("*** JBouncer ready to accept connections on port " + port);

        bouncers.forEach((user, jBouncer) -> {
            if (user == null || jBouncer == null) return;
            String[] listServers;
            try {
                listServers = user.getSaver().load();
            } catch (IOException e) {
                Logger.getLogger(JBouncerMain.class.getName()).log(Level.SEVERE, null, e);
                return;
            }
            for (String s : listServers) {
                if (s.equals("")) {
                    continue;
                }
                String[] splittet = s.split(";");
                String name = splittet[0];
                String ip = splittet[1];
                String IPport = splittet[2];
                String passwort = splittet[3];
                if (passwort.equals("null")) {
                    passwort = null;
                }
                String hash = splittet[4];
                String channels = splittet[5];

                if (!jBouncer.isConntectedTo(name, ip, Integer.parseInt(IPport))) {
                    jBouncer.add(name, new ServerConnection(ip, Integer.parseInt(IPport), passwort, user.getLogin(), user, channels.split(",")));
                    JBouncerManager.log("Login to " + user.getLogin() + "@" + ip + ":" + IPport + " " + channels);
                }
            }
        });

    }
}
