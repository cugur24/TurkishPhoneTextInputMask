package com.cugur24.turkishphonetextinputmask

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cugur24.turkishphonetextinputmask.testutils.TypeTextToSelection.Companion.typeToSelection
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
            PTE("3", "(594) ___ __ __", "(594) ___ __ __", 1)
        )
        runTest(testCases)
    }

    @Test
    fun deletingBlankTest() {
        val testCases = listOf(
            PTE("(593)303 __ __", "(593) 303 __ __", "(593) 303 __ __"),
            PTE("(594) 3011_ __", "(594) 301 1_ __", "(594) 301 1_ __"),
            PTE("(593) 303 45__", "(593) 303 45 __", "(593) 303 45 __"),
            PTE("(593) 303 656_", "(593) 303 65 6_", "(593) 303 65 6_")
        )
        runTest(testCases)
    }

    @Test
    fun deletingParenthesesTest() {
        val testCases = listOf(
            PTE("(594 303 4_ __", "(594) 303 4_ __", "(594) 303 4_ __")
        )
        runTest(testCases)

    }

    @Test
    fun deletingDigitTest() {
        val testCases = listOf(
            PTE(
                "(5_) ___ __ __",
                "(5__) ___ __ __",
                "(59_) ___ __ __",
                3,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(59) ___ __ __",
                "(59_) ___ __ __",
                "(594) ___ __ __",
                4,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(594) __ __ __",
                "(594) ___ __ __",
                "(594) 9__ __ __",
                7,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(594) 4_ __ __",
                "(594) 4__ __ __",
                "(594) 49_ __ __",
                8,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(594) 43 __ __",
                "(594) 43_ __ __",
                "(594) 439 __ __",
                9,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(594) 434 _ __",
                "(594) 434 __ __",
                "(594) 434 9_ __",
                11,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(594) 434 5 __",
                "(594) 434 5_ __",
                "(594) 434 59 __",
                12,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(594) 434 54 _",
                "(594) 434 54 __",
                "(594) 434 54 9_",
                14,
                actionType = ActionType.Deleting(1)
            ),
            PTE(
                "(594) 434 54 9",
                "(594) 434 54 9_",
                "(594) 434 54 99",
                15,
                actionType = ActionType.Deleting(1)
            ),
        )
        runTest(testCases)
    }

    @Test
    fun deletingSelectionTest() {
        val testCases = listOf(
            PTE("(594) 434", "(594) 43_ __ __", "(594) 434 __ __", 14, ActionType.Deleting(6)),
            PTE("(5", "(5__) ___ __ __"),
            PTE("(594)", "(594) ___ __ __"),
            PTE("(594) 434 43", "(594) 434 43 __"),
            PTE("(", "(5__) ___ __ __")
        )
        runTest(testCases)
    }

    @Test
    fun addingDigitTest() {
        val testCases = listOf(
            PTE("(59__) ___ __ __", "(59_) ___ __ __"),
            PTE("(594_) ___ __ __", "(594) ___ __ __"),
            PTE("(594) 5___ __ __", "(594) 5__ __ __"),
            PTE("(594) 59__ __ __", "(594) 59_ __ __"),
            PTE("(594) 594_ __ __", "(594) 594 __ __"),
            PTE("(594) 594_ __ __", "(594) 594 __ __"),
            PTE("(594) 594 5__ __", "(594) 594 5_ __"),
            PTE("(594) 594 53_ __", "(594) 594 53 __"),
            PTE("(594) 594 53 5__", "(594) 594 53 5_"),
            PTE("(594) 594 53 56_", "(594) 594 53 56")
        )
        runTest(testCases)
    }

    @Test
    fun reachingMaxSizeTest() {
        val testCases = listOf(
            PTE("(594) 594 53 564", "(594) 594 53 56", "(594) 594 53 56")
        )
        runTest(testCases)
    }

    private fun runTest(testCases: List<ParamToExpected<String, String>>) {
        testCases.forEach { case ->
            verboseTest(case)
            Espresso.onView(ViewMatchers.withId(R.id.et_defaultPhoneNumber)).also {
                if (case.previousState != null)
                    it.perform(ViewActions.replaceText(case.previousState))
                if (case.changingStartingIndex != -1)
                    it.perform(
                        typeToSelection(
                            case.param,
                            case.changingStartingIndex,
                            case.actionType
                        )
                    )
                else it.perform(ViewActions.replaceText(case.param))
                it.check(ViewAssertions.matches(ViewMatchers.withText(case.expected)))
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