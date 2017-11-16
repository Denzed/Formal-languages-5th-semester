
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.assertEquals

class MainTest {
    private val errContent = ByteArrayOutputStream()

    @Before
    fun setUpStreams() {
        System.setErr(PrintStream(errContent))
    }

    @After
    fun cleanUpStreams() {
        assertEquals(0, errContent.size(), errContent.toString())
        errContent.reset()
        System.setErr(null)
    }

    @Test
    fun testMain() {
        val inputFile = javaClass.classLoader.getResource("L.test")!!.file
        val answerFile = javaClass.classLoader.getResource("L.test.answer")!!.file

        val expectedResult = File(answerFile).bufferedReader().use { it.readText() }

        main(arrayOf("ast", inputFile))
        val actualResult = File("$inputFile.out").bufferedReader().use { it.readText() }
        assertEquals(expectedResult, actualResult)
    }
}