package io.gatehill.buildclerk.dao.mongo.model

import io.gatehill.buildclerk.api.model.PullRequest
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.Repository
import io.gatehill.buildclerk.api.model.User
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

class MongoPullRequestMergedEvent(
    override val actor: User,
    override val repository: Repository,
    override val pullRequest: PullRequest
) : PullRequestMergedEvent(
    actor = actor,
    repository = repository,
    pullRequest = pullRequest
) {
    @BsonId
    val key: Id<MongoPullRequestMergedEvent> = newId()
}

internal fun PullRequestMergedEvent.toMongoPullRequestMergedEvent() =
    MongoPullRequestMergedEvent(
        actor = this.actor,
        repository = this.repository,
        pullRequest = this.pullRequest
    )

internal fun MongoPullRequestMergedEvent.toPullRequestMergedEvent() =
    PullRequestMergedEvent(
        actor = this.actor,
        repository = this.repository,
        pullRequest = this.pullRequest
    )
