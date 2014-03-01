package com.github.grimpy.jenkins.plugins.lxc.token;

import com.github.grimpy.jenkins.plugins.lxc.LXCSlave;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Node;
import hudson.model.TaskListener;

import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

import java.io.IOException;

/**
 * Created by magnayn on 30/01/2014.
 */
@Extension(optional=true)
public class LXCHostTokenMacro extends DataBoundTokenMacro {
    @Override
    public String evaluate(AbstractBuild<?, ?> abstractBuild, TaskListener taskListener, String s) throws MacroEvaluationException, IOException, InterruptedException {
        Node node = abstractBuild.getBuiltOn();
        if( node instanceof LXCSlave) {
            LXCSlave dockerSlave = (LXCSlave)node;
            return dockerSlave.containerId;
        }

        return null;
    }

    @Override
    public boolean acceptsMacroName(String macroName) {
        return macroName.equals("DOCKER_CONTAINERID");
    }
}
