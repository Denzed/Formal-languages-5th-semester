
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.assertEquals

class MainTest {
    private val errContent = ByteArrayOutputStream()
    private val outContent = ByteArrayOutputStream()

    @Before
    fun setUpStreams() {
        System.setErr(PrintStream(errContent))
//        System.setOut(PrintStream(errContent))
    }

    @After
    fun cleanUpStreams() {
        assertEquals(0, errContent.size(), errContent.toString())
        errContent.reset()
        assertEquals(0, outContent.size(), outContent.toString())
        outContent.reset()
        System.setErr(null)
//        System.setOut(null)
    }

    @Test
    fun testMain() {
        val inputFile = javaClass.classLoader.getResource("L.test")!!.file
        val answerFile = javaClass.classLoader.getResource("L.test.answer")!!.file
        val expectedResult = File(answerFile).readText()

        main(arrayOf("ast", inputFile))
        val actualResult = File("$inputFile.out").readText()
        assertEquals(expectedResult, actualResult)
    }
}