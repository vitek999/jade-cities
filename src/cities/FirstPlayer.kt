package cities

import java.io.File

class FirstPlayer : CommonPlayer(
    playerName = "FirstPlayer",
    targetPlayerName = "SecondPlayer",
    isInitiator = true,
    citiesFile = File("resources/real_cities.txt"),
    usedFile = File("resources/used.txt"),
)