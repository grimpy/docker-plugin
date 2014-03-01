package com.github.grimpy.jenkins.plugins.lxc.builder;

import com.github.grimpy.jenkins.plugins.lxc.action.LXCLaunchAction;

import hudson.Extension;
import hudson.model.AbstractBuild;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by magnayn on 30/01/2014.
 */
public class LXCBuilderControlOptionStopAll extends LXCBuilderControlOption {

    @DataBoundConstructor
    public LXCBuilderControlOptionStopAll() {

    }

    @Override
    public void execute(AbstractBuild<?, ?> build) {
        LOGGER.info("Stopping all containers");
        for(LXCLaunchAction.Item containerItem : getLaunchAction(build).getRunning()) {
            try {
                LOGGER.info("Stopping container " + containerItem.id);
                containerItem.client.getContainer(containerItem.id).stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends LXCBuilderControlOptionDescriptor {
        @Override
        public String getDisplayName() {
            return "Stop All Containers";
        }

    }
}
