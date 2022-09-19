package com.cugur24.turkish_phone_mask

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import org.junit.runners.Suite
import kotlin.reflect.KFunction

typealias PTE<T, K> = ParamToExpected<T, K>

class ParamToExpected<T, K>(val param: T, val expected: K)

fun verboseTest(a: KFunction<Any>, param: Any): String = "$a.name for -> $param <- "

@RunWith(Suite::class)
@SuiteClasses(MaskGenerationTests::class, CleaningPhoneTests::class)
class AllTests

class MaskGenerationTests {
    private lateinit var tpTextWatcher: TurkishPhoneTextWatcher

    @Before
    fun setUp() {
        tpTextWatcher = TurkishPhoneTextWatcher()

    }

    @Test
    fun `Unwanted states handling test`() {
        val testCases = listOf(
            PTE("5", "(5__) ___ __ __"),
            PTE("", "(5__) ___ __ __"),
            PTE("4(5__) ___ __ __", "(5__) ___ __ __")
        )
        testCases.forEach { case ->
            assertEquals(
                verboseTest(MaskGenerationTests::`Unwanted states handling test`, case.param),
                case.expected,
                tpTextWatcher.generateMask(case.param)
            )
        }
    }

    @Test
    fun `Return prevPhone states test`() {
        val testCases = listOf(
            PTE("(35_) ___ __ __", "(5__) ___ __ __"),
            PTE("5__) ___ __ __", "(5__) ___ __ __"),
            PTE("(3554) ___ __ __", "(554) ___ __ __",)
        )
        testCases.forEach { case ->
            assertEquals(
                verboseTest(MaskGenerationTests::`Return prevPhone states test`, case.param),
                case.expected,
                tpTextWatcher.generateMask(case.param)
            )
        }
    }

    @Test
    fun `Deleting blank test`() {
        val testCases = listOf(
            PTE("(543)303 __ __", "(543) 303 __ __"),
            PTE("(554) 3011_ __", "(554) 301 1_ __"),
            PTE("(543) 303 45__", "(543) 303 45 __"),
            PTE("(543) 303 656_", "(543) 303 65 6_")
        )
        testCases.forEach { case ->
            assertEquals(
                verboseTest(MaskGenerationTests::`Deleting blank test`, case.param),
                case.expected,
                tpTextWatcher.generateMask(case.param)
            )
        }
    }

    @Test
    fun `Deleting parentheses test`() {
        val testCases = listOf(
            PTE("(544 303 4_ __", "(544) 303 4_ __")
        )
        testCases.forEach { case ->
            assertEquals(
                verboseTest(MaskGenerationTests::`Deleting parentheses test`, case.param),
                case.expected,
                tpTextWatcher.generateMask(case.param)
            )
        }
    }

    @Test
    fun `Deleting digit test`() {
        val testCases = listOf(
            PTE("(5_) ___ __ __", "(5__) ___ __ __"),
            PTE("(53) ___ __ __", "(53_) ___ __ __"),
            PTE("(534) __ __ __", "(53_) ___ __ __"),
            PTE("(534) 4_ __ __", "(53_) 4__ __ __"),
            PTE("(534) 43 __ __", "(53_) 43_ __ __"),
            PTE("(534) 434 _ __", "(53_) 434 __ __"),
            PTE("(534) 434 5 __", "(53_) 434 5_ __"),
            PTE("(534) 434 54 _", "(53_) 434 54 __"),
            PTE("(534) 434 54 9", "(53_) 434 54 9_"),
        )
        testCases.forEach { case ->
            assertEquals(
                verboseTest(MaskGenerationTests::`Deleting digit test`, case.param),
                case.expected,
                tpTextWatcher.generateMask(case.param)
            )
        }
    }

    @Test
    fun `Deleting selection test`() {
        val testCases = listOf(
            PTE("(534) 434", "(53_) 434 __ __"),
            PTE("(5", "(5__) ___ __ __"),
            PTE("(534)", "(534) ___ __ __"),
            PTE("(534) 434 43", "(53_) 434 43 __"),
            PTE("(", "(5__) ___ __ __")
        )
        testCases.forEach { case ->
            assertEquals(
                verboseTest(MaskGenerationTests::`Deleting selection test`, case.param),
                case.expected,
                tpTextWatcher.generateMask(case.param)
            )
        }
    }

    @Test
    fun `Adding number test`() {
        val testCases = listOf(
            PTE("(53__) ___ __ __", "(53_) ___ __ __"),
            PTE("(534_) ___ __ __", "(534) ___ __ __"),
            PTE("(534) 5___ __ __", "(534) 5__ __ __"),
            PTE("(534) 59__ __ __", "(534) 59_ __ __"),
            PTE("(534) 594_ __ __", "(534) 594 __ __"),
            PTE("(534) 594_ __ __", "(534) 594 __ __"),
            PTE("(534) 594 5__ __", "(534) 594 5_ __"),
            PTE("(534) 594 53_ __", "(534) 594 53 __"),
            PTE("(534) 594 53 5__", "(534) 594 53 5_"),
            PTE("(534) 594 53 56_", "(534) 594 53 56")
        )
        testCases.forEach { case ->
            assertEquals(
                verboseTest(MaskGenerationTests::`Adding number test`, case.param),
                case.expected,
                tpTextWatcher.generateMask(case.param)
            )
        }
    }

    @Test
    fun `Reaching max size test`() {
        val testCases = listOf(
            PTE("(534) 594 53 564", "(534) 594 53 56")
        )
        testCases.forEach { case ->
            assertEquals(
                verboseTest(MaskGenerationTests::`Reaching max size test`, case.param),
                case.expected,
                tpTextWatcher.generateMask(case.param)
            )
        }
    }


}

class CleaningPhoneTests {
    private lateinit var tpTextWatcher: TurkishPhoneTextWatcher

    @Before
    fun setUp() {
        tpTextWatcher = TurkishPhoneTextWatcher()
    }

    @Test
    fun `Clean phone tests`() {
        val testCases = listOf(
            PTE("543", "543"),
            PTE("()", ""),
            PTE("(674) _", "674"),
            PTE("(53_) ___ __ __", "53"),
            PTE("", "")
        )
        testCases.forEach { case ->
            assertEquals(
                verboseTest(CleaningPhoneTests::`Clean phone tests`, case.param),
                case.expected,
                tpTextWatcher.cleanPhone(case.param)
            )
        }
    }
}