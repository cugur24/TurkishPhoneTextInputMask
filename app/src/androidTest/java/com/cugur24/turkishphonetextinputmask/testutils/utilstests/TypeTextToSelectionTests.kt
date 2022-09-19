package com.cugur24.turkishphonetextinputmask.testutils.utilstests

import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cugur24.turkishphonetextinputmask.ActionType
import com.cugur24.turkishphonetextinputmask.MainActivity
import com.cugur24.turkishphonetextinputmask.R
import com.cugur24.turkishphonetextinputmask.testutils.TypeTextToSelection
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TypeTextToSelectionTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private lateinit var view: ViewInteraction
    @Before
    fun setUp(){
        view = Espresso.onView(ViewMatchers.withId(R.id.et_testPhoneNumber))
    }
    @Test
    fun addingToBoundIndexTest() {
        view.let {
            it.perform(ViewActions.typeText("43422"))
            it.perform(TypeTextToSelection.typeToSelection("55", 9)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText("4342255")
                )
            )
        }
    }

    @Test
    fun addingToMiddleIndexTest() {
        view.let {
            it.perform(ViewActions.typeText("43422"))
            it.perform(TypeTextToSelection.typeToSelection("99", 2)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText("4399422")
                )
            )
        }
    }

    @Test
    fun addingToStartIndex() {
        view.let {
            it.perform(ViewActions.typeText("43422"))
            it.perform(TypeTextToSelection.typeToSelection("33", 0)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText("3343422")
                )
            )
        }
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun addingToNegativeIndex() {
        view.let {
            it.perform(ViewActions.typeText("43422"))
            it.perform(TypeTextToSelection.typeToSelection("33", -1))
        }
    }

    @Test
    fun deletingTest() {
        view.let {
            it.perform(ViewActions.replaceText("123456789"))
            it.perform(TypeTextToSelection.typeToSelection("", 4, ActionType.Deleting(3)))
                .check(ViewAssertions.matches(ViewMatchers.withText("156789")))
            it.perform(ViewActions.replaceText("123456789"))
            it.perform(TypeTextToSelection.typeToSelection("", 8, ActionType.Deleting(5)))
                .check(ViewAssertions.matches(ViewMatchers.withText("1239")))
            it.perform(ViewActions.replaceText("123456789"))
            it.perform(TypeTextToSelection.typeToSelection("", 3, ActionType.Deleting(1)))
                .check(ViewAssertions.matches(ViewMatchers.withText("12456789")))
            it.perform(ViewActions.replaceText("123456789"))
            it.perform(TypeTextToSelection.typeToSelection("", 1, ActionType.Deleting(1)))
                .check(ViewAssertions.matches(ViewMatchers.withText("23456789")))
        }
    }
}
