package com.yonatankarp.ff4k.buildlogic

import org.gradle.api.Project
import java.io.File
import java.util.Properties

fun Project.isAndroidSdkAvailable(): Boolean {
    val env = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    if (env != null && File(env).exists()) return true
    val localProperties = rootProject.file("local.properties")
    if (localProperties.exists()) {
        val properties = Properties()
        localProperties.inputStream().use { properties.load(it) }
        val sdkDir = properties.getProperty("sdk.dir")
        return sdkDir != null && File(sdkDir).exists()
    }
    return false
}

fun Project.areAppleTargetsAvailable(): Boolean {
    val localProperties = rootProject.file("local.properties")
    if (localProperties.exists()) {
        val properties = Properties()
        localProperties.inputStream().use { properties.load(it) }
        if (properties.getProperty("ff4k.include.apple") == "false") return false
    }
    val osName = System.getProperty("os.name")
    if (!osName.contains("Mac", ignoreCase = true)) return false
    return File("/usr/bin/xcrun").exists()
}
