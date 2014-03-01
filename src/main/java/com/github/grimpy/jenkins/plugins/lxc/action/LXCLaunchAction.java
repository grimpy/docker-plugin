package com.github.grimpy.jenkins.plugins.lxc.action;

import hudson.model.Action;
import hudson.model.Describable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.github.grimpy.jenkins.plugins.lxc.client.LXCClient;

/**
 * Action to record launching of a slave.
 */
public class LXCLaunchAction implements Action, Serializable, Cloneable{

    public static class Item {
        public final LXCClient client;
        public final String id;

        public Item(LXCClient client, String id) {
            this.client = client;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            if (!client.equals(item.client)) return false;
            if (!id.equals(item.id)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = client.hashCode();
            result = 31 * result + id.hashCode();
            return result;
        }
    }

    private transient List<Item> running = new ArrayList<Item>();

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }

    public void started(LXCClient client, String containerName) {
        running.add( new Item(client, containerName) );
    }

    public void stopped(LXCClient client, String containerName) {
        running.remove( new Item(client, containerName) );
    }

    public Iterable<Item> getRunning() {
        return running;
    }
}
