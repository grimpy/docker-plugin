package com.github.grimpy.jenkins.plugins.lxc.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Shell {
    public static String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
 
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));
 
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            if (p.exitValue() != 0) {
                throw new RuntimeException(String.format("Failed to execute %s output: %s", command, output.toString()));
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return output.toString();
 
    }
}
