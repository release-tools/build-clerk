package com.gatehill.buildbouncer.jenkins;

import com.gatehill.buildbouncer.jenkins.service.NotificationService;
import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.PrintStream;
import java.util.Set;

/**
 * Pipeline step.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class BuildBouncerStep extends Step {
    private String serverUrl;

    @DataBoundConstructor
    public BuildBouncerStep(final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    @DataBoundSetter
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new BuildBouncerStepExecution(this, context);
    }

    @Extension(optional = true)
    @Symbol("buildBouncer")
    public static final class DescriptorImpl extends StepDescriptor {
        /**
         * Global configuration information variables. If you don't want fields
         * to be persisted, use <tt>transient</tt>.
         */
        private String serverUrl;

        public String getServerUrl() {
            return serverUrl;
        }

        @Override
        public String getFunctionName() {
            return "buildBouncer";
        }

        @Override
        public String getDisplayName() {
            return "Build Bouncer step";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
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
    }

    public static class BuildBouncerStepExecution extends SynchronousStepExecution<Void> {
        private final NotificationService notificationService = new NotificationService();
        private final BuildBouncerStep step;

        BuildBouncerStepExecution(BuildBouncerStep step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            final PrintStream logger = getContext().get(TaskListener.class).getLogger();
            final Run run = getContext().get(Run.class);

            notificationService.sendNotification(logger, run, step.getServerUrl());
            return null;
        }
    }
}
