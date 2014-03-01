package com.github.grimpy.jenkins.plugins.lxc;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;

@Extension
public class LXCComputerListener extends ComputerListener {

    @Override
    public void onOnline(Computer c, TaskListener listener) {
        if(c instanceof LXCComputer){
            ((LXCComputer) c).onConnected();
        }
    }
}
