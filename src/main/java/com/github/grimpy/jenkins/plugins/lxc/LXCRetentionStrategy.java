package com.github.grimpy.jenkins.plugins.lxc;

import hudson.model.Descriptor;
import hudson.slaves.RetentionStrategy;
import hudson.util.TimeUnit2;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LXCRetentionStrategy  extends RetentionStrategy<LXCComputer> {



    @DataBoundConstructor
    public LXCRetentionStrategy() {

    }

    @Override
    public synchronized long check(LXCComputer c) {
        LOGGER.log(Level.INFO, "Checking " + c);
        if (c.isIdle() && c.isOnline() && !disabled && c.haveWeRunAnyJobs()) {
            // TODO: really think about the right strategy here
            final long idleMilliseconds = System.currentTimeMillis() - c.getIdleStartMilliseconds();
            if (idleMilliseconds > 0) {
                LOGGER.info("Idle timeout: "+c.getName());
                LOGGER.log(Level.INFO, "Terminating " + c);
                try {
                    c.getNode().retentionTerminate();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return 1;
    }

    /**
     * Try to connect to it ASAP.
     */
    @Override
    public void start(LXCComputer c) {
        c.connect(false);
    }

    // no registration since this retention strategy is used only for EC2 nodes that we provision automatically.
    // @Extension
    public static class DescriptorImpl extends Descriptor<RetentionStrategy<?>> {
        @Override
        public String getDisplayName() {
            return "Docker";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(LXCRetentionStrategy.class.getName());

    public static boolean disabled = Boolean.getBoolean(LXCRetentionStrategy.class.getName()+".disabled");
}
