package com.cugur24.turkishphonetextinputmask.testutils.utilstests

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cugur24.turkishphonetextinputmask.*
import com.cugur24.turkishphonetextinputmask.testutils.TypeTextToSelection
import com.cugur24.turkishphonetextinputmask.testutils.WithSelectionMatcher.Companion.withSelection
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
                "(59_) ___ __ __",
                3,
                actionType = ActionType.Deleting(1),
                2
            ),
            PTE(
                "(59) ___ __ __",
                "(59_) ___ __ __",
                "(594) ___ __ __",
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
            PTE("(59__) ___ __ __", "(59_) ___ __ __", expectingCursorPoint = 3),
            PTE("(594_) ___ __ __", "(594) ___ __ __", expectingCursorPoint = 4),
            PTE("(594) 5___ __ __", "(594) 5__ __ __", expectingCursorPoint = 7),
            PTE("(594) 59__ __ __", "(594) 59_ __ __", expectingCursorPoint = 8),
            PTE("(594) 594_ __ __", "(594) 594 __ __", expectingCursorPoint = 9),
        )
        runTest(testCases)
    }

    private fun runTest(testCases: List<ParamToExpected<String, String>>) {
        testCases.forEach { case ->
            Espresso.onView(ViewMatchers.withId(R.id.et_defaultPhoneNumber)).let {
                if (case.expectingCursorPoint == -1) throw IllegalStateException("expectingCursorPoint must not be -1 in ${this.javaClass.simpleName}")
                if (case.previousState != null)
                    it.perform(ViewActions.replaceText(case.previousState))
                if (case.changingStartingIndex != -1)
                    it.perform(
                        TypeTextToSelection.typeToSelection(
                            case.param,
                            case.changingStartingIndex,
                            case.actionType
                        )
                    )
                else it.perform(ViewActions.replaceText(case.param))
                it.check(ViewAssertions.matches(withSelection(case.expectingCursorPoint)))
            }
        }
    }
}
