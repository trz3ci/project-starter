package com.project.starter.quality.plugins

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.internal.findByType
import com.project.starter.quality.tasks.IssueLinksTask.Companion.registerIssueCheckerTask
import com.project.starter.quality.internal.configureCheckstyle
import com.project.starter.quality.internal.configureDetekt
import com.project.starter.quality.internal.configureKtlint
import com.project.starter.quality.tasks.ProjectCodeStyleTask.Companion.addProjectCodeStyleTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class QualityPlugin : Plugin<Project> {

    private val SourceSet.kotlin
        get() = ((getConvention("kotlin") ?: getConvention("kotlin2js")) as? KotlinSourceSet)?.kotlin

    override fun apply(project: Project) = with(project) {
        repositories.jcenter()
        addProjectCodeStyleTask()
        configureKtlint()
        configureDetekt(rootConfig)
        configureCheckstyle(rootConfig)
        configureIssueCheckerTask()

        val config = rootConfig.quality
        if (config.formatOnCompile) {
            applyFormatOnRecompile()
        }
    }

    private fun Project.configureIssueCheckerTask() {
        registerIssueCheckerTask {
            val extension = project.extensions.findByType<BaseExtension>()
            if (extension != null) {
                extension.sourceSets.configureEach { sourceSet ->
                    source += sourceSet.java.srcDirs
                        .map { dir -> project.fileTree(dir) }
                        .reduce { merged: FileTree, tree: FileTree -> merged + tree }
                }
            } else {
                val javaPlugin = project.convention.getPlugin(JavaPluginConvention::class.java)
                javaPlugin.sourceSets.configureEach { sourceSet ->
                    source += sourceSet.java
                    val kotlin = sourceSet.kotlin ?: return@configureEach
                    source += kotlin.sourceDirectories.asFileTree
                }
            }
            report.set(buildDir.resolve("reports/issue_comments.txt"))
            githubToken.set(provider<String?> { properties["GITHUB_TOKEN"]?.toString() })
        }
    }

    private fun Project.applyFormatOnRecompile() {
        pluginManager.withPlugin("kotlin") {
            tasks.named("compileKotlin").dependsOn("$path:formatKotlin")
        }
        pluginManager.withPlugin("com.android.library") {
            tasks.named("preBuild").dependsOn("$path:formatKotlin")
        }
        pluginManager.withPlugin("com.android.application") {
            tasks.named("preBuild").dependsOn("$path:formatKotlin")
        }
    }

    private fun SourceSet.getConvention(name: String) =
        (this as HasConvention).convention.plugins[name]
}
