package com.project.starter.versioning.plugins

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.project.starter.configure
import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookAction

class VersioningPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (this != rootProject) throw GradleException("Versioning plugin can be applied to the root project only")
        pluginManager.apply(ReleasePlugin::class.java)

        val scmConfig = extensions.getByType(VersionConfig::class.java).apply {
            tag.apply {
                versionSeparator = "/"
            }
            hooks.apply {
                preReleaseHooks.add(
                    ReleaseHookAction { context ->
                        Git.open(repository.directory).use {
                            val version = context.releaseVersion
                            val isNonPatchVersion = version.matches("^\\d+\\.\\d\\.0[-*]?$".toRegex())
                            if (isNonPatchVersion) {
                                it.branchCreate().setName("release/$version").call()
                            }
                        }
                    }
                )

                postReleaseHooks.add(
                    ReleaseHookAction {
                        try {
                            Git.open(repository.directory)
                                .push()
                                .setPushAll()
                                .setPushTags()
                                .setRemote(repository.remote)
                                .call()
                        } catch (@Suppress("TooGenericExceptionCaught") error: Throwable) {
                            logger.error("Couldn't push. Run `git push --tags --all` manually.", error)
                        }
                    }
                )
            }
        }

        afterEvaluate {
            println("Has extension")
            allprojects { project ->
                println("APPLIED ${project.path}")
                project.version = scmConfig.version

                val configureVersion: BaseExtension.() -> Unit = {
                    val versionElements = scmConfig.undecoratedVersion.split(".")
                    val minor = versionElements[0].toInt()
                    val major = versionElements[1].toInt()
                    val patch = versionElements[2].toInt()
                    defaultConfig.versionCode = minor * 1_000_000 + major * 1000 + patch
                    defaultConfig.versionName = "$minor.$major.$patch"
                }
                project.pluginManager.withPlugin("com.android.library") {
                    project.extensions.configure<LibraryExtension>().configureVersion()
                }
                project.pluginManager.withPlugin("com.android.application") {
                    println("APPLIED2 ")
                    project.extensions.configure<AppExtension> {
                        println("DUPA2: " + scmConfig.undecoratedVersion)
                        configureVersion()
                    }
                }
            }
        }
    }
}
