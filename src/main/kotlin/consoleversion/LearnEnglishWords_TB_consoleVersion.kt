package consoleversion

import LearnWordsTrainer
import Question
import Word

fun Question.asConsoleString(): String {
    val listOfAnswers = this.listOfAnswers
        .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translation}" }
        .joinToString(separator = "\n")
    return "Выберете перевод для слова " + this.wordForLearning.englishWord.uppercase() +
            " из предложенных ниже вариантов и введите цифру, соответствующую Вашему ответу:" +
            "\n" + listOfAnswers + "\n0 - выйти в меню"
}

fun main() {

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь.")
        return
    }

    println(
        "Вас приветствует Тренажер - бот!\nЯ помогу Вам в изучении английских слов.\nДля навигации по меню введите" +
                " соответствующую цифру."
    )

    while (true) {
        println("\nМеню:\n1 - Учить слова.\n2 - Статистика.\n0 - Выход.")
        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Все слова выучены!")
                        return
                    } else {
                        println(question.asConsoleString())
                    }

                    val userAnswer = readln().toIntOrNull()
                    if (userAnswer == 0) break

                    if (trainer.checkAnswer(userAnswer)) {
                        println("Правильно!\n")
                    } else {
                        println(
                            "Неправильно: слово \"${question.wordForLearning.englishWord}\" " +
                                    "- перевод \"${question.wordForLearning.translation}\".\n"
                        )
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println(
                    "Ваша статистика: выучено ${statistics.numberOfLearnedWords} из ${statistics.numberOfWords} слов |" +
                            " ${statistics.progressInPercent} %.\nДля продолжения введите соответствующую цифру меню."
                )
            }

            0 -> break
            else -> println("Вы ввели не ту цифру. Пробуйте снова!")
        }
    }
}