package io.github.cdsap.fingerprinting.report

import com.google.gson.Gson
import io.github.cdsap.fingerprinting.view.FingerprintingView
import io.github.cdsap.geapi.client.domain.impl.GetBuildScansWithQueryImpl
import io.github.cdsap.geapi.client.domain.impl.GetCachePerformanceImpl
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths

class FingerPrintingReport(
    private val filter: Filter,
    private val repository: GradleEnterpriseRepository
) {


    suspend fun process() {
        if (File("outcome").exists()) {
            val outcome2 =
                Gson().fromJson(Files.newBufferedReader(Paths.get(File("outcome").path)), Array<Build>::class.java)
                    .toList()
            val filter2 = Gson().fromJson(Files.newBufferedReader(Paths.get(File("filter").path)), Filter::class.java)
            FingerprintingView(outcome2).print(filter2)
        } else {

            val getBuildScans = GetBuildScansWithQueryImpl(repository)
            val getOutcome = GetCachePerformanceImpl(repository)
            val buildScansFiltered = getBuildScans.get(filter)
            val outcome = getOutcome.get(buildScansFiltered, filter)
                .filter {
                    if (it.builtTool == "gradle") {
                        it.taskExecution.filter {
                            (it.avoidanceOutcome == "avoided_from_local_cache" || it.avoidanceOutcome == "avoided_from_remote_cache") &&
                                it.fingerprintingDuration > filter.maxDuration
                        }.isNotEmpty()
                    } else {
                        it.goalExecution.filter {
                            (it.avoidanceOutcome == "avoided_from_local_cache" || it.avoidanceOutcome == "avoided_from_remote_cache") &&
                                it.fingerprintingDuration > filter.maxDuration
                        }.isNotEmpty()
                    }
                }

            val fw = FileWriter(File("outcome"))
            val bw = BufferedWriter(fw)
            Gson().toJson(outcome, bw)
            bw.close()
            outcome.toTypedArray()

            val fw2 = FileWriter(File("filter"))
            val bw2 = BufferedWriter(fw2)
            Gson().toJson(filter, bw2)
            bw2.close()
            FingerprintingView(outcome).print(filter)
        }
    }
}
