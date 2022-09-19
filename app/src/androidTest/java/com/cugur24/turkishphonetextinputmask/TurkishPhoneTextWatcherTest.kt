package com.cugur24.turkishphonetextinputmask

import android.util.Log
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cugur24.turkishphonetextinputmask.testutils.TypeTextToSelection
import com.cugur24.turkishphonetextinputmask.testutils.WithSelectionMatcher
import org.hamcrest.Matcher
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.hamcrest.CoreMatchers.`is` as Is


typealias PTE<T, K> = ParamToExpected<T, K>

class ParamToExpected<T, K>(
    val param: T,
    val expected: K,
    val previousState: T? = null,
    val changingStartingIndex: Int = -1,
    val actionType: ActionType = ActionType.Adding(),
    val expectingCursorPoint: Int = -1
)

sealed class ActionType(open val count: Int = 0) {
    class Adding(override var count: Int = 0) : ActionType(count)
    class Deleting(override val count: Int = 0) : ActionType(count)
}

@RunWith(Suite::class)
@Suite.SuiteClasses(
    MaskGenerationTests::class,
    TypeTextToSelectionTests::class,
    CursorSetTests::class
)
class AllTests

@RunWith(AndroidJUnit4::class)
class TypeTextToSelectionTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private lateinit var view:ViewInteraction
    @Before
    fun setUp(){
        view = onView(withId(R.id.et_testPhoneNumber))
    }
    @Test
    fun addingToBoundIndexTest() {
        view.let {
            it.perform(typeText("43422"))
            it.perform(typeToSelection("55", 9)).check(matches(withText("4342255")))
        }
    }

    @Test
    fun addingToMiddleIndexTest() {
        view.let {
            it.perform(typeText("43422"))
            it.perform(typeToSelection("99", 2)).check(matches(withText("4399422")))
        }
    }

    @Test
    fun addingToStartIndex() {
        view.let {
            it.perform(typeText("43422"))
            it.perform(typeToSelection("33", 0)).check(matches(withText("3343422")))
        }
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun addingToNegativeIndex() {
        view.let {
            it.perform(typeText("43422"))
            it.perform(typeToSelection("33", -1))
        }
    }

    @Test
    fun deletingTest() {
        view.let {
            it.perform(replaceText("123456789"))
            it.perform(typeToSelection("", 4, ActionType.Deleting(3)))
                .check(matches(withText("156789")))
            it.perform(replaceText("123456789"))
            it.perform(typeToSelection("", 8, ActionType.Deleting(5)))
                .check(matches(withText("1239")))
            it.perform(replaceText("123456789"))
            it.perform(typeToSelection("", 3, ActionType.Deleting(1)))
                .check(matches(withText("12456789")))
            it.perform(replaceText("123456789"))
            it.perform(typeToSelection("", 1, ActionType.Deleting(1)))
                .check(matches(withText("23456789")))
        }
    }
}

@RunWith(AndroidJUnit4::class)
class MaskGenerationTests {
    private lateinit var currentActivity: MainActivity

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            currentActivity = activity
        }
    }

    @Test
    fun unwantedStateHandlingTest() {
        val testCases = listOf(
            PTE("5", "(5__) ___ __ __", ""),
            PTE("", "(5__) ___ __ __", ""),
            PTE("4", "(5__) ___ __ __", "(5__) ___ __ __", 0)
        )
        runTest(testCases)
    }

    @Test
    fun returnPrevPhoneStatesTest() {
        val testCases = listOf(
            //Testing (35__) ___ __ __ case
            PTE("3", "(5__) ___ __ __", "(5__) ___ __ __", 1),
            PTE("5__) ___ __ __", "(5__) ___ __ __", "(5__) ___ __ __"),
            //Testing (3554) ___ __ __ case
            PTE("3", "(554) ___ __ __", "(554) ___ __ __", 1)
        )
        runTest(testCases)
    }

    @Test
    fun deletingBlankTest() {
        val testCases = listOf(
            PTE("(543)303 __ __", "(543) 303 __ __", "(543) 303 __ __"),
            PTE("(554) 3011_ __", "(554) 301 1_ __", "(554) 301 1_ __"),
            PTE("(543) 303 45__", "(543) 303 45 __", "(543) 303 45 __"),
            PTE("(543) 303 656_", "(543) 303 65 6_", "(543) 303 65 6_")
        )
        runTest(testCases)
    }

    @Test
    fun deletingParenthesesTest() {
        val testCases = listOf(
            PTE("(544 303 4_ __", "(544) 303 4_ __", "(544) 303 4_ __")
        )
        runTest(testCases)

    }

    @Test
    fun deletingDigitTest() {
        val testCases = listOf(
            PTE(
                "(5_) ___ __ __",
                "(5__) ___ __ __",
                "(53_) ___ __ __",
                3,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(53) ___ __ __",
                "(53_) ___ __ __",
                "(534) ___ __ __",
                4,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(534) __ __ __",
                "(534) ___ __ __",
                "(534) 9__ __ __",
                7,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(534) 4_ __ __",
                "(534) 4__ __ __",
                "(534) 49_ __ __",
                8,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(534) 43 __ __",
                "(534) 43_ __ __",
                "(534) 439 __ __",
                9,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(534) 434 _ __",
                "(534) 434 __ __",
                "(534) 434 9_ __",
                11,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(534) 434 5 __",
                "(534) 434 5_ __",
                "(534) 434 59 __",
                12,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(534) 434 54 _",
                "(534) 434 54 __",
                "(534) 434 54 9_",
                14,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(534) 434 54 9",
                "(534) 434 54 9_",
                "(534) 434 54 99",
                15,
                actionType = ActionType.Deleting(1)
            ),
        )
        runTest(testCases)
    }

    @Test
    fun deletingSelectionTest() {
        val testCases = listOf(
            PTE("(534) 434", "(534) 43_ __ __", "(534) 434 __ __", 14, ActionType.Deleting(6)),
            PTE("(5", "(5__) ___ __ __"),
            PTE("(534)", "(534) ___ __ __"),
            PTE("(534) 434 43", "(534) 434 43 __"),
            PTE("(", "(5__) ___ __ __")
        )
        runTest(testCases)
    }

    @Test
    fun addingDigitTest() {
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
        runTest(testCases)
    }

    @Test
    fun reachingMaxSizeTest() {
        val testCases = listOf(
            PTE("(534) 594 53 564", "(534) 594 53 56", "(534) 594 53 56")
        )
        runTest(testCases)
    }

    private fun runTest(testCases: List<ParamToExpected<String, String>>) {
        testCases.forEach { case ->
            verboseTest(case)
            onView(withId(R.id.et_defaultPhoneNumber)).also {
                if (case.previousState != null)
                    it.perform(replaceText(case.previousState))
                if (case.changingStartingIndex != -1)
                    it.perform(
                        typeToSelection(
                            case.param,
                            case.changingStartingIndex,
                            case.actionType
                        )
                    )
                else it.perform(replaceText(case.param))
                it.check(matches(withText(case.expected)))
            }
            verboseResult(case)
        }
    }

    private fun verboseTest(case: ParamToExpected<String, String>) {
        Log.v("UITEST", "Test param for: -> |${case.param}| <- expecting -> |${case.expected}| <-")
    }

    private fun verboseResult(case: ParamToExpected<String, String>) {
        Log.v("UITEST", "Test successfully for -> |${case.param}| <-")
    }
}

@RunWith(AndroidJUnit4::class)
class CursorSetTests {
    private lateinit var currentActivity: MainActivity

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            currentActivity = activity
        }
    }

    @Test
    fun setCursorOnDeleteDigitTest() {
        val testCases = listOf(
            PTE(
                "(5_) ___ __ __",
                "(5__) ___ __ __",
                "(53_) ___ __ __",
                3,
                actionType = ActionType.Deleting(1),
                2
            ),
            PTE(
                "(53) ___ __ __",
                "(53_) ___ __ __",
                "(534) ___ __ __",
                4,
                actionType = ActionType.Deleting(1),
                3
            ),
        )
        runTest(testCases)
    }

    @Test
    fun setCursorOnAddingTest() {
        val testCases = listOf(
            PTE("(53__) ___ __ __", "(53_) ___ __ __", expectingCursorPoint = 3),
            PTE("(534_) ___ __ __", "(534) ___ __ __", expectingCursorPoint = 4),
            PTE("(534) 5___ __ __", "(534) 5__ __ __", expectingCursorPoint = 7),
            PTE("(534) 59__ __ __", "(534) 59_ __ __", expectingCursorPoint = 8),
            PTE("(534) 594_ __ __", "(534) 594 __ __", expectingCursorPoint = 9),
        )
        runTest(testCases)
    }

    private fun runTest(testCases: List<ParamToExpected<String, String>>) {
        testCases.forEach { case ->
            onView(withId(R.id.et_defaultPhoneNumber)).let {
                if (case.expectingCursorPoint == -1) throw IllegalStateException("expectingCursorPoint must not be -1 in ${this.javaClass.simpleName}")
                if (case.previousState != null)
                    it.perform(replaceText(case.previousState))
                if (case.changingStartingIndex != -1)
                    it.perform(
                        typeToSelection(
                            case.param,
                            case.changingStartingIndex,
                            case.actionType
                        )
                    )
                else it.perform(replaceText(case.param))
                it.check(matches(withSelection(case.expectingCursorPoint)))
            }
        }
    }
}

private fun withSelection(selection: Int): Matcher<View> {
    return WithSelectionMatcher(Is(selection))
}

private fun typeToSelection(
    stringToBeTyped: String,
    selection: Int,
    actionType: ActionType = ActionType.Adding()
): TypeTextToSelection {
    return TypeTextToSelection(stringToBeTyped, selection, actionType)
}