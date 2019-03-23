package io.gatehill.buildclerk.jenkins.service;

import io.gatehill.buildclerk.api.model.BuildDetails;
import io.gatehill.buildclerk.api.model.BuildReport;
import io.gatehill.buildclerk.api.model.BuildStatus;
import io.gatehill.buildclerk.api.model.Scm;
import io.gatehill.buildclerk.jenkins.api.BackendApiClientBuilder;
import io.gatehill.buildclerk.jenkins.util.Constants;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Run;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.lang.StringUtils;
import retrofit2.Call;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Sends notifications to the backend API.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class NotificationService {
    private final JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();

    public void sendNotification(PrintStream logger, Run run, @Nonnull String serverUrl,
                                 @Nonnull Map<String, String> scmVars) {
        sendNotification(logger, run, serverUrl, null, scmVars);
    }

    public void sendNotification(PrintStream logger, Run run, @Nonnull String serverUrl,
                                 @Nullable String status, @Nonnull Map<String, String> scmVars) {
        try {
            final BuildReport notification = createBuildReport(logger, run, status, scmVars);
            final String finalServerUrl = buildFinalServerUrl(serverUrl);
            logger.printf("Sending build report to %s:%n%s%n", finalServerUrl, notification);

            final BackendApiClientBuilder builder = new BackendApiClientBuilder(finalServerUrl);
            final Call<Void> call = builder.buildApiClient(Collections.emptyMap()).notifyBuild(notification);
            call.execute();

            logger.println("Build report sent");

        } catch (Exception e) {
            logger.printf("Failed to send build report: %s%n", e.getMessage());
            e.printStackTrace(logger);
        }
    }

    private BuildReport createBuildReport(PrintStream logger, Run run, @Nullable String status,
                                          @Nonnull Map<String, String> scmVars) {

        final BuildStatus buildStatus = determineBuildStatus(logger, run, status);
        final Scm scm = fetchScmDetails(scmVars);
        final String triggeredBy = determineTriggeredBy(run);

        return new BuildReport(
                run.getParent().getName(),
                run.getParent().getShortUrl(),
                new BuildDetails(
                        run.getNumber(),
                        buildStatus,
                        scm,
                        getJenkinsUrl() + run.getUrl(),
                        triggeredBy
                )
        );
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private String determineTriggeredBy(Run run) {
        for (Cause cause : (List<Cause>) run.getCauses()) {
            if (cause instanceof Cause.UserIdCause) {
                return ((Cause.UserIdCause) cause).getUserName();
            } else if (cause instanceof SCMTrigger.SCMTriggerCause) {
                return "SCM trigger";
            } else if (cause instanceof TimerTrigger.TimerTriggerCause) {
                return "Timer";
            }
        }

        return "Unknown";
    }

    /**
     * @param logger the logger
     * @param run    the current run
     * @param status the status set by the caller
     * @return the build status
     */
    private BuildStatus determineBuildStatus(PrintStream logger, Run run, @Nullable String status) {
        final BuildStatus buildStatus;
        if (null != status) {
            buildStatus = BuildStatus.valueOf(status);

        } else {
            logger.println("Status not specified - attempting to determine from current run");

            if (null != run.getResult()) {
                buildStatus = convertResult(run.getResult());

            } else {
                // workaround for null result bug - see https://issues.jenkins-ci.org/browse/JENKINS-46325
                if (run.isBuilding()) {
                    logger.println("Run result was null, but run is building - interpreting as success");
                    buildStatus = BuildStatus.SUCCESS;
                } else {
                    logger.println("Run result was null, but run is not building - interpreting as failure");
                    buildStatus = BuildStatus.FAILED;
                }
            }
        }
        return buildStatus;
    }

    /**
     * Conservatively converts a Job Result to a BuildStatus.
     *
     * @param result the result of the Job
     * @return the corresponding build status
     */
    private BuildStatus convertResult(@CheckForNull Result result) {
        if (Result.FAILURE.equals(result) || Result.UNSTABLE.equals(result) || Result.ABORTED.equals(result)) {
            return BuildStatus.FAILED;
        } else {
            // By design we presume a positive outcome unless an explicit failure result is returned.
            // Consider whether we should fail 'safe' the other way in future.
            return BuildStatus.SUCCESS;
        }
    }

    private Scm fetchScmDetails(Map<String, String> scmVars) {
        return new Scm(
                checkNotNull(scmVars.get("GIT_LOCAL_BRANCH"), "GIT_LOCAL_BRANCH variable was null"),
                checkNotNull(scmVars.get("GIT_COMMIT"), "GIT_COMMIT variable was null")
        );
    }

    @Nonnull
    private String getJenkinsUrl() {
        final String rawJenkinsUrl = jenkinsConfig.getUrl();

        final String jenkinsUrl;
        if (StringUtils.isNotBlank(rawJenkinsUrl)) {
            if (rawJenkinsUrl.endsWith("/")) {
                jenkinsUrl = rawJenkinsUrl;
            } else {
                jenkinsUrl = rawJenkinsUrl + "/";
            }
        } else {
            jenkinsUrl = "";
        }
        return jenkinsUrl;
    }

    /**
     * Strips the builds API suffix from the server URL.
     *
     * @param serverUrl the raw server URL from config
     * @return the Server URL to use
     */
    private String buildFinalServerUrl(String serverUrl) {
        if (serverUrl.endsWith(Constants.SERVER_URL_BUILDS_SUFFIX)) {
            return serverUrl.substring(0, serverUrl.length() - Constants.SERVER_URL_BUILDS_SUFFIX.length());
        } else {
            return serverUrl;
        }
    }
}
