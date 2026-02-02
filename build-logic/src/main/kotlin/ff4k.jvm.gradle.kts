import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

configure<KotlinMultiplatformExtension> {
    jvm()

    sourceSets {
        val jvmSharedMain = findByName("jvmSharedMain")
        val jvmSharedTest = findByName("jvmSharedTest")

        named("jvmMain") {
            jvmSharedMain?.let { dependsOn(it) }
        }

        named("jvmTest") {
            jvmSharedTest?.let { dependsOn(it) }
        }
    }
}
