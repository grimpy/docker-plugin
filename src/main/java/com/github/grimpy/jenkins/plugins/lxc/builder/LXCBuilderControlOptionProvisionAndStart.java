package com.github.grimpy.jenkins.plugins.lxc.builder;

import com.github.grimpy.jenkins.plugins.lxc.LXCTemplate;
import com.github.grimpy.jenkins.plugins.lxc.client.LXCClient;

import hudson.Extension;
import hudson.model.AbstractBuild;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by magnayn on 30/01/2014.
 */
public class LXCBuilderControlOptionProvisionAndStart extends LXCBuilderControlCloudOption {
    private final String templateId;

    @DataBoundConstructor
    public LXCBuilderControlOptionProvisionAndStart(String cloudId, String templateId) {
        super(cloudId);
        this.templateId = templateId;
    }

    @Override
    public void execute(AbstractBuild<?, ?> build){

        LXCTemplate template = getCloud(build).getTemplate(templateId);

        String containerId = template.provisionNew().getName();

        LOGGER.info("Starting container " + containerId);
        LXCClient client = getClient(build);
        client.getContainer(containerId).start();
        getLaunchAction(build).started(client, containerId);
    }

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends LXCBuilderControlOptionDescriptor {
        @Override
        public String getDisplayName() {
            return "Provision & Start Container";
        }

    }
}
