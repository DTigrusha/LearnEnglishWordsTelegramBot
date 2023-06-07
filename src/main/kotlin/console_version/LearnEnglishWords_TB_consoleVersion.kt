package console_version

import java.io.File

data class Word(
    val englishWord: String,
    val translation: String,
    val correctAnswerCount: Int = 0,
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
        "Вас приветствует Тренажер - бот!\nЯ помогу Вам в изучении английских слов.\nДля навигации по меню введите " +
                "соответствующую цифру.\n\nМеню:\n1 - Учить слова.\n2 - Статистика.\n0 - Выход."
    )

    while (true) {
        when (readln()) {
            "1" -> println("Начинаем учить слова.") // TODO реализовать функцию изучения слов!
            "2" -> {
                val numberOfWords = dictionary.size
                val numberOfLearnedWords = (dictionary.filter { word: Word -> word.correctAnswerCount >= 3 }).size
                val progressInPercent = (numberOfLearnedWords * 100 / numberOfWords)
                println("Ваша статистика: выучено $numberOfLearnedWords из $numberOfWords слов | $progressInPercent %.")
            }

            "0" -> break
            else -> println("Вы ввели не ту цифру. Пробуйте снова!")
        }
    }
}