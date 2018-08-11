package com.gatehill.buildbouncer.jenkins;

import com.gatehill.buildbouncer.jenkins.service.NotificationService;
import com.gatehill.buildbouncer.jenkins.util.Constants;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

/**
 * Classic post build notifier.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class BuildBouncerNotifier extends Notifier {
    private final NotificationService notificationService = new NotificationService();
    private final String serverUrl;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public BuildBouncerNotifier(@Nonnull final String serverUrl) {
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

        notificationService.sendNotification(listener.getLogger(), build, serverUrl);
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * Global configuration information variables. If you don't want fields
         * to be persisted, use <tt>transient</tt>.
         */
        private String serverUrl;

        public String getServerUrl() {
            return serverUrl;
        }

        /**
         * In order to load the persisted global configuration, you have to call
         * load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'serverUrl'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         * <p/>
         * Note that returning {@link FormValidation#error(String)} does not
         * prevent the form from being saved. It just means that a message
         * will be displayed to the user.
         */
        public FormValidation doCheckServerUrl(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("Please set a Server URL");
            } else if (!value.startsWith("http://") || !value.startsWith("https://")) {
                return FormValidation.warning("Server URL should start with http:// or https://");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData)
                throws FormException {

            // To persist global configuration information, set that to
            // properties and call save().
            serverUrl = formData.getString("serverUrl");
            save();
            return super.configure(req, formData);
        }

        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project
            // types.
            return true;
        }

        @Override
        public String getDisplayName() {
            return Constants.DISPLAY_NAME;
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
