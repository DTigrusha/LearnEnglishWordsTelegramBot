import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.math.BigInteger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val RESET_CLICKED = "reset_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val MENU_CLICKED = "menu_clicked"
const val ADD_DICTIONARY_CLICKED = "add_dictionary_clicked"

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: MutableList<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class GetFileRequest(
    @SerialName("file_id")
    val fileId: String,
)

@Serializable
data class GetFileResponse(
    @SerialName("ok")
    val ok: Boolean,
    @SerialName("result")
    val result: TelegramFile? = null,
)

@Serializable
data class TelegramFile(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_unique_id")
    val fileUniqueId: String,
    @SerialName("file_size")
    val fileSize: Long,
    @SerialName("file_path")
    val filePath: String,
)

@Serializable
data class SendPhotoRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("photo")
    val photo: String,
    @SerialName("has_spoiler")
    val hasSpoiler: Boolean,
)

@Serializable
data class GetPhotoResponse(
    @SerialName("ok")
    val ok: Boolean,
    @SerialName("result")
    val result: Photo,
)

@Serializable
data class Photo(
    @SerialName("photo")
    val photo: List<PhotoSize>,
)

@Serializable
data class PhotoSize(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_unique_id")
    val fileUniqueId: String,
    @SerialName("file_size")
    val fileSize: Int,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int,
)

class TelegramBotService(private val botToken: String) {

    companion object {
        const val BOT_URL = "https://api.telegram.org/bot"
        const val BOT_FILE_URL = "https://api.telegram.org/file/bot"
    }

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$BOT_URL$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, message: String, json: Json): String {
        val urlSendMessage = "$BOT_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
        return response.body()
    }

    fun sendMenu(chatId: Long, json: Json): String {
        val urlSendMessage = "$BOT_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                mutableListOf(
                    listOf(
                        InlineKeyboard(text = "Изучить слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                    ),
                    listOf(
                        InlineKeyboard(text = "Добавить слова в словарь", callbackData = ADD_DICTIONARY_CLICKED),
                    ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICKED),
                    ),
                ),
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun getFile(fileId: String, json: Json): String {
        val urlGetFile = "$BOT_URL$botToken/getFile"
        val requestBody = GetFileRequest(
            fileId = fileId,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetFile))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
        return response.body()
    }

    fun downloadFile(filePath: String, fileName: String) {
        val urlGetFile = "$BOT_FILE_URL$botToken/$filePath"
        println(urlGetFile)
        val request: HttpRequest = HttpRequest
            .newBuilder()
            .uri(URI.create(urlGetFile))
            .GET()
            .build()
        val response: HttpResponse<InputStream> = HttpClient
            .newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofInputStream())
        println("status code: " + response.statusCode())

        val body: InputStream = response.body()
        body.copyTo(File(fileName).outputStream(), 16 * 1024)
    }

    private fun sendPhotoByFileId(chatId: Long, json: Json, fileId: String, hasSpoiler: Boolean): String {
        val urlSendMessage = "$BOT_URL$botToken/sendPhoto"
        val requestBody = SendPhotoRequest(
            chatId,
            photo = fileId,
            hasSpoiler = hasSpoiler,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
        return response.body()
    }

    private fun sendPhotoByMultipart(file: File, chatId: Long, hasSpoiler: Boolean): String {
        val data: MutableMap<String, Any> = LinkedHashMap()
        data["chat_id"] = chatId.toString()
        data["photo"] = file
        data["has_spoiler"] = hasSpoiler
        val boundary: String = BigInteger(35, Random()).toString()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$BOT_URL$botToken/sendPhoto"))
            .postMultipartFormData(boundary, data)
            .build()
        val client: HttpClient = HttpClient.newBuilder().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
        return response.body()
    }

    private fun HttpRequest.Builder.postMultipartFormData(
        boundary: String,
        data: Map<String, Any>,
    ): HttpRequest.Builder {
        val byteArrays = ArrayList<ByteArray>()
        val separator = "--$boundary\r\nContent-Disposition: form-data; name=".toByteArray(StandardCharsets.UTF_8)
        for (entry in data.entries) {
            byteArrays.add(separator)
            when (entry.value) {
                is File -> {
                    val file = entry.value as File
                    val path = Path.of(file.toURI())
                    val mimeType = Files.probeContentType(path)
                    byteArrays.add(
                        "\"${entry.key}\"; filename=\"${path.fileName}\"\r\nContent-Type: $mimeType\r\n\r\n".toByteArray(
                            StandardCharsets.UTF_8
                        )
                    )
                    byteArrays.add(Files.readAllBytes(path))
                    byteArrays.add("\r\n".toByteArray(StandardCharsets.UTF_8))
                }

                else -> byteArrays.add("\"${entry.key}\"\r\n\r\n${entry.value}\r\n".toByteArray(StandardCharsets.UTF_8))
            }
        }
        byteArrays.add("--$boundary--".toByteArray(StandardCharsets.UTF_8))
        this.header("Content-Type", "multipart/form-data;boundary=$boundary")
            .POST(HttpRequest.BodyPublishers.ofByteArrays(byteArrays))
        return this
    }

    private fun sendQuestion(botToken: String, chatId: Long, question: Question, json: Json): String {
        val urlSendMessage = "$BOT_URL$botToken/sendMessage"
        val listOfInlineKeyboard: MutableList<List<InlineKeyboard>> = question.listOfAnswers.mapIndexed { index, word ->
            listOf(
                InlineKeyboard(
                    text = word.translation, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX${index + 1}"
                )
            )
        }.toMutableList()
        listOfInlineKeyboard.add(listOf(InlineKeyboard(text = "Вернуться в меню", callbackData = MENU_CLICKED)))

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.wordForLearning.englishWord,
            replyMarkup = ReplyMarkup(
                listOfInlineKeyboard
            )
        )

        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkAndSendPhotoHint(trainer: LearnWordsTrainer, json: Json, chatId: Long) {
        if (!trainer.question?.wordForLearning?.fileId.isNullOrEmpty()) {
            sendPhotoByFileId(chatId, json, trainer.question?.wordForLearning?.fileId.toString(), hasSpoiler = true)
        } else {
            val jsonResponse = sendPhotoByMultipart(
                File(trainer.question?.wordForLearning?.filePath.toString()),
                chatId,
                hasSpoiler = true,
            )
            val response: GetPhotoResponse = json.decodeFromString(jsonResponse)
            val photo = response.result.photo
            trainer.question?.wordForLearning?.fileId = photo[0].fileId
            val changingWord = trainer.dictionary[trainer.dictionary.indexOf(trainer.question?.wordForLearning)]
            changingWord.fileId = photo[0].fileId
            trainer.saveDictionary()
        }
    }

    fun checkNextQuestionAndSend(question: Question?, chatId: Long, json: Json) {
        if (question == null) {
            sendMessage(chatId, "Вы выучили все слова в базе!", json)
        } else {
            sendQuestion(botToken, chatId, question, json)
        }
    }
}