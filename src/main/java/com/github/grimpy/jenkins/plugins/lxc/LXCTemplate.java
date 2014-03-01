package com.github.grimpy.jenkins.plugins.lxc;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHAuthenticator;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserListBoxModel;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.github.grimpy.jenkins.plugins.lxc.client.Container;
import com.github.grimpy.jenkins.plugins.lxc.client.LXCClient;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.trilead.ssh2.Connection;

import hudson.Extension;
import hudson.Util;
import hudson.model.Node;
import hudson.model.Descriptor;
import hudson.model.Describable;
import hudson.model.Label;
import hudson.model.ItemGroup;
import hudson.model.labels.LabelAtom;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.security.ACL;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import hudson.util.ListBoxModel;
import hudson.util.StreamTaskListener;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;


public class LXCTemplate implements Describable<LXCTemplate> {
    private static final Logger LOGGER = Logger.getLogger(LXCTemplate.class.getName());


    public final String image;
    public final String labelString;
    public final String cloneOptions;

    // SSH settings
    /**
     * The id of the credentials to use.
     */
    public final String credentialsId;

    /**
     * Field jvmOptions.
     */
    public final String jvmOptions;

    /**
     * Field javaPath.
     */
    public final String javaPath;

    /**
     * Field prefixStartSlaveCmd.
     */
    public final String prefixStartSlaveCmd;

    /**
     *  Field suffixStartSlaveCmd.
     */
    public final String suffixStartSlaveCmd;


    public final String remoteFs; // = "/home/jenkins";


    public final int instanceCap;
    public final String[] dnsHosts;

    public final boolean keepBuild;

    private transient /*almost final*/ Set<LabelAtom> labelSet;
    public transient LXCCloud parent;

    public final String additionalTag;
    public final boolean push;

    @DataBoundConstructor
    public LXCTemplate(String image, String labelString,
                          String remoteFs,
                          String credentialsId, String cloneOptions, String jvmOptions, String javaPath,
                          String prefixStartSlaveCmd, String suffixStartSlaveCmd,
                          boolean keepBuild, String instanceCapStr, String dnsString,
                          String additionalTag, boolean push
    ) {
        this.image = image;
        this.labelString = Util.fixNull(labelString);
        this.cloneOptions = cloneOptions;
        this.credentialsId = credentialsId;
        this.jvmOptions = jvmOptions;
        this.javaPath = javaPath;
        this.prefixStartSlaveCmd = prefixStartSlaveCmd;
        this.suffixStartSlaveCmd = suffixStartSlaveCmd;
        this.remoteFs =  Strings.isNullOrEmpty(remoteFs)?"/home/jenkins":remoteFs;
        this.keepBuild = keepBuild;

        if(instanceCapStr.equals("")) {
            this.instanceCap = Integer.MAX_VALUE;
        } else {
            this.instanceCap = Integer.parseInt(instanceCapStr);
        }

        this.dnsHosts = dnsString.split(" ");
        this.additionalTag = additionalTag;
        this.push = push;

        readResolve();
    }

    public String getInstanceCapStr() {
        if (instanceCap==Integer.MAX_VALUE) {
            return "";
        } else {
            return String.valueOf(instanceCap);
        }
    }

    public String getDnsString() {
        return Joiner.on(" ").join(dnsHosts);
    }

    public Descriptor<LXCTemplate> getDescriptor() {
        return Jenkins.getInstance().getDescriptor(getClass());
    }

    public Set<LabelAtom> getLabelSet(){
        return labelSet;
    }

    /**
     * Initializes data structure that we don't persist.
     */
    protected Object readResolve() {
        labelSet = Label.parse(labelString);
        return this;
    }

    public String getDisplayName() {
        return "Image of " + image;
    }

    public LXCCloud getParent() {
        return parent;
    }

    public LXCSlave provision(StreamTaskListener listener) throws IOException, Descriptor.FormException {
        PrintStream logger = listener.getLogger();


        logger.println("Launching " + image );

        String nodeDescription = "LXC Node";


        int numExecutors = 1;
        Node.Mode mode = Node.Mode.EXCLUSIVE;


        RetentionStrategy retentionStrategy = new LXCRetentionStrategy();//RetentionStrategy.INSTANCE;

        List<? extends NodeProperty<?>> nodeProperties = new ArrayList();

        Container container = provisionNew();
        String containerId = container.getName();

        ComputerLauncher launcher = new LXCComputerLauncher(this, container);

        return new LXCSlave(this, containerId,
                containerId,
                nodeDescription,
                remoteFs, numExecutors, mode, labelString,
                launcher, retentionStrategy, nodeProperties);

    }

    public Container provisionNew() {
        LXCClient client = getParent().connect();
        String tmpname = java.util.UUID.randomUUID().toString();
        Container newcontainer = client.clone(image, tmpname, cloneOptions);
        newcontainer.start();
        return newcontainer;
    }

    public int getNumExecutors() {
        return 1;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<LXCTemplate> {

        @Override
        public String getDisplayName() {
            return "Docker Template";
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath ItemGroup context) {

            return new SSHUserListBoxModel().withMatching(SSHAuthenticator.matcher(Connection.class),
                    CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, context,
                            ACL.SYSTEM, SSHLauncher.SSH_SCHEME));
        }
    }
}
