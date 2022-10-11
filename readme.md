## todo
- Proof of Concept, not production ready yet.
- Add iOS examples and perhaps adapt the generated code.
- Generate supporting code, for example extension function to convert generic args to NavHost args.
- Support more types for arguments than the four primitives it does now.
- It is required to define a base class which is preferably a sealed class to make a closed navigation event hierarchy. This might change in the future.
- There is no support for nesting navigation events at the moment.
- The library is not present in a Maven repository yet. Install locally by executing: ```./gradelw publishToMavenLocal```

This projects implements a Kotlin Symbol Processor (KSP) to help implement navigation between screens in KMM apps.

A navigation destination typically contains the following data:
- a path which defines the destination: race/{raceId}
- the arguments themselves
- the type of the arguments
- the resolved path with the actual values of the arguments: race/5

This KSP enables declaring the above requirements as a navigation event using annotations:

```kotlin
sealed class BaseNavigationEvent

@Route("race/{raceId}", baseClassQualifiedName = "BaseNavigationEvent")
@Argument(name = "raceId", type = ArgType.STRING)
object RaceDetails
```

This will generate the following code:

```kotlin
public class RaceDetailsNavEvent(
  public val raceId: String,
) : BaseNavigationEvent() {
  public fun route(): String = "race/{raceId}"
  .replace("{raceId}", raceId)

  public companion object {
    public val route: String = "race/{raceId}"

    public val raceId: String = "raceId"

    public val arguments: List<Pair<String, ArgType>> = listOf(Pair("raceId",ArgType.STRING))
  }
}
```

This generated code can be used like this:

**1 in commonMain to actually dispatch a navigation event**

```kotlin
scope.launch {
    navigator.navigate(RaceDetailsNavEvent(raceId = 10)) // Creates RaceDetailsNavEvent for race with raceId = 10 
}
```

The above navigator object is implemented as follows. The navigator defines a channel to which navigation events are send to.
[style.json](../../../../Downloads/ACCEPTANCE-(in-use)(ckzy3z68o002816muigbjriq7)/style.json)
```kotlin
class ScreenNavigator {
    private val eventChannel = Channel<BaseNavigationEvent>()

    val navigationEvents = eventChannel.receiveAsFlow()

    suspend fun navigate(event: BaseNavigationEvent) {
        eventChannel.send(event)
    }
}
```

Those navigation events can be consumed and used (in Android) as follows:

```kotlin
val event: BaseNavigationEvent? by navigator.navigationEvents.collectAsStateWithLifecycle(null)

event.let {
    when (it) {
        is RaceDetailsNavEvent -> navHostController.navigate(it.route()) // it.route gives: race/10
        null -> { /*Do nothing*/ }
    }
}
```

To define the actual routes in the NavHost (Android) do the following:

```kotlin
composable(
    route = RaceDetailsNavEvent.route,
    arguments = RaceDetailsNavEvent.arguments.toNavArguments,
) { entry ->
    val raceId = entry.arguments?.getString(RaceDetailsNavEvent.raceId) ?: ""
    RaceScreen(seasonViewModel, raceId)
}
```

The toNavArguments is an extension property which converts generic args to NavHost args. This might be included in the code generator
in the future.

```kotlin
val List<Pair<String, ArgType>>.toNavArguments: List<NamedNavArgument>
    get() {
        return this.map { argument ->
            navArgument(argument.first) {
                when (argument.second) {
                    ArgType.STRING -> NavType.StringType
                    ArgType.FLOAT -> NavType.FloatType
                    ArgType.INT -> NavType.IntType
                    ArgType.LONG -> NavType.LongType
                }
            }
        }
    }
```
