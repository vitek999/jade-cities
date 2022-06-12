package cities

import jade.core.Agent
import jade.core.behaviours.CyclicBehaviour
import jade.domain.AMSService
import jade.domain.FIPAAgentManagement.AMSAgentDescription
import jade.domain.FIPAAgentManagement.SearchConstraints
import jade.lang.acl.ACLMessage
import java.io.File
import java.util.concurrent.TimeUnit

private const val CITIES_DELIMITER = ";"
private const val TIMEOUT_IM_MILLS = 250L
private const val MESSAGE_CITY_PREFIX = "city:"
private const val END_MESSAGE = "end"

open class CommonPlayer(
    private val playerName: String,
    private val targetPlayerName: String,
    private val isInitiator: Boolean = false,
    citiesFile: File,
    private val usedFile: File,
) : Agent() {
    init {
        check(usedFile.exists()) { "Used file should be exists" }
        check(citiesFile.exists()) { "Cities file should be exists" }
    }

    private val cities = citiesFile.readText().split(CITIES_DELIMITER).shuffled()
    private val usedCities get() = usedFile.readText().split(CITIES_DELIMITER)

    override fun setup() {
        println("Hello! I'm $playerName and i will play with $targetPlayerName")
        addBehaviour(CommonBehaviour(this, targetPlayerName, ::handleMessage))

        if (isInitiator) {
            runCatching { TimeUnit.SECONDS.sleep(1) }
            clearUsedFile()

            sendCity(cities.random())
        }
    }

    private fun handleMessage(message: String) {
        when {
            message.startsWith(MESSAGE_CITY_PREFIX) -> handleCityCommand(message.substringAfter(MESSAGE_CITY_PREFIX))
            message == END_MESSAGE -> handleEndMessage()
            else -> throw IllegalStateException("Unknown command")
        }
    }

    private fun handleCityCommand(cityName: String) {
        val newCityName = cities.firstOrNull { city -> city.startsWith(cityName.last()) && city !in usedCities }
        if (newCityName != null) {
            sendCity(newCityName)
        } else {
            println("[$playerName]: I haven't new cities... $targetPlayerName, you are win!")
            sendMessage(END_MESSAGE)
            doDelete()
        }
    }

    private fun sendCity(cityName: String) {
        println("[$playerName]: $cityName")
        writeToUsed(cityName)
        sendMessage(cityMessage(cityName))
    }

    private fun handleEndMessage() {
        println("[$playerName]: Thank you for the game, $targetPlayerName!")
        doDelete()
    }

    private fun sendMessage(message: String)  {
        val agents: Array<AMSAgentDescription> = runCatching {
            val searchConstraints = SearchConstraints().apply { maxResults = -1L }
            AMSService.search(this, AMSAgentDescription(), searchConstraints)
        }.getOrDefault(emptyArray())

        val targetAgent = agents.firstOrNull { it.name.localName == targetPlayerName }
        if (targetAgent != null) {
            val newMessage = ACLMessage(ACLMessage.INFORM).apply {
                addReceiver(targetAgent.name)
                language = "English"
                content = message
            }
            send(newMessage)
        }
    }

    private fun cityMessage(cityName: String) = MESSAGE_CITY_PREFIX + cityName

    private fun writeToUsed(cityName: String) {
        usedFile.appendText(cityName + CITIES_DELIMITER)
    }

    private fun clearUsedFile() {
        usedFile.writeText("")
    }
}

class CommonBehaviour(
    agent: Agent,
    private val targetPlayerName: String,
    private val handleMessage: (String) -> Unit
) : CyclicBehaviour(agent) {
    override fun action() {
        val message = agent.receive()
        if (message != null) {
            val senderAgentName = message.sender.localName
            if (senderAgentName == targetPlayerName) {
                runCatching { TimeUnit.MILLISECONDS.sleep(TIMEOUT_IM_MILLS) }
                handleMessage(message.content)
            }
        }
        block()
    }
}