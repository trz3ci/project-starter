package com.project.starter.modules.internal

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.AndroidExtension
import com.project.starter.modules.tasks.ForbidJavaFilesTask.Companion.addForbidJavaFilesTask
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.addProjectCoverageTask
import com.project.starter.modules.tasks.ProjectLintTask.Companion.addProjectLintTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.addProjectTestTask
import com.project.starter.quality.internal.configureAndroidCoverage
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

internal fun BaseExtension.configureAndroidPlugin(rootConfig: RootConfigExtension) {
    defaultConfig.apply {
        compileSdkVersion(rootConfig.android.compileSdkVersion.get())
        minSdkVersion(rootConfig.android.minSdkVersion.get())
        targetSdkVersion(rootConfig.android.targetSdkVersion.orNull ?: rootConfig.android.compileSdkVersion.get())
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    dexOptions {
        it.preDexLibraries = true
    }
    addKotlinSourceSets()

    compileOptions.apply {
        sourceCompatibility = rootConfig.javaVersion.get()
        targetCompatibility = rootConfig.javaVersion.get()
    }
}

internal fun Project.configureAndroidProject(variants: DomainObjectSet<out BaseVariant>, projectConfig: AndroidExtension) {
    configureAndroidCoverage(variants, projectConfig.coverageExclusions.get())
    val findBuildVariants = {
        if (projectConfig.defaultVariants.get().isEmpty()) {
            val default = variants.firstOrNull { it.buildType.name == "debug" }
                ?: variants.first()

            listOf(default.name.capitalize())
        } else {
            projectConfig.defaultVariants.get()
        }
    }
    addProjectLintTask { projectLint ->
        val childTasks = findBuildVariants().map { "$path:lint${it.capitalize()}" }
        childTasks.forEach { projectLint.dependsOn(it) }
    }
    addProjectTestTask { projectTest ->
        val childTasks = findBuildVariants().map { "$path:test${it.capitalize()}UnitTest" }
        childTasks.forEach { projectTest.dependsOn(it) }
    }
    addProjectCoverageTask { projectCoverage ->
        val childTasks = findBuildVariants().map { "$path:jacoco${it.capitalize()}TestReport" }
        childTasks.forEach { projectCoverage.dependsOn(it) }
    }
    val javaFilesAllowed = projectConfig.javaFilesAllowed.orNull ?: rootConfig.javaFilesAllowed.get()
    if (!javaFilesAllowed) {
        tasks.named("preBuild").dependsOn(addForbidJavaFilesTask())
    }
}

private fun BaseExtension.addKotlinSourceSets() {
    sourceSets.all { set ->
        val withKotlin = set.java.srcDirs.map { it.path.replace("java", "kotlin") }
        set.java.setSrcDirs(set.java.srcDirs + withKotlin)
    }
}
