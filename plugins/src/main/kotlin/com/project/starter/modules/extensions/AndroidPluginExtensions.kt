package com.project.starter.modules.extensions

import com.project.starter.listProperty
import com.project.starter.property
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class AndroidExtension(objects: ObjectFactory) {
    var javaFilesAllowed = objects.property<Boolean>(default = null)
    var defaultVariants = objects.listProperty<String>()
    var coverageExclusions = objects.listProperty<String>()
}

open class AndroidLibraryConfigExtension @Inject constructor(
    objects: ObjectFactory
) : AndroidExtension(objects) {
    var generateBuildConfig = objects.property(default = false)
}

open class AndroidApplicationConfigExtension @Inject constructor(
    objects: ObjectFactory
) : AndroidExtension(objects)
