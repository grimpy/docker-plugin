package com.github.grimpy.jenkins.plugins.lxc;

import com.github.grimpy.jenkins.plugins.lxc.client.Container;
import com.github.grimpy.jenkins.plugins.lxc.client.LXCClient;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;

import hudson.Extension;
import hudson.model.*;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import hudson.util.FormValidation;
import hudson.util.StreamTaskListener;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nullable;
import javax.servlet.ServletException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by magnayn on 08/01/2014.
 */
public class LXCCloud extends Cloud {

    private static final Logger LOGGER = Logger.getLogger(LXCCloud.class.getName());

    public static final String CLOUD_ID_PREFIX = "lxc-";

    public final List<? extends LXCTemplate> templates;
    public final String serverUrl;

    private transient LXCClient connection;

    @DataBoundConstructor
    public LXCCloud(String name, List<? extends LXCTemplate> templates, String serverUrl, String instanceCapStr) {
        super(name);
        this.serverUrl = serverUrl;

        if( templates != null )
            this.templates = templates;
        else
            this.templates = Collections.emptyList();



        readResolve();
    }

    protected Object readResolve() {
        for (LXCTemplate t : templates)
            t.parent = this;
        return this;
    }

    /**
     * Connects to Docker.
     */
    public synchronized LXCClient connect() {

        if (connection == null) {
            connection = new LXCClient();
        }
        return connection;

    }

    @Override
    public synchronized Collection<NodeProvisioner.PlannedNode> provision(Label label, int excessWorkload) {
        try {
            LOGGER.log(Level.INFO, "Excess workload after pending Spot instances: " + excessWorkload);

            List<NodeProvisioner.PlannedNode> r = new ArrayList<NodeProvisioner.PlannedNode>();

            final LXCTemplate t = getTemplate(label);

            while (excessWorkload>0) {

                if (!addProvisionedSlave(t.image, t.instanceCap)) {
                    break;
                }

                r.add(new NodeProvisioner.PlannedNode(t.getDisplayName(),
                        Computer.threadPoolForRemoting.submit(new Callable<Node>() {
                            public Node call() throws Exception {
                                // TODO: record the output somewhere
                                try {
                                    LXCSlave s = t.provision(new StreamTaskListener(System.out));
                                    Jenkins.getInstance().addNode(s);
                                    // Docker instances may have a long init script. If we declare
                                    // the provisioning complete by returning without the connect
                                    // operation, NodeProvisioner may decide that it still wants
                                    // one more instance, because it sees that (1) all the slaves
                                    // are offline (because it's still being launched) and
                                    // (2) there's no capacity provisioned yet.
                                    //
                                    // deferring the completion of provisioning until the launch
                                    // goes successful prevents this problem.
                                    s.toComputer().connect(false).get();
                                    return s;
                                }
                                catch(Exception ex) {
                                    LOGGER.log(Level.WARNING, "Error in provisioning");
                                    ex.printStackTrace();
                                    throw Throwables.propagate(ex);
                                }
                                finally {
                                    //decrementAmiSlaveProvision(t.ami);
                                }
                            }
                        })
                        ,t.getNumExecutors()));

                excessWorkload -= t.getNumExecutors();

            }
            return r;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Failed to count the # of live instances on Docker",e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean canProvision(Label label) {
        return getTemplate(label)!=null;
    }

    public LXCTemplate getTemplate(String template) {
        for (LXCTemplate t : templates) {
            if(t.image.equals(template)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Gets {@link SlaveTemplate} that has the matching {@link Label}.
     */
    public LXCTemplate getTemplate(Label label) {
        for (LXCTemplate t : templates) {
            if(label == null || label.matches(t.getLabelSet())) {
                return t;
            }
        }
        return null;
    }

    /**
     * Check not too many already running.
     *
     */
    private synchronized boolean addProvisionedSlave(String image, int amiCap) throws Exception {
        if( amiCap == 0 )
            return true;


        final LXCClient lxcClient = connect();

        List<Container> containers = lxcClient.listContainers();

        Collection<Container> matching = Collections2.filter(containers, new Predicate<Container>() {
            public boolean apply(@Nullable Container container) {
                return container.getStatus().equals("RUNNING");
            }
        });

        return matching.size() < amiCap;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Cloud> {
        @Override
        public String getDisplayName() {
            return "LXC";
        }

        public FormValidation doTestConnection(
                @QueryParameter URL serverUrl
                ) throws IOException, ServletException {

            LXCClient dc = new LXCClient();

            String version = dc.getVersion();

            return FormValidation.ok("Version = " + version);
        }
    }
}