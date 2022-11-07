package dev.jamiecraane.example

import dev.jamiecraane.ksp.naventry.annotations.ArgType
import dev.jamiecraane.ksp.naventry.annotations.Argument
import dev.jamiecraane.ksp.naventry.annotations.Route

/**
 * Superclass for all generated navigation events.
 */
@Route("race/{seasonId}/{raceId}")
@Argument(name = "seasonId", type = ArgType.STRING)
@Argument(name = "raceId", type = ArgType.STRING)
object RaceDetails

@Route("profile/{profileId}")
@Argument(name = "profileId", type = ArgType.STRING)
object ProfileDetails
