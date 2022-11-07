package dev.jamiecraane.ksp.naventry.annotations

/**
 * Defines a navigation route. The name is the fully qualified name of the navigation route, ie: profileScreen/{profileId}
 *
 * ```
 * sealed class NavigationEvent
 *
 * const val name = "com.example.NavigationEvent"
 *
 * @Route("race/{seasonId}/{raceId}", baseClassQualifiedName = name)
 * @Argument(name = "seasonId", type = ArgType.STRING)
 * @Argument(name = "raceId", type = ArgType.STRING)
 * object RaceDetails
 * ```
 *
 * Please note the baseClassQualifiedName is a String at the moment. Passing a class name which is not present in the build path
 * of the ksp processor does not work since that class is not on the classpath. KSP inspects the passed-in class which results
 * in a NoClassDefFoundError.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Route(val path: String)

/**
 * Defines a single nav argument. The name should match the path in the Route annotation.
 */
@Repeatable
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Argument(val name: String, val type: ArgType)

/**
 * Supported types for individual nav arguments.
 */
enum class ArgType {
    STRING,
    FLOAT,
    INT,
    LONG
}
