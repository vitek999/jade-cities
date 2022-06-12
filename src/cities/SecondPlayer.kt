package cities

import java.io.File

class SecondPlayer : CommonPlayer(
    playerName = "SecondPlayer",
    targetPlayerName = "FirstPlayer",
    citiesFile = File("resources/cities.txt"),
    usedFile = File("resources/used.txt"),
)