package io.gatehill.buildclerk.jenkins;

import io.gatehill.buildclerk.jenkins.service.NotificationService;
import io.gatehill.buildclerk.jenkins.util.Constants;
import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.gatehill.buildclerk.jenkins.service.NotificationService;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pipeline step.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class ClerkStep extends Step {
    private final String serverUrl;
    private String branch;
    private String commit;
    private Map<String, String> scmVars;

    @DataBoundConstructor
    public ClerkStep(@Nonnull final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getBranch() {
        return branch;
    }

    @DataBoundSetter
    public void setBranch(@Nullable String branch) {
        this.branch = branch;
    }

    public String getCommit() {
        return commit;
    }

    @DataBoundSetter
    public void setCommit(@Nullable String commit) {
        this.commit = commit;
    }

    public Map<String, String> getScmVars() {
        return scmVars;
    }

    @DataBoundSetter
    public void setScmVars(@Nullable Map<String, String> scmVars) {
        this.scmVars = scmVars;
    }

    @Override
    public StepExecution start(StepContext context) {
        final Map<String, String> consolidatedScmVars = new HashMap<>();
        if (null != scmVars) {
            consolidatedScmVars.putAll(scmVars);
        }
        if (StringUtils.isNotBlank(branch)) {
            consolidatedScmVars.put("GIT_LOCAL_BRANCH", branch);
        }
        if (StringUtils.isNotBlank(commit)) {
            consolidatedScmVars.put("GIT_COMMIT", commit);
        }
        return new ClerkStepExecution(this, context, consolidatedScmVars);
    }

    @Symbol("buildClerk")
    @Extension
    public static final class DescriptorImpl extends StepDescriptor implements ClerkDescriptor {
        @Override
        public String getDisplayName() {
            return Constants.DISPLAY_NAME;
        }

        @Override
        public String getFunctionName() {
            return "buildClerk";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class, Launcher.class);
        }
    }

    public static class ClerkStepExecution extends SynchronousStepExecution<Void> {
        private final NotificationService notificationService = new NotificationService();
        private final ClerkStep step;
        private final Map<String, String> scmVars;

        ClerkStepExecution(ClerkStep step, StepContext context, Map<String, String> scmVars) {
            super(context);
            this.step = step;
            this.scmVars = scmVars;
        }

        @Override
        protected Void run() throws Exception {
            final PrintStream logger = getContext().get(TaskListener.class).getLogger();
            final Run run = getContext().get(Run.class);

            notificationService.sendNotification(logger, run, step.getServerUrl(), scmVars);
            return null;
        }
    }
}
