package com.project.starter.config.extensions

import com.project.starter.property
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.model.ObjectFactory
import org.gradle.util.ConfigureUtil

open class RootConfigExtension(objects: ObjectFactory) {

    var javaVersion = objects.property(JavaVersion.VERSION_1_8)
    var javaFilesAllowed = objects.property(default = true)

    val quality = QualityPluginConfig(objects)
    val android = AndroidPluginConfig(objects)
    val versioning = VersioningPluginConfig(objects)

    fun qualityPlugin(c: Closure<QualityPluginConfig>) =
        ConfigureUtil.configure(c, quality)

    fun qualityPlugin(action: Action<QualityPluginConfig>) =
        action.execute(quality)

    fun androidPlugin(c: Closure<AndroidPluginConfig>) =
        ConfigureUtil.configure(c, android)

    fun androidPlugin(action: Action<AndroidPluginConfig>) =
        action.execute(android)

    fun versioningPlugin(c: Closure<VersioningPluginConfig>) =
        ConfigureUtil.configure(c, versioning)

    fun versioningPlugin(action: Action<VersioningPluginConfig>) =
        action.execute(versioning)
}

open class QualityPluginConfig(
    objects: ObjectFactory
) {
    var formatOnCompile = objects.property(default = false)
    var enabled= objects.property(default = true)
}

open class AndroidPluginConfig(objects: ObjectFactory) {
    var compileSdkVersion = objects.property(default = 29)
    var minSdkVersion = objects.property(default = 23)
    var targetSdkVersion = objects.property<Int?>(default = null)
}

open class VersioningPluginConfig(objects: ObjectFactory) {
    var enabled = objects.property(default = true)
}
