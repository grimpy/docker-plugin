package com.github.grimpy.jenkins.plugins.lxc;

import com.github.grimpy.jenkins.plugins.lxc.action.LXCBuildAction;
import com.github.grimpy.jenkins.plugins.lxc.client.LXCClient;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import hudson.Extension;
import hudson.model.*;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LXCSlave extends AbstractCloudSlave {

    private static final Logger LOGGER = Logger.getLogger(LXCSlave.class.getName());

    public final LXCTemplate template;
    public final String containerId;

    private transient Run theRun;

    private transient boolean commitOnTermate;

    public LXCSlave(LXCTemplate template, String containerId, String name, String nodeDescription, String remoteFS, int numExecutors, Node.Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws Descriptor.FormException, IOException {
        super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, nodeProperties);
        this.template = template;
        this.containerId = containerId;
    }

    public LXCCloud getCloud() {
        return template.getParent();
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    public void setRun(Run run) {
        this.theRun = run;
    }

    public void commitOnTerminate() {
       commitOnTermate = true;
    }

    @Override
    public LXCComputer createComputer() {
        return new LXCComputer(this);
    }

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
        LXCClient client = getClient();

        try {
            toComputer().disconnect(null);
            client.getContainer(containerId).stop();
            if (theRun == null || !commitOnTermate){
                client.getContainer(containerId).remove();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failure to terminate instance " + containerId);
        }
    }

    private void tag(String tag_image) throws IOException {
        theRun.addAction( new LXCBuildAction(getCloud().serverUrl, containerId, tag_image) );
        theRun.save();
    }

    public LXCClient getClient() {
        return template.getParent().connect();
    }

    /**
     * Called when the slave is connected to Jenkins
     */
    public void onConnected() {

    }

    public void retentionTerminate() throws IOException, InterruptedException {
        terminate();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("containerId", containerId)
                .toString();
    }

    @Extension
	public static final class DescriptorImpl extends SlaveDescriptor {

    	@Override
		public String getDisplayName() {
			return "LXC Slave";
    	};

		@Override
		public boolean isInstantiable() {
			return false;
		}

	}
}
