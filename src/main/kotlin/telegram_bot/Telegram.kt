package telegram_bot

fun main(args: Array<String>) {

    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val chatTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    val telegramBot = TelegramBotService(args[0])
    val trainer = LearnWordsTrainer()
    var lastUpdateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBot.getUpdates(lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId.plus(1)

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt() ?: continue
        val chatText = chatTextRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (chatText?.contains("/start", true) == true) {
            telegramBot.sendMenu(chatId)
        }
        if (data?.contains(STATISTICS_CLICKED) == true) {
            val statistics = trainer.getStatistics()
            telegramBot.sendMessage(
                chatId,
                "Ваша статистика: выучено ${statistics.numberOfLearnedWords} из ${statistics.numberOfWords} слов |" +
                        " ${statistics.progressInPercent} %.",
            )
        }
        if (data?.contains(LEARN_WORDS_CLICKED) == true) {
            telegramBot.checkNextQuestionAndSend(trainer, chatId)
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswerIndex)) {
                telegramBot.sendMessage(chatId, "Правильно!")
            } else {
                telegramBot.sendMessage(
                    chatId,
                    "Неправильно:\n${trainer.question?.wordForLearning?.englishWord} - " +
                            "${trainer.question?.wordForLearning?.translation}.",
                )
            }
            telegramBot.checkNextQuestionAndSend(trainer, chatId)
        }
    }
}