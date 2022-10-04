import dev.jamiecraane.example.NavigationEvent
import dev.jamiecraane.example.ProfileDetailsNavEvent
import dev.jamiecraane.example.RaceDetailsNavEvent

fun main(args: Array<String>) {
    println(RaceDetailsNavEvent.route)
    println(RaceDetailsNavEvent.raceId)
    println(RaceDetailsNavEvent.seasonId)
    val raceDetailsEvent = RaceDetailsNavEvent("10", "100")
    println(raceDetailsEvent.route())

    val profileEvent = ProfileDetailsNavEvent("101")

    val event = profileEvent as NavigationEvent

    when (event) {
        is ProfileDetailsNavEvent -> println("profile details event")
        is RaceDetailsNavEvent -> println("race details event")
    }
}
