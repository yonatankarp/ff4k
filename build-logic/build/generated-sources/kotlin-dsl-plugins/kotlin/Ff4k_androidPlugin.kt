/**
 * Precompiled [ff4k.android.gradle.kts][Ff4k_android_gradle] script plugin.
 *
 * @see Ff4k_android_gradle
 */
public
class Ff4k_androidPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Ff4k_android_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
