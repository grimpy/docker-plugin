package com.github.grimpy.jenkins.plugins.lxc;


import com.github.grimpy.jenkins.plugins.lxc.client.Container;
import com.google.common.base.Preconditions;

import hudson.Extension;
import hudson.model.*;
import hudson.plugins.sshslaves.SSHConnector;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import hudson.model.TaskListener;
import jenkins.model.Jenkins;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * {@link hudson.slaves.ComputerLauncher} for Docker that waits for the instance to really come up before proceeding to
 * the real user-specified {@link hudson.slaves.ComputerLauncher}.
 *
 * @author Kohsuke Kawaguchi
 */
public class LXCComputerLauncher extends ComputerLauncher {

    private static final Logger LOGGER = Logger.getLogger(LXCComputerLauncher.class.getName());


    public final Container detail;
    public final LXCTemplate template;

    public LXCComputerLauncher(LXCTemplate template, Container container) {
        Preconditions.checkNotNull(template);
        Preconditions.checkNotNull(container);

        this.template = template;
        this.detail = container;
    }

    @Override
    public void launch(SlaveComputer _computer, TaskListener listener) throws IOException, InterruptedException {

        for(int tries=0;tries < 3; tries++) {
        SSHLauncher launcher = getSSHLauncher();
        launcher.launch(_computer, listener);

            if( launcher.getConnection() != null ) {
                LOGGER.log(Level.INFO, "Launched " + _computer);
                return;
            }

            Thread.sleep(5000 * (tries+1));
        }

            LOGGER.log(Level.WARNING, "Couldn't launch Docker template. Closing.");
            LXCComputer dc = (LXCComputer)_computer;
            dc.getNode().terminate();
        }

    public SSHLauncher getSSHLauncher() throws MalformedURLException {

        int port;

        try {
            port = 22;
        } catch(NullPointerException ex) {
            throw new RuntimeException("No mapped port 22 in host for SSL. Config=" + detail);
        }
        String host = detail.getIP();
        
        LOGGER.log(Level.INFO, "Creating slave SSH launcher for " + host + ":" + port);

        return new SSHLauncher(host, port, template.credentialsId, template.jvmOptions , template.javaPath, template.prefixStartSlaveCmd, template.suffixStartSlaveCmd);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ComputerLauncher> {

        /**
         * {@inheritDoc}
         */
        public String getDisplayName() {
            return "Docker SSH Launcher";
        }

        public Class getSshConnectorClass() {
            return SSHConnector.class;
        }

        /**
         * Delegates the help link to the {@link SSHConnector}.
         */
        @Override
        public String getHelpFile(String fieldName) {
            String n = super.getHelpFile(fieldName);
            if (n==null)
                n = Jenkins.getInstance().getDescriptor(SSHConnector.class).getHelpFile(fieldName);
            return n;
        }
    }
}