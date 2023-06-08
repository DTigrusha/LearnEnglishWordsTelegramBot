package console_version

import java.io.File

data class Word(
    val englishWord: String,
    val translation: String,
    var correctAnswerCount: Int = 0,
)

fun main() {

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()

    val dictionary: MutableList<Word> = mutableListOf()

    val listOfWords = wordsFile.readLines()

    for (line in listOfWords) {
        val line = line.split("|")
        val word = Word(
            englishWord = line[0],
            translation = line[1],
            correctAnswerCount = line[2].toIntOrNull() ?: 0,
        )
        dictionary.add(word)
    }

    println(
        "Вас приветствует Тренажер - бот!\nЯ помогу Вам в изучении английских слов.\nДля навигации по меню введите" +
                " соответствующую цифру."
    )

    startMenu()

    while (true) {
        when (readln()) {
            "1" -> {
                while (true) {
                    val unlearnedWords = mutableListOf<Word>()
                    for (word in dictionary) {
                        if (word.correctAnswerCount < 3) {
                            unlearnedWords.add(word)
                        }
                    }

                    if (unlearnedWords.isEmpty()) {
                        println("Все слова выучены!")
                        return
                    } else {
                        val wordForLearning = unlearnedWords.random()
                        println(
                            "Выберете перевода для слова \"${(wordForLearning.englishWord).uppercase()}\" из " +
                                    "предложенных ниже вариантов и введите цифру, соответствующую Вашему ответу:"
                        )

                        val listOfAnswers = unlearnedWords.take(4).shuffled()
                        var number = 1
                        listOfAnswers.forEach {
                            println("${number++} - ${it.translation}")
                        }

                        println("0 - для возврата в главное меню")
                    }

                    val userAnswer = readln().toIntOrNull()
                    if (userAnswer == 0)
                        break
                }
                startMenu()
            }

            "2" -> {
                val numberOfWords = dictionary.size
                val numberOfLearnedWords = (dictionary.filter { word: Word -> word.correctAnswerCount >= 3 }).size
                val progressInPercent = (numberOfLearnedWords * 100 / numberOfWords)
                println(
                    "Ваша статистика: выучено $numberOfLearnedWords из $numberOfWords слов | $progressInPercent %.\n" +
                            "Для продолжения введите соответствующую цифру меню.")
            }

            "0" -> break
            else -> println("Вы ввели не ту цифру. Пробуйте снова!")
        }
    }
}

fun startMenu() {
    println("\nМеню:\n1 - Учить слова.\n2 - Статистика.\n0 - Выход.")
}

