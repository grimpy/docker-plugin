package com.github.grimpy.jenkins.plugins.lxc.builder;

import com.github.grimpy.jenkins.plugins.lxc.client.LXCClient;

import hudson.Extension;
import hudson.model.AbstractBuild;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by magnayn on 30/01/2014.
 */
public class LXCBuilderControlOptionStop extends LXCBuilderControlOptionStopStart {

    @DataBoundConstructor
    public LXCBuilderControlOptionStop(String cloudId, String containerId) {
        super(cloudId, containerId);
    }

    @Override
    public void execute(AbstractBuild<?, ?> build){
        LOGGER.info("Stopping container " + containerId);
        LXCClient client = getClient(build);
        client.getContainer(containerId).stop();
        getLaunchAction(build).stopped(client, containerId);
    }

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends LXCBuilderControlOptionDescriptor {
        @Override
        public String getDisplayName() {
            return "Stop Container";
        }

    }
}
