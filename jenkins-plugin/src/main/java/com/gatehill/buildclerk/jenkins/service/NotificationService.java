package com.gatehill.buildclerk.jenkins.service;

import com.gatehill.buildclerk.api.model.BuildDetails;
import com.gatehill.buildclerk.api.model.BuildOutcome;
import com.gatehill.buildclerk.api.model.BuildStatus;
import com.gatehill.buildclerk.api.model.Scm;
import com.gatehill.buildclerk.jenkins.api.BackendApiClientBuilder;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.lang.StringUtils;
import retrofit2.Call;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Collections;

/**
 * Sends notifications to the backend API.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class NotificationService {
    private final JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();

    public void sendNotification(PrintStream logger, Run run, String serverUrl) {
        try {
            final BuildOutcome notification = createBuildOutcome(logger, run);
            logger.printf("Sending build report to %s:%n%s%n", serverUrl, notification);

            final BackendApiClientBuilder builder = new BackendApiClientBuilder(serverUrl);
            final Call<Void> call = builder.buildApiClient(Collections.emptyMap()).notifyBuild(notification);
            call.execute();

            logger.println("Build report sent");

        } catch (Exception e) {
            logger.printf("Failed to send build report: %s%n", e.getMessage());
            e.printStackTrace(logger);
        }
    }

    /**
     * Workaround for null result bug - see https://issues.jenkins-ci.org/browse/JENKINS-46325
     *
     * @param logger the logger
     * @param run the current run
     * @return the build status
     */
    private BuildStatus determineBuildStatus(PrintStream logger, Run run) {
        final BuildStatus buildStatus;
        if (null == run.getResult()) {
            if (run.isBuilding()) {
                logger.println("Run result was null, but run is building - interpreting as success");
                buildStatus = BuildStatus.SUCCESS;
            } else {
                logger.println("Run result was null, but run is not building - interpreting as failure");
                buildStatus = BuildStatus.FAILED;
            }
        } else {
            buildStatus = convertResult(run.getResult());
        }
        return buildStatus;
    }

    private BuildOutcome createBuildOutcome(PrintStream logger, Run run) {
        final BuildStatus buildStatus = determineBuildStatus(logger, run);

        // TODO complete this
        return new BuildOutcome(
                run.getParent().getName(),
                run.getParent().getShortUrl(),
                new BuildDetails(
                        run.getNumber(),
                        buildStatus,
                        new Scm(
                                "TODO",
                                "TODO"
                        ),
                        getJenkinsUrl() + run.getUrl()
                )
        );
    }

    @Nonnull
    private String getJenkinsUrl() {
        final String configUrl = jenkinsConfig.getUrl();

        final String jenkinsUrl;
        if (StringUtils.isNotBlank(configUrl)) {
            if (configUrl.endsWith("/")) {
                jenkinsUrl = configUrl;
            } else {
                jenkinsUrl = configUrl + "/";
            }
        } else {
            jenkinsUrl = "";
        }
        return jenkinsUrl;
    }

    /**
     * Conservatively converts a Job Result to a BuildStatus.
     *
     * @param result the result of the Job
     * @return the corresponding build status
     */
    private BuildStatus convertResult(@CheckForNull Result result) {
        if (Result.FAILURE.equals(result) || Result.UNSTABLE.equals(result)) {
            return BuildStatus.FAILED;
        } else {
            // By design we presume a positive outcome unless an explicit failure result is returned.
            // Consider whether we should fail 'safe' the other way in future.
            return BuildStatus.SUCCESS;
        }
    }
}
