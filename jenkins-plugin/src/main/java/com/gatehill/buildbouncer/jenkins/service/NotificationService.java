package com.gatehill.buildbouncer.jenkins.service;

import com.gatehill.buildbouncer.api.model.BuildDetails;
import com.gatehill.buildbouncer.api.model.BuildOutcome;
import com.gatehill.buildbouncer.api.model.BuildStatus;
import com.gatehill.buildbouncer.api.model.Scm;
import com.gatehill.buildbouncer.jenkins.api.BackendApiClientBuilder;
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
            // logger prints to job 'Console Output'
            final BuildOutcome notification = createBuildOutcome(run);
            logger.printf("Sending notification to %s: %s%n", serverUrl, notification);

            final BackendApiClientBuilder builder = new BackendApiClientBuilder(serverUrl);
            final Call<Void> call = builder.buildApiClient(Collections.emptyMap()).notifyBuild(notification);
            call.execute();

            logger.println("Notification sent");

        } catch (Exception e) {
            logger.printf("Failed to send notification: %s%n", e.getMessage());
            e.printStackTrace(logger);
        }
    }

    private BuildOutcome createBuildOutcome(Run run) {
        // TODO complete this
        return new BuildOutcome(
                run.getDisplayName(),
                run.getUrl(),
                new BuildDetails(
                        run.getNumber(),
                        convertResult(run.getResult()),
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
