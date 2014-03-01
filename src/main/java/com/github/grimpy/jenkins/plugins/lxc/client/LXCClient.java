package com.github.grimpy.jenkins.plugins.lxc.client;

import java.util.ArrayList;


public class LXCClient {
    
    public ArrayList<Container> listContainers() {
        return new ArrayList<Container>();
    }
    
    public Container getContainer(String name) {
        return new Container(name);
    }
    
    public String getVersion(){
        return "1.0";
    }
    
    public Container clone(String origin, String name, String cloneOptions) {
        //run clone command
        Shell.executeCommand(String.format("sudo lxc-clone %s -o %s -n %s", cloneOptions, origin, name));
        return new Container(name);
    }
}
