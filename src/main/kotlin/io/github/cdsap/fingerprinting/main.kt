package io.github.cdsap.fingerprinting

import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.network.GEClient
import io.github.cdsap.geapi.client.repository.impl.GradleRepositoryImpl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import io.github.cdsap.fingerprinting.report.FingerPrintingReport

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    Fingerprinting().main(args)
}

class Fingerprinting : CliktCommand() {
    private val apiKey: String by option().required()
    private val url by option().required()
    private val maxBuilds by option().int().default(1000)
    private val project: String? by option().required()
    private val tags: List<String> by option().multiple(default = emptyList())
    private val concurrentCalls by option().int().default(150)
    private val user: String? by option()
    private val maxDuration by option().long().required()

    override fun run() {
        val filter = Filter(
            url = url,
            maxBuilds = maxBuilds,
            project = project,
            tags = tags,
            initFilter = System.currentTimeMillis(),
            user = user,
            concurrentCalls = concurrentCalls,
            maxDuration = maxDuration
        )
        val repository = GradleRepositoryImpl(GEClient(apiKey, url))

        runBlocking {
            FingerPrintingReport(filter, repository).process()
        }
    }
}


