import java.io.File
import java.io.PrintWriter

data class Word(
    val englishWord: String,
    val translation: String,
    var correctAnswerCount: Int = 0,
)

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
    private val fileName: String = "words.txt",
    private val maxCorrectAnswer: Int = 3,
    private val variantsOfAnswer: Int = 4,
) {

    var question: Question? = null
    private val dictionary = loadDictionary(File(fileName))

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
            val correctAnswerId = it.listOfAnswers.indexOf(it.wordForLearning) + 1
            if (correctAnswerId == userAnswerIndex) {
                it.wordForLearning.correctAnswerCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(file: File): MutableList<Word> {
        try {
            if (!file.exists()) {
                File("words.txt").copyTo(file)
            }
            val dictionary = mutableListOf<Word>()
            file.readLines().forEach {
                val line = it.split("|")
                if (line.size == 3) {
                    dictionary.add(Word(line[0], line[1], line[2].toIntOrNull() ?: 0))
                }
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Невозможно загрузить словарь.")
        }
    }

    private fun saveDictionary() {
        val wordsFile = File(fileName)
        val writer = PrintWriter(wordsFile)

        dictionary.forEach {
            val lineOfChangedWords =
                listOf(it.englishWord, it.translation, it.correctAnswerCount.toString()).joinToString("|")
            writer.appendLine(lineOfChangedWords)
        }
        writer.close()
    }

    private fun checkDictionaries(firstDictionary: MutableList<Word>, secondDictionary: MutableList<Word>): MutableList<Word> {
        for (word in firstDictionary) {
            val userDictionaryForCompare = secondDictionary.map { it.englishWord }
            if (!userDictionaryForCompare.contains(word.englishWord)) {
                secondDictionary.add(word)
            }
            saveDictionary()
        }
        return secondDictionary
    }

    fun checkAndUpdateUserWordsFile() {
        val userWordsFile = File(fileName)
        val generalWordsFile = File("words.txt")
        if (generalWordsFile.length() != userWordsFile.length()) {
            checkDictionaries(loadDictionary(generalWordsFile), dictionary)
        }
    }

    fun addUserWordsFile() {
        val userWordsFile = File(fileName)
        val additionalUserWordsFile = File("${userWordsFile.nameWithoutExtension}_userWords.txt")
        checkDictionaries(loadDictionary(additionalUserWordsFile), dictionary)
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswerCount = 0 }
        saveDictionary()
    }
}