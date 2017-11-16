
import parser.astFromCode
import parser.tokensFromCode
import java.io.File

fun printHelp(message: String = "") {
    if (message.isNotEmpty()) {
        println(message)
    }
    println(
            buildString {
                appendln("Usage: command inputFile [outputFile={inputFile}.out]")
                appendln("Commands: ")
                appendln("    lex -- splits given code to tokens")
                appendln("    ast -- builds AST from given code")
                appendln("    help -- outputs this text")
            }
    )
}

fun main(args: Array<String>) {
    if (args.size < 2) {
        printHelp("Too few arguments supplied")
        return
    }
    val inputFile = File(args[1])
    if (!inputFile.isFile) {
        println("No such file")
        return
    }
    if (!inputFile.canRead()) {
        println("Cannot read file")
        return
    }
    val code = inputFile.inputStream().bufferedReader().use { it.readText() }
    val output = try {
        when (args[0]) {
            "lex" -> tokensFromCode(code).joinToString()
            "ast" -> astFromCode(code).toPlantUML()
            else -> {
                printHelp("Invalid command \"${args[0]}\"")
                return
            }
        }
    } catch (t: Throwable) {
        println(t.message ?:
                "An unknown error occurred. Here is the stack trace just in case:\n" +
                        "${t.stackTrace}")
        return
    }
    val outputFile = File(if (args.size > 2) args[2] else "$inputFile.out")
    outputFile.writeText(output)
}