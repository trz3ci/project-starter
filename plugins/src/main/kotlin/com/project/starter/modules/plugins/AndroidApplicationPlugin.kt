package com.project.starter.modules.plugins

import com.android.build.gradle.AppExtension
import com.project.starter.config.plugins.rootConfig
import com.project.starter.configure
import com.project.starter.modules.extensions.AndroidApplicationConfigExtension
import com.project.starter.modules.internal.configureAndroidLint
import com.project.starter.modules.internal.configureAndroidPlugin
import com.project.starter.modules.internal.configureAndroidProject
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("kotlin-android")

        val extension = extensions.create("projectConfig", AndroidApplicationConfigExtension::class.java)

        pluginManager.apply(ConfigurationPlugin::class.java)

         val android = extensions.configure<AppExtension> {
            configureAndroidPlugin(rootConfig)
            configureAndroidLint(lintOptions)
        }

        configureAndroidProject(android.applicationVariants, extension)
    }
}
