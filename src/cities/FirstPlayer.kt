package cities

import java.io.File

class FirstPlayer : CommonPlayer(
    playerName = "FirstPlayer",
    targetPlayerName = "SecondPlayer",
    isInitiator = true,
    citiesFile = File("resources/cities.txt"),
    usedFile = File("resources/used.txt"),
)