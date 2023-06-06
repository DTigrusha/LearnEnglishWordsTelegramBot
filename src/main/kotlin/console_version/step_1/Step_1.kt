package console_version.step_1

import java.io.File

fun main() {

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()

    val listOfWords = wordsFile.readLines()

    for (line in listOfWords) {
        println(line)
    }
}