package com.github.grimpy.jenkins.plugins.lxc.builder;

import com.github.grimpy.jenkins.plugins.lxc.LXCCloud;
import com.github.grimpy.jenkins.plugins.lxc.LXCSlave;
import com.github.grimpy.jenkins.plugins.lxc.client.LXCClient;
import com.google.common.base.Strings;

import hudson.model.AbstractBuild;
import hudson.model.Node;
import jenkins.model.Jenkins;

/**
 * Created by magnayn on 30/01/2014.
 */
public abstract class LXCBuilderControlCloudOption extends LXCBuilderControlOption {
    public final String cloudName;

    protected LXCBuilderControlCloudOption(String cloudName) {
        this.cloudName = cloudName;
    }

    protected LXCCloud getCloud(AbstractBuild<?, ?> build) {
        LXCCloud cloud = null;

        Node node = build.getBuiltOn();
        if( node instanceof LXCSlave) {
            LXCSlave dockerSlave = (LXCSlave)node;
            cloud = dockerSlave.getCloud();
        }

        if( !Strings.isNullOrEmpty(cloudName) ) {
            cloud = (LXCCloud) Jenkins.getInstance().getCloud(cloudName);
        }

        if( cloud == null ) {
            throw new RuntimeException("Cannot list cloud for docker action");
        }

        return cloud;
    }

    protected LXCClient getClient(AbstractBuild<?, ?> build) {
        LXCCloud cloud = null;

        Node node = build.getBuiltOn();
        if( node instanceof LXCSlave) {
            LXCSlave dockerSlave = (LXCSlave)node;
            cloud = dockerSlave.getCloud();
        }

        if( !Strings.isNullOrEmpty(cloudName) ) {
            cloud = (LXCCloud) Jenkins.getInstance().getCloud(cloudName);
        }

        if( cloud == null ) {
            throw new RuntimeException("Cannot list cloud for docker action");
        }

        LXCClient client = cloud.connect();
        return client;
    }
}
