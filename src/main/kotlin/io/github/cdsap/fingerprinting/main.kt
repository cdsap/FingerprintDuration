package io.github.cdsap.fingerprinting

import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.network.GEClient
import io.github.cdsap.geapi.client.repository.impl.GradleRepositoryImpl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import io.github.cdsap.fingerprinting.report.FingerPrintingReport
import io.github.cdsap.geapi.client.network.ClientConf

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    Fingerprinting().main(args)
}

class Fingerprinting : CliktCommand() {
    private val apiKey: String by option().required()
    private val url by option().required()
    private val maxBuilds by option().int().default(1000).check("max builds to process 30000") { it <= 30000 }
    private val project: String? by option()
    private val tags: List<String> by option().multiple(default = emptyList())
    private val concurrentCalls by option().int().default(150)
    private val concurrentCallsCache by option().int().default(10)
    private val user: String? by option()
    private val duration by option().long().required()
        .check("duration must greater than 10s (10000)") { it >= 10000 }


    override fun run() {
        val filter = Filter(
            url = url,
            maxBuilds = maxBuilds,
            project = project,
            tags = tags,
            initFilter = System.currentTimeMillis(),
            user = user,
            concurrentCalls = concurrentCalls,
            maxDuration = duration,
            concurrentCallsConservative = concurrentCallsCache
        )
        val repository = GradleRepositoryImpl(
            GEClient(
                apiKey, url, ClientConf(
                    maxRetries = 300,
                    exponentialBase = 1.0,
                    exponentialMaxDelay = 5000
                )
            )
        )


        val cacheRepository = GradleRepositoryImpl(
            GEClient(
                apiKey, url, ClientConf(
                    maxRetries = 100,
                    exponentialBase = 1.0,
                    exponentialMaxDelay = 10000
                )
            )
        )

        runBlocking {
            FingerPrintingReport(filter, repository, cacheRepository).process()
        }
    }
}


