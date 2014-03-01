package com.github.grimpy.jenkins.plugins.lxc.action;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.BuildBadgeAction;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;

/**
 * Created by magnayn on 10/01/2014.
 */
@ExportedBean
public class LXCBuildAction implements Action, Serializable, Cloneable, Describable<LXCBuildAction> {

    public final String containerHost;
    public final String containerId;

    public final String taggedId;

    public LXCBuildAction(String containerHost, String containerId, String taggedId) {
        this.containerHost = containerHost;
        this.containerId = containerId;
        this.taggedId = taggedId;
    }

    public String getIconFileName() {
        return "/plugin/docker-plugin/images/24x24/docker.png";
    }

    public String getDisplayName() {
        return "Built on Docker";
    }

    public String getUrlName() {
        return "docker";
    }

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    /**
     * Just for assisting form related stuff.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<LXCBuildAction> {
        public String getDisplayName() {
            return "Docker";
        }
    }
}
