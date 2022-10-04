package dev.jamiecraane.example

import dev.jamiecraane.ksp.naventry.annotations.ArgType
import dev.jamiecraane.ksp.naventry.annotations.Argument
import dev.jamiecraane.ksp.naventry.annotations.Route

/**
 * Superclass for all generated navigation events.
 */
sealed class NavigationEvent

const val name = "dev.jamiecraane.example.NavigationEvent"

@Route("race/{seasonId}/{raceId}", baseClassQualifiedName = name)
@Argument(name = "seasonId", type = ArgType.STRING)
@Argument(name = "raceId", type = ArgType.STRING)
object RaceDetails

@Route("profile/{profileId}", baseClassQualifiedName = name)
@Argument(name = "profileId", type = ArgType.STRING)
object ProfileDetails
