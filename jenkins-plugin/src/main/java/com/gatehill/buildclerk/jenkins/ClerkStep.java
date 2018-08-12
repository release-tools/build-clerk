package com.gatehill.buildclerk.jenkins;

import com.gatehill.buildclerk.jenkins.service.NotificationService;
import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Set;

/**
 * Pipeline step.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class ClerkStep extends Step {
    private final String serverUrl;

    @DataBoundConstructor
    public ClerkStep(@Nonnull final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new ClerkStepExecution(this, context);
    }

    @Symbol("buildClerk")
    @Extension
    public static final class DescriptorImpl extends StepDescriptor implements ClerkDescriptor {
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
            return "buildClerk";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
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

    public static class ClerkStepExecution extends SynchronousStepExecution<Void> {
        private final NotificationService notificationService = new NotificationService();
        private final ClerkStep step;

        ClerkStepExecution(ClerkStep step, StepContext context) {
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
