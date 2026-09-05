package com.cashfluent.app.data.model

/** Three states, and no fourth. A module is untouched, opened, or checked. */
enum class ModuleStatus {
    NOT_STARTED,
    IN_PROGRESS,
    DONE;

    companion object {
        fun fromStored(value: String?): ModuleStatus =
            entries.firstOrNull { it.name == value } ?: NOT_STARTED
    }
}

/**
 * [answers] maps a question's index to the option the user picked. Answers are kept
 * after a wrong pick on purpose: the explanation is the content, and changing your mind
 * costs nothing.
 */
data class ModuleProgress(
    val status: ModuleStatus = ModuleStatus.NOT_STARTED,
    val answers: Map<Int, Int> = emptyMap(),
)

data class Progress(val byModule: Map<String, ModuleProgress> = emptyMap()) {

    fun of(moduleId: String): ModuleProgress = byModule[moduleId] ?: ModuleProgress()

    fun statusOf(moduleId: String): ModuleStatus = of(moduleId).status

    fun isDone(moduleId: String): Boolean = statusOf(moduleId) == ModuleStatus.DONE

    /** Counted against a given list so bonus modules never inflate "2 of 6 done". */
    fun doneCountIn(moduleIds: List<String>): Int = moduleIds.count(::isDone)

    /**
     * The first module in [moduleIds] that is not finished — what "Start here" points at,
     * and what the guided path unlocks. Null once everything is done.
     */
    fun firstUnfinished(moduleIds: List<String>): String? = moduleIds.firstOrNull { !isDone(it) }
}
