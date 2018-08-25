package io.gatehill.buildclerk.api.model.action

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = RebuildBranchAction::class, name = "rebuild"),
    JsonSubTypes.Type(value = ShowTextAction::class, name = "show_text"),
    JsonSubTypes.Type(value = RevertCommitAction::class, name = "revert"),
    JsonSubTypes.Type(value = LockBranchAction::class, name = "lock")
)
interface PendingAction {
    val name: String
    val title: String
    val exclusive: Boolean

    /**
     * Should be lowercase to enable easy insertion into action sentence.
     */
    fun describe(): String
}
