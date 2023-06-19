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
        val updates: String = telegramBot.getUpdates(telegramBot.botToken, lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId.plus(1)

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()
        val chatText = chatTextRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (chatText?.contains("/start", true) == true && chatId != null) {
            telegramBot.sendMenu(telegramBot.botToken, chatId)
        }
        if (data?.contains(STATISTICS_CLICKED) == true && chatId != null) {
            val statistics = trainer.getStatistics()
            telegramBot.sendMessage(
                telegramBot.botToken,
                chatId,
                "Ваша статистика: выучено ${statistics.numberOfLearnedWords} из ${statistics.numberOfWords} слов |" +
                        " ${statistics.progressInPercent} %.",
            )
        }
        if (data?.contains(LEARN_WORDS_CLICKED) == true && chatId != null) {
            telegramBot.checkNextQuestionAndSend(trainer, telegramBot.botToken, chatId)
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true && chatId != null) {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswerIndex)) {
                telegramBot.sendMessage(telegramBot.botToken, chatId, "Правильно!")
            } else {
                telegramBot.sendMessage(
                    telegramBot.botToken,
                    chatId,
                    "Не правильно:\n${trainer.question?.wordForLearning?.englishWord} - " +
                            "${trainer.question?.wordForLearning?.translation}.",
                )
            }
            telegramBot.checkNextQuestionAndSend(trainer, telegramBot.botToken, chatId)
        }
    }
}