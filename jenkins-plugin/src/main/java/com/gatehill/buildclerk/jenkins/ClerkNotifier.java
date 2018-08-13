package com.gatehill.buildclerk.jenkins;

import com.gatehill.buildclerk.jenkins.service.NotificationService;
import com.gatehill.buildclerk.jenkins.util.Constants;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Classic post build notifier.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class ClerkNotifier extends Notifier {
    private final NotificationService notificationService = new NotificationService();
    private final String serverUrl;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ClerkNotifier(@Nonnull final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Used from <tt>config.jelly</tt>.
     */
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public boolean perform(final AbstractBuild build,
                           final Launcher launcher,
                           final BuildListener listener) {

        final Map<String, String> scmVars = new HashMap<>();
        try {
            final EnvVars environment = build.getEnvironment(listener);
            scmVars.put("GIT_LOCAL_BRANCH", environment.get("GIT_LOCAL_BRANCH"));
            scmVars.put("GIT_COMMIT", environment.get("GIT_COMMIT"));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read GIT_BRANCH and GIT_COMMIT environment variables", e);
        }

        notificationService.sendNotification(listener.getLogger(), build, serverUrl, scmVars);
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> implements ClerkDescriptor {
        /**
         * Global configuration information variables. If you don't want fields
         * to be persisted, use <tt>transient</tt>.
         */
        private String serverUrl;

        /**
         * In order to load the persisted global configuration, you have to call
         * load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return Constants.DISPLAY_NAME;
        }

        public String getServerUrl() {
            return serverUrl;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData)
                throws FormException {

            // To persist global configuration information, set properties and call save().
            serverUrl = formData.getString("serverUrl");
            save();
            return super.configure(req, formData);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project
            // types.
            return true;
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
