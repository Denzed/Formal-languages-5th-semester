
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import parser.astFromCode
import parser.tokensFromCode
import java.io.ByteArrayOutputStream
import java.io.File



fun printHelp(message: String = "") {
    if (message.isNotEmpty()) {
        println(message)
    }
    println("""
            |Usage: command inputFile [outputFile={inputFile}.out]
            |Commands:
            |    lex -- splits given code to tokens
            |    ast -- builds AST from given code and outputs it as an SVG image
            |       if output file's extension is ".svg" or a PlantUML diagram instead.
            |    help -- outputs this text
            """.trimMargin()
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
    val code = inputFile.readText()
    try {
        val output = when (args[0]) {
            "lex" -> tokensFromCode(code).joinToString()
            "ast" -> astFromCode(code).toPlantUML()
            else -> {
                printHelp("Invalid command \"${args[0]}\"")
                return
            }
        }
        val outputFile = File(if (args.size > 2) args[2] else "$inputFile.out")
        if (args[0] == "ast" && outputFile.extension == "svg") {
            println("Generating image")
            val outputStream = ByteArrayOutputStream()
            // Write the first image to "os"
            val desc = SourceStringReader(output).generateImage(
                    outputStream,
                    FileFormatOption(FileFormat.SVG)
            )
            if (desc != null && desc.isNotEmpty()) {
                println(desc)
            }
            outputStream.close()

            outputFile.writeBytes(outputStream.toByteArray())
        } else {
            outputFile.writeText(output)
        }
    } catch (e: Exception) {
        if (e.message == null) {
            println("An unknown error \"${e.javaClass.name}\" occurred.")
            e.printStackTrace()
        } else {
            println(e.message)
        }
    }
}