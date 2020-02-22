package com.project.starter.modules.extensions

import com.project.starter.property
import org.gradle.api.model.ObjectFactory

open class KotlinLibraryConfigExtension(
    objects: ObjectFactory
) {

    var javaFilesAllowed = objects.property<Boolean?>(default = null)
}
