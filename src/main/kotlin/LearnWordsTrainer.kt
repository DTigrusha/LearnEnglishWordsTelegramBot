import console_version.Word
import java.io.File
import java.io.PrintWriter

data class Statistics(
    val numberOfWords: Int,
    val numberOfLearnedWords: Int,
    val progressInPercent: Int,
)

data class Question(
    val listOfAnswers: List<Word>,
    val wordForLearning: Word,
)

class LearnWordsTrainer(
    private val maxCorrectAnswer: Int = 3,
    private val variantsOfAnswer: Int = 4,
) {

    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val numberOfWords = dictionary.size
        val numberOfLearnedWords = (dictionary.filter { it.correctAnswerCount >= maxCorrectAnswer }).size
        val progressInPercent = numberOfLearnedWords * 100 / numberOfWords
        return Statistics(numberOfWords, numberOfLearnedWords, progressInPercent)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords = dictionary.filter { it.correctAnswerCount < maxCorrectAnswer }
        if (unlearnedWords.isEmpty()) return null
        val listOfAnswers = if (unlearnedWords.size < variantsOfAnswer) {
            val learnedWords =
                dictionary.filter { it.correctAnswerCount >= maxCorrectAnswer }.shuffled()
            unlearnedWords.shuffled().take(variantsOfAnswer) +
                    learnedWords.take(variantsOfAnswer - unlearnedWords.size)
        } else {
            unlearnedWords.shuffled().take(variantsOfAnswer)
        }.shuffled()

        val wordForLearning = listOfAnswers.random()

        question = Question(
            listOfAnswers = listOfAnswers,
            wordForLearning = wordForLearning,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.listOfAnswers.indexOf(it.wordForLearning)
            if (correctAnswerId == userAnswerIndex) {
                it.wordForLearning.correctAnswerCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): MutableList<Word> {
        val dictionary = mutableListOf<Word>()
        val wordsFile = File("words.txt")
        wordsFile.readLines().forEach {
            val line = it.split("|")
            dictionary.add(Word(line[0], line[1], line[2].toIntOrNull() ?: 0))
        }
        return dictionary
    }

    private fun saveDictionary(dictionary: MutableList<Word>) {
        val wordsFile = File("words.txt")
        val writer = PrintWriter(wordsFile)

        dictionary.forEach {
            val lineOfChangedWords =
                listOf(it.englishWord, it.translation, it.correctAnswerCount.toString()).joinToString("|")
            writer.appendLine(lineOfChangedWords)
        }
        writer.close()
    }
}