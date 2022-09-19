package com.cugur24.turkishphonetextinputmask.testutils

import android.view.View
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedDiagnosingMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

class WithSelectionMatcher(private val integerMatcher: Matcher<Int>) :
    BoundedDiagnosingMatcher<View, TextView>(TextView::class.java) {

    override fun matchesSafely(item: TextView?, mismatchDescription: Description?): Boolean {
        val selectionEnd = item?.selectionEnd
        selectionEnd?:return false
        return integerMatcher.matches(selectionEnd)
    }

    override fun describeMoreTo(description: Description?) {
        description?.appendText("view.selectionEnd matching: ")?:return
        integerMatcher.describeTo(description)
    }
}