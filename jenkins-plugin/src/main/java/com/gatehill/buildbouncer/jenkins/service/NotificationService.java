package com.gatehill.buildbouncer.jenkins.service;

import com.gatehill.buildbouncer.api.model.BuildDetails;
import com.gatehill.buildbouncer.api.model.BuildOutcome;
import com.gatehill.buildbouncer.api.model.BuildStatus;
import com.gatehill.buildbouncer.api.model.Scm;
import com.gatehill.buildbouncer.jenkins.api.BackendApiClientBuilder;
import hudson.model.Run;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

/**
 * Sends notifications to the backend API.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class NotificationService {
    public void sendNotification(PrintStream logger, Run run, String serverUrl) {
        try {
            // logger prints to job 'Console Output'
            logger.printf("Sending notification to %s", serverUrl);
            final BuildOutcome notification = createBuildOutcome(run);

            final BackendApiClientBuilder builder = new BackendApiClientBuilder(serverUrl);
            final Call<Void> call = builder.buildApiClient(Collections.emptyMap()).notifyBuild(notification);
            call.execute();

            logger.println("Notification sent");

        } catch (Exception e) {
            logger.printf("Failed to send notification: %s", e);
        }
    }

    private BuildOutcome createBuildOutcome(Run run) {
        // TODO complete this
        return new BuildOutcome(
                run.getDisplayName(),
                run.getUrl(),
                new BuildDetails(
                        run.getNumber(),
                        // FIXME
                        BuildStatus.SUCCESS,
                        new Scm(
                                "TODO",
                                "TODO"
                        ),
                        "http://example.com/" + run.getUrl()
                )
        );
    }
}
