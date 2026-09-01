package com.cashfluent.app.content

/**
 * The shape every module has to fit.
 *
 * Three blocks, always in this order — the idea, the mechanism, the real numbers — then
 * a short check. The repetition is the feature: after the second module nobody has to
 * work out where the formula lives, and they can spend that attention on the content.
 *
 * All of it is typed Kotlin rather than JSON or string resources, so a missing field is
 * a compile error instead of a blank line in front of a judge.
 */

enum class Track { CORE, BONUS }

/** Which "Try it with your numbers" panel the module opens. */
enum class SimulatorKind { BUDGET, COMPOUND, DEBT, INFLATION, FEES, PAYSLIP }

/** Whether a headline number is something gained or something it costs you. */
enum class Tone { GOOD, COST }

/** Every symbol in every formula gets a name, a meaning and a real value. No exceptions. */
data class Variable(
    val symbol: String,
    val name: String,
    val meaning: String,
    val example: String,
)

data class TaxBandRow(val range: String, val rate: String)

/** One line of a worked example. [math] is shown in monospace when a step is arithmetic. */
data class ExampleStep(val text: String, val math: String? = null)

data class Question(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val why: String,
    val whyNotOthers: String,
) {
    init {
        require(options.size >= 2) { "a check question needs at least two options" }
        require(correctIndex in options.indices) { "correctIndex $correctIndex is not an option" }
    }
}

/** Block ①. No numbers at all: this block exists to say why the reader should care. */
data class Idea(
    val paragraphs: List<String>,
    val whySchoolSkipsIt: String,
)

/** Block ②. The real formula, and everything needed to read it. */
data class Mechanism(
    val intro: String,
    val formulas: List<String>,
    val plainEnglish: String,
    val variables: List<Variable>,
    val steps: List<String>,
    val watchOut: String,
    val bands: List<TaxBandRow> = emptyList(),
    val bandsNote: String? = null,
)

/** Block ③. One named person, real figures, and an honest word about the limits. */
data class RealNumbers(
    val persona: String,
    val steps: List<ExampleStep>,
    val punchline: String,
    val punchlineTone: Tone,
    val realityCheck: String,
)

data class Module(
    val id: String,
    val number: Int,
    val title: String,
    val hook: String,
    val minutes: Int,
    val idea: Idea,
    val mechanism: Mechanism,
    val realNumbers: RealNumbers,
    val simulator: SimulatorKind,
    val check: List<Question>,
    val takeaway: String,
    /** The bridge from "I understood" to "I did": one free, concrete thing, this week. */
    val action: String,
    val track: Track = Track.CORE,
) {
    val displayNumber: String get() = number.toString().padStart(2, '0')
}
