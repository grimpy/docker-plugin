package com.github.grimpy.jenkins.plugins.lxc.client;

public class Container {

    private String name;
    private String status = "STOPPED";
    
    public Container(String name) {
        this.name = name;
    }
    
    public void stop() {
        String command = String.format("sudo lxc-stop -n %s", name);
        Shell.executeCommand(command);
    }
    
    public void start() {
        String command = String.format("sudo lxc-start -d -n %s", name);
        Shell.executeCommand(command);
        command = String.format("sudo lxc-wait -n %s -s RUNNING", name);
        Shell.executeCommand(command);
        long now = new java.util.Date().getTime()/1000;
        while (now + 30 > new java.util.Date().getTime()/1000) {
            String ip = getIP();
            if (ip != null){
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public String getStatus() {
        return status;
    }
    
    public void remove() {
        String command = String.format("sudo lxc-destroy -fn %s", name);
        Shell.executeCommand(command);
    }
    
    public String getName() {
        return name;
    }
    
    public String getIP() {
        String command = String.format("sudo lxc-ls --fancy --fancy-format ipv4 %s", name);
        String output = Shell.executeCommand(command);
        String[] lines = output.split("\n");
        if (lines.length > 0){
            String ip = lines[lines.length-1].trim();
            if (ip.equals("-")) {
                return null;
            }
            return ip;
        }
        return null;
    }

}
