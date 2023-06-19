package telegram_bot

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(private val botToken: String) {

    companion object {
        const val BOT_URL = "https://api.telegram.org/bot"
    }

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$BOT_URL$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Int, messageText: String): String {
        val encoded = URLEncoder.encode(
            messageText,
            StandardCharsets.UTF_8
        )
        println(encoded)

        val urlSendMessage = "$BOT_URL$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Int): String {
        val urlSendMessage = "$BOT_URL$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "$LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestion(botToken: String, chatId: Int, question: Question?): String {
        val urlSendMessage = "$BOT_URL$botToken/sendMessage"
        val correctIndexOfAnswer = question?.listOfAnswers?.mapIndexed { index, _ ->
            ("$CALLBACK_DATA_ANSWER_PREFIX${index + 1}")
        }
        val sendQuestionBody = """
            {
                "chat_id": $chatId,
                "text": "${question?.wordForLearning?.englishWord}",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "${question?.listOfAnswers?.get(0)?.translation}",
                                "callback_data": "${correctIndexOfAnswer?.get(0)}"
                            },
                            {
                                "text": "${question?.listOfAnswers?.get(1)?.translation}",
                                "callback_data": "${correctIndexOfAnswer?.get(1)}"
                            }
                        ],
                        [
                        {
                                "text": "${question?.listOfAnswers?.get(2)?.translation}",
                                "callback_data": "${correctIndexOfAnswer?.get(2)}"
                            },
                            {
                                "text": "${question?.listOfAnswers?.get(3)?.translation}",
                                "callback_data": "${correctIndexOfAnswer?.get(3)}"
                            }
                        ]
                    ]
                }
            }
            """.trimIndent()

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Int) {
        if (trainer.getNextQuestion() == null) {
            sendMessage(chatId, "Вы выучили все слова в базе!")
        } else {
            sendQuestion(botToken, chatId, trainer.getNextQuestion())
        }
    }
}