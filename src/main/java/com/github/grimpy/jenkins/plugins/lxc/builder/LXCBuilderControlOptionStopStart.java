package com.github.grimpy.jenkins.plugins.lxc.builder;

/**
 * Created by magnayn on 30/01/2014.
 */
public abstract class LXCBuilderControlOptionStopStart extends LXCBuilderControlCloudOption {

    public final String containerId;

    public LXCBuilderControlOptionStopStart(String cloudId, String containerId) {
        super(cloudId);
        this.containerId = containerId;
    }
}
