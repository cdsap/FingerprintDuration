package io.github.cdsap.fingerprinting.view


import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.renderText
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.Filter
import java.io.File

import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class FingerprintingView(private val outcome: List<Build>) {

    fun print(filter: Filter) {
        printSummary(filter)
        generateExtendedReport(outcome, filter)
    }

    private fun printSummary(filter: Filter) {
        println(
            table {
                cellStyle {
                    border = true
                    alignment = TextAlignment.MiddleLeft
                    padding = 1
                }
                body {

                    row {
                        cell(
                            "${outcome.size} Builds found with tasks/goals cached with FingerPrinting Time  > ${
                                filter.maxDuration.toDuration(
                                    DurationUnit.MILLISECONDS
                                )
                            }"
                        ) {
                            columnSpan = 4
                            alignment = TextAlignment.MiddleLeft
                        }
                    }
                    if (outcome.isNotEmpty()) {
                        row {
                            cell("Build Scan")
                            cell("Project")
                            cell("Date")
                            cell("Total Fingerprinting duration FROM-CACHE tasks/goals")
                        }
                    }
                    outcome.forEach {

                        row {
                            cell("${filter.url}/s/${it.id}")
                            cell(it.projectName)
                            cell(SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(it.buildStartTime)).toString())
                            cell(duration(it).toDuration(DurationUnit.MILLISECONDS)) {
                                alignment = TextAlignment.MiddleRight
                            }
                        }
                    }
                }
            })
    }

    private fun generateExtendedReport(outcome: List<Build>, filter: Filter) {
        if (File("extended_report.txt").exists()) {
            File("extended_report.txt").delete()
        }
        File("extended_report.txt").writeText(
            table {
                cellStyle {
                    border = true
                    alignment = TextAlignment.MiddleLeft
                    padding = 1
                }
                body {
                    row {
                        cell(
                            "${outcome.size} builds found with tasks/goals cached with FingerPrinting Time  > ${
                                filter.maxDuration.toDuration(DurationUnit.MILLISECONDS)
                            }"
                        ) {
                            columnSpan = 6
                            alignment = TextAlignment.MiddleLeft
                        }
                    }
                }
                outcome.forEach {
                    val buildId = it.id
                    row {
                        cell("Build")
                        cell("${filter.url}/s/${it.id}") {
                            columnSpan = 5
                        }

                    }
                    row {
                        cell("Project")
                        cell(it.projectName) {
                            columnSpan = 5
                        }

                    }
                    row {
                        cell("Date")
                        cell(SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(it.buildStartTime)).toString()) {
                            columnSpan = 5
                        }
                    }
                    row {
                        cell("Task/Goal")
                        cell(
                            it.requestedTask.joinToString(" ")
                        ) {
                            columnSpan = 5
                        }
                    }
                    row {
                        cell("Tags")
                        cell(it.tags.joinToString(",")) {
                            columnSpan = 5
                        }

                    }

                    if (it.builtTool == "gradle") {
                        row {
                            cell("Tasks types") {
                                columnSpan = 6
                            }
                        }
                        it.taskExecution.filter {
                            it.avoidanceOutcome == "avoided_from_local_cache" || it.avoidanceOutcome == "avoided_from_remote_cache"
                        }
                            .forEach {
                                val duration = it.fingerprintingDuration
                                if (duration > filter.maxDuration) {
                                    row {
                                        cell("${it.taskPath}\n${filter.url}/s/$buildId/timeline?outcome=from-cache&task-path=${it.taskPath}") {
                                            alignment = TextAlignment.MiddleLeft
                                            columnSpan = 3
                                        }
                                        cell(duration.toDuration(DurationUnit.MILLISECONDS)) {
                                            columnSpan = 3
                                            alignment = TextAlignment.MiddleCenter
                                        }
                                    }
                                }
                            }
                    } else {
                        row {
                            cell("Goal mojos") {
                                columnSpan = 6
                            }
                        }
                        it.goalExecution.filter {
                            it.avoidanceOutcome == "avoided_from_local_cache" || it.avoidanceOutcome == "avoided_from_remote_cache"
                        }
                            .forEach {
                                val duration = it.fingerprintingDuration
                                if (duration > filter.maxDuration) {
                                    row {
                                        cell("${it.goalExecutionId}\n${filter.url}/s/$buildId/timeline?outcome=from-cache&goal-execution=${it.goalExecutionId}") {
                                            alignment = TextAlignment.MiddleLeft
                                            columnSpan = 3
                                        }
                                        cell(duration.toDuration(DurationUnit.MILLISECONDS)) {
                                            columnSpan = 3
                                            alignment = TextAlignment.MiddleCenter
                                        }
                                    }
                                }
                            }
                    }

                }
            }.renderText()
        )
        if (outcome.isNotEmpty()) {
            println("For more information on the tasks/goals check extended_report.txt")
        }
    }

    private fun duration(build: Build): Long {
        return if (build.builtTool == "gradle") {
            build.taskExecution.filter {
                it.avoidanceOutcome == "avoided_from_local_cache" || it.avoidanceOutcome == "avoided_from_remote_cache"
            }.sumOf { it.duration }
        } else {
            build.goalExecution.filter {
                it.avoidanceOutcome == "avoided_from_local_cache" || it.avoidanceOutcome == "avoided_from_remote_cache"
            }.sumOf { it.duration }
        }
    }
}

