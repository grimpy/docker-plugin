package com.github.grimpy.jenkins.plugins.lxc.builder;

import com.github.grimpy.jenkins.plugins.lxc.action.LXCLaunchAction;

import hudson.model.AbstractBuild;
import hudson.model.Describable;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by magnayn on 30/01/2014.
 */
public abstract class LXCBuilderControlOption implements Describable<LXCBuilderControlOption> {
    protected static final Logger LOGGER = Logger.getLogger(LXCBuilderControl.class.getName());

    public abstract void execute(AbstractBuild<?, ?> build);

    protected LXCLaunchAction getLaunchAction(AbstractBuild<?, ?> build) {
        List<LXCLaunchAction> launchActionList = build.getActions(LXCLaunchAction.class);
        LXCLaunchAction launchAction;
        if( launchActionList.size() > 0 ) {
            launchAction = launchActionList.get(0);
        } else {
            launchAction = new LXCLaunchAction();
            build.addAction(launchAction);
        }
        return launchAction;
    }

    public LXCBuilderControlOptionDescriptor getDescriptor() {
        return (LXCBuilderControlOptionDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }
}
