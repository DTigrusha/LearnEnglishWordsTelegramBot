import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String? = null,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

fun main(args: Array<String>) {

    val json = Json {
        ignoreUnknownKeys = true
    }
    val telegramBot = TelegramBotService(args[0])
    var lastUpdateId: Long = 0
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val result = runCatching { telegramBot.getUpdates(lastUpdateId) }
        val responseString: String = result.getOrNull() ?: continue
        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, json, trainers, telegramBot) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    json: Json,
    trainers: HashMap<Long, LearnWordsTrainer>,
    telegramBot: TelegramBotService,
) {
    val message = update.message?.text
    val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    if (message?.contains("/start", true) == true) {
        telegramBot.sendMenu(chatId, json)
    }
    if (data?.contains(STATISTICS_CLICKED) == true) {
        val statistics = trainer.getStatistics()
        telegramBot.sendMessage(
            chatId,
            "Ваша статистика: выучено ${statistics.numberOfLearnedWords} из ${statistics.numberOfWords} слов |" +
                    " ${statistics.progressInPercent} %.",
            json,
        )
    }
    if (data == RESET_CLICKED) {
        trainer.resetProgress()
        telegramBot.sendMessage(chatId, "Прогресс сброшен!", json)
    }
    if (data?.contains(LEARN_WORDS_CLICKED) == true) {
        trainer.checkAndUpdateUserWordsFile()
        telegramBot.checkNextQuestionAndSend(trainer.getNextQuestion(), chatId, json)
    }
    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
        if (trainer.checkAnswer(userAnswerIndex)) {
            telegramBot.sendMessage(chatId, "Правильно!", json)
        } else {
            telegramBot.sendMessage(
                chatId,
                "Неправильно:\n${trainer.question?.wordForLearning?.englishWord} - " +
                        "${trainer.question?.wordForLearning?.translation}.",
                json
            )
        }
        telegramBot.checkNextQuestionAndSend(trainer.getNextQuestion()!!, chatId, json)
    }
    if (data == MENU_CLICKED) {
        telegramBot.sendMenu(chatId, json)
    }
}