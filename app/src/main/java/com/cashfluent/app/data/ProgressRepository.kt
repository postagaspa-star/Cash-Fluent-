package com.cashfluent.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cashfluent.app.data.model.ModuleProgress
import com.cashfluent.app.data.model.ModuleStatus
import com.cashfluent.app.data.model.Progress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressStore: DataStore<Preferences> by preferencesDataStore(name = "cashfluent_progress")

/**
 * Everything Cashfluent remembers about a person, which is deliberately almost nothing:
 * which modules they have opened or finished, and which answers they picked. It lives on
 * the device and there is no code path that sends it anywhere.
 */
class ProgressRepository(private val context: Context) {

    val progress: Flow<Progress> = context.progressStore.data.map { it.toProgress() }

    /** Opening a module counts as starting it — but never demotes a finished one. */
    suspend fun markStarted(moduleId: String) {
        context.progressStore.edit { prefs ->
            val current = ModuleStatus.fromStored(prefs[statusKey(moduleId)])
            if (current == ModuleStatus.NOT_STARTED) {
                prefs[statusKey(moduleId)] = ModuleStatus.IN_PROGRESS.name
            }
        }
    }

    /**
     * Records the pick and, once every one of [questionCount] questions has an answer,
     * marks the module done — in the same transaction. Deciding that from a snapshot of
     * UI state instead could miss a tap that landed before the previous write had come
     * back round through the flow, and leave a finished module showing as open.
     */
    suspend fun recordAnswer(moduleId: String, questionIndex: Int, optionIndex: Int, questionCount: Int) {
        context.progressStore.edit { prefs ->
            val key = answersKey(moduleId)
            val kept = (prefs[key] ?: emptySet())
                .filterNot { it.substringBefore(ENTRY_SEPARATOR) == questionIndex.toString() }
            val answers = (kept + "$questionIndex$ENTRY_SEPARATOR$optionIndex").toSet()
            prefs[key] = answers
            if (answers.size >= questionCount) {
                prefs[statusKey(moduleId)] = ModuleStatus.DONE.name
            }
        }
    }

    suspend fun markDone(moduleId: String) {
        context.progressStore.edit { prefs ->
            prefs[statusKey(moduleId)] = ModuleStatus.DONE.name
        }
    }

    /** Clears progress only. There is nothing else stored to clear. */
    suspend fun reset() {
        context.progressStore.edit { it.clear() }
    }

    private companion object {
        const val STATUS_PREFIX = "module_status_"
        const val ANSWERS_PREFIX = "quiz_answers_"
        const val ENTRY_SEPARATOR = ':'

        fun statusKey(moduleId: String) = stringPreferencesKey("$STATUS_PREFIX$moduleId")
        fun answersKey(moduleId: String) = stringSetPreferencesKey("$ANSWERS_PREFIX$moduleId")

        fun Preferences.toProgress(): Progress {
            val statuses = mutableMapOf<String, ModuleStatus>()
            val answers = mutableMapOf<String, Map<Int, Int>>()

            asMap().forEach { (key, value) ->
                when {
                    key.name.startsWith(STATUS_PREFIX) ->
                        statuses[key.name.removePrefix(STATUS_PREFIX)] =
                            ModuleStatus.fromStored(value as? String)

                    key.name.startsWith(ANSWERS_PREFIX) -> {
                        val stored = (value as? Set<*>)?.filterIsInstance<String>().orEmpty()
                        answers[key.name.removePrefix(ANSWERS_PREFIX)] = stored.decodeAnswers()
                    }
                }
            }

            val moduleIds = statuses.keys + answers.keys
            return Progress(
                moduleIds.associateWith { id ->
                    ModuleProgress(
                        status = statuses[id] ?: ModuleStatus.NOT_STARTED,
                        answers = answers[id].orEmpty(),
                    )
                },
            )
        }

        /** Entries look like "2:1" — question 2, option 1. Anything malformed is dropped. */
        fun List<String>.decodeAnswers(): Map<Int, Int> = mapNotNull { entry ->
            val question = entry.substringBefore(ENTRY_SEPARATOR).toIntOrNull()
            val option = entry.substringAfter(ENTRY_SEPARATOR, "").toIntOrNull()
            if (question != null && option != null) question to option else null
        }.toMap()
    }
}
