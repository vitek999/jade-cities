package cities

import java.io.File

class SecondPlayer : CommonPlayer(
    playerName = "SecondPlayer",
    targetPlayerName = "FirstPlayer",
    citiesFile = File("resources/real_cities.txt"),
    usedFile = File("resources/used.txt"),
)