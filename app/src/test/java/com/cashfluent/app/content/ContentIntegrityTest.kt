package com.cashfluent.app.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The content is typed Kotlin, so most mistakes are compile errors. These cover the ones
 * that are not: a question whose correct answer points at nothing, a module missing its
 * reality check, a blank string that would ship as an empty line in front of a judge.
 */
class ContentIntegrityTest {

    private val modules = Modules.all

    @Test
    fun `there are six core modules, numbered one to six in order`() {
        assertEquals(6, Modules.core.size)
        assertEquals((1..6).toList(), modules.map { it.number })
        assertEquals(listOf("01", "02", "03", "04", "05", "06"), modules.map { it.displayNumber })
    }

    @Test
    fun `ids are unique and resolvable`() {
        assertEquals(modules.size, modules.map { it.id }.toSet().size)
        modules.forEach { assertEquals(it, Modules.byId(it.id)) }
        assertEquals(null, Modules.byId("nope"))
    }

    @Test
    fun `every module has all three blocks filled in`() {
        modules.forEach { module ->
            val where = "module ${module.id}"
            assertTrue("$where: idea needs paragraphs", module.idea.paragraphs.size >= 2)
            assertTrue("$where: needs a school note", module.idea.whySchoolSkipsIt.isNotBlank())
            assertTrue("$where: needs at least one formula", module.mechanism.formulas.isNotEmpty())
            assertTrue("$where: needs named variables", module.mechanism.variables.size >= 3)
            assertTrue("$where: needs steps", module.mechanism.steps.size >= 3)
            assertTrue("$where: needs a watch out", module.mechanism.watchOut.isNotBlank())
            assertTrue("$where: needs worked steps", module.realNumbers.steps.size >= 2)
            assertTrue("$where: needs a punchline", module.realNumbers.punchline.isNotBlank())
            assertTrue("$where: needs a reality check", module.realNumbers.realityCheck.isNotBlank())
        }
    }

    @Test
    fun `every check has answerable questions with both explanations`() {
        modules.forEach { module ->
            assertTrue("module ${module.id} needs at least two questions", module.check.size >= 2)
            module.check.forEachIndexed { index, question ->
                val where = "${module.id} question $index"
                assertTrue("$where: correct answer out of range", question.correctIndex in question.options.indices)
                assertTrue("$where: needs three options", question.options.size >= 3 || module.check.size == 2)
                assertTrue("$where: needs a why", question.why.isNotBlank())
                assertTrue("$where: needs a why-not", question.whyNotOthers.isNotBlank())
                assertEquals("$where: duplicate options", question.options.size, question.options.toSet().size)
            }
        }
    }

    @Test
    fun `nothing anywhere is blank`() {
        modules.forEach { module ->
            val strings = buildList {
                add(module.title); add(module.hook); add(module.takeaway); add(module.action)
                addAll(module.idea.paragraphs)
                add(module.mechanism.intro); add(module.mechanism.plainEnglish)
                addAll(module.mechanism.formulas)
                addAll(module.mechanism.steps)
                module.mechanism.variables.forEach {
                    add(it.symbol); add(it.name); add(it.meaning); add(it.example)
                }
                add(module.realNumbers.persona)
                module.realNumbers.steps.forEach { add(it.text) }
                module.check.forEach { question ->
                    add(question.prompt)
                    addAll(question.options)
                }
            }
            strings.forEach { assertTrue("blank string in ${module.id}", it.isNotBlank()) }
        }
    }

    @Test
    fun `every module ends with something to do this week`() {
        modules.forEach { module ->
            assertTrue(
                "module ${module.id} needs a concrete action",
                module.action.length > 40,
            )
        }
    }

    @Test
    fun `reading order chains correctly and stops at the end`() {
        assertEquals(modules[1], Modules.next(modules[0].id))
        assertEquals(null, Modules.next(modules.last().id))
        assertEquals(null, Modules.next("nope"))
    }

    @Test
    fun `the guided path opens modules one at a time`() {
        val nothingDone = { _: String -> false }
        assertTrue("the first module is always open", Modules.isUnlocked(modules[0], nothingDone))
        assertFalse("the second waits for the first", Modules.isUnlocked(modules[1], nothingDone))

        val firstDone = { id: String -> id == modules[0].id }
        assertTrue(Modules.isUnlocked(modules[1], firstDone))
        assertFalse(Modules.isUnlocked(modules[2], firstDone))
    }

    @Test
    fun `only the payslip module claims illustrative tax bands`() {
        val withBands = modules.filter { it.mechanism.bands.isNotEmpty() }
        assertEquals(listOf("payslip"), withBands.map { it.id })
        assertTrue(
            "illustrative rates must say so on screen",
            withBands.single().mechanism.watchOut.contains("illustrative"),
        )
    }

    @Test
    fun `each simulator is used exactly once`() {
        val kinds = modules.map { it.simulator }
        assertEquals(kinds.size, kinds.toSet().size)
        assertEquals(SimulatorKind.entries.size, kinds.size)
    }
}
