package io.github.cdsap.fingerprinting.report

import io.github.cdsap.fingerprinting.view.FingerprintingView
import io.github.cdsap.geapi.client.domain.impl.GetBuildScansWithQueryImpl
import io.github.cdsap.geapi.client.domain.impl.GetCachePerformanceImpl
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class FingerPrintingReport(
    private val filter: Filter,
    private val repository: GradleEnterpriseRepository,
    private val cacheRepository: GradleEnterpriseRepository

) {

    suspend fun process() {
        val getBuildScans = GetBuildScansWithQueryImpl(repository)
        val getOutcome = GetCachePerformanceImpl(cacheRepository)
        val buildScansFiltered = getBuildScans.get(filter)
        val outcome = getOutcome.get(buildScansFiltered, filter).filter { cacheableTasks(it) }
        if (outcome.isNotEmpty()) {
            FingerprintingView(outcome).print(filter)
        }
    }

    private fun cacheableTasks(it: Build) = if (it.builtTool == "gradle") {
        it.taskExecution.any {
            (it.avoidanceOutcome == "avoided_from_local_cache" || it.avoidanceOutcome == "avoided_from_remote_cache") &&
                it.fingerprintingDuration > filter.maxDuration
        }
    } else {
        it.goalExecution.any {
            (it.avoidanceOutcome == "avoided_from_local_cache" || it.avoidanceOutcome == "avoided_from_remote_cache") &&
                it.fingerprintingDuration > filter.maxDuration
        }
    }
}
