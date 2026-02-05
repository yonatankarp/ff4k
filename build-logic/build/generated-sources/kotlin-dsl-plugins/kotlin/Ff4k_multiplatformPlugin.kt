/**
 * Precompiled [ff4k.multiplatform.gradle.kts][Ff4k_multiplatform_gradle] script plugin.
 *
 * @see Ff4k_multiplatform_gradle
 */
public
class Ff4k_multiplatformPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Ff4k_multiplatform_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
