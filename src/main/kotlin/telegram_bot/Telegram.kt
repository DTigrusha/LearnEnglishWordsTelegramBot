package telegram_bot

fun main(args: Array<String>) {

    val telegramBot = TelegramBotService()
    val botToken: String = args[0]
    var updateId = 0
    var chatId: Int

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBot.getUpdates(botToken, updateId)
        println(updates)

        val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
        val matchResult: MatchResult? = updateIdRegex.find(updates)
        val groups: MatchGroupCollection? = matchResult?.groups
        val updateIdString: String? = groups?.get(1)?.value
        updateId = updateIdString?.toInt()?.plus(1) ?: 0

        val chatIdRegex: Regex = "\"id\":(.+?),".toRegex()
        val matchResultChatId: MatchResult? = chatIdRegex.find(updates)
        val groupsChat: MatchGroupCollection? = matchResultChatId?.groups
        val chatIdString: String? = groupsChat?.get(1)?.value
        chatId = chatIdString?.toInt() ?: 0

        val chatTextRegex: Regex = "\"text\":(.+?)}".toRegex()
        val matchResultChatText: MatchResult? = chatTextRegex.find(updates)
        val groupsChatText: MatchGroupCollection? = matchResultChatText?.groups
        val chatText: String? = groupsChatText?.get(1)?.value

        if (chatText?.contains("Hello", true) == true) {
            telegramBot.sendMessage(botToken, chatId, messageText = "Hello")
        }
    }
}

