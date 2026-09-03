package com.cashfluent.app.content

import com.cashfluent.app.content.module.BudgetingModule
import com.cashfluent.app.content.module.CompoundInterestModule
import com.cashfluent.app.content.module.CreditRecordModule
import com.cashfluent.app.content.module.DebtModule
import com.cashfluent.app.content.module.InflationModule
import com.cashfluent.app.content.module.InstalmentsModule
import com.cashfluent.app.content.module.InvestingModule
import com.cashfluent.app.content.module.PayslipModule
import com.cashfluent.app.content.module.RentVsBuyModule
import com.cashfluent.app.content.module.SideIncomeModule

/**
 * The whole curriculum, in the order it is meant to be read. Everything is open by
 * default; the order is a recommendation the Home screen makes visible, not a lock.
 */
object Modules {

    val core: List<Module> = listOf(
        BudgetingModule,
        CompoundInterestModule,
        DebtModule,
        InflationModule,
        InvestingModule,
        PayslipModule,
        InstalmentsModule,
        CreditRecordModule,
        SideIncomeModule,
        RentVsBuyModule,
    )

    val all: List<Module> = core

    val coreIds: List<String> = core.map { it.id }

    private val byId: Map<String, Module> = all.associateBy { it.id }

    fun byId(id: String): Module? = byId[id]

    /** The module after [id] in reading order, for the "Up next" card. */
    fun next(id: String): Module? {
        val index = all.indexOfFirst { it.id == id }
        return if (index == -1) null else all.getOrNull(index + 1)
    }

    /**
     * With the guided path on, a module is open once everything before it is finished.
     * With it off — the default — this is never consulted.
     */
    fun isUnlocked(module: Module, isDone: (String) -> Boolean): Boolean {
        val index = all.indexOfFirst { it.id == module.id }
        if (index <= 0) return true
        return all.take(index).all { isDone(it.id) }
    }
}
