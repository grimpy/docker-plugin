package com.github.grimpy.jenkins.plugins.lxc.publissher;

import com.github.grimpy.jenkins.plugins.lxc.builder.LXCBuilderControlOptionStopAll;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Created by magnayn on 30/01/2014.
 */
public class LXCPublisherControl extends Recorder implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(LXCPublisherControl.class.getName());

    @DataBoundConstructor
    public LXCPublisherControl()
    {}

    public BuildStepMonitor getRequiredMonitorService() {
        return null;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        new LXCBuilderControlOptionStopAll().execute(build);
        return true;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Stop LXC Containers";
        }
    }
}
