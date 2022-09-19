package com.cugur24.turkishphonetextinputmask.testutils

import android.view.View
import android.widget.EditText
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.cugur24.turkishphonetextinputmask.ActionType
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf
import java.lang.IllegalStateException
import java.lang.IndexOutOfBoundsException

/**
 * This class can be use for inserting text to given selection index for [EditText]}.
 */
class TypeTextToSelection(
    private val stringToBeTyped: String,
    private val selection: Int,
    private val actionType: ActionType
) : ViewAction {
    companion object{
        fun typeToSelection(
            stringToBeTyped: String,
            selection: Int,
            actionType: ActionType = ActionType.Adding()
        ): TypeTextToSelection {
            return TypeTextToSelection(stringToBeTyped, selection, actionType)
        }
    }
    override fun getConstraints(): Matcher<View> {
        return AllOf.allOf(
            ViewMatchers.isDisplayed(),
            ViewMatchers.isAssignableFrom(EditText::class.java)
        )
    }

    override fun getDescription(): String {
        return "type $stringToBeTyped to $selection index"
    }

    override fun perform(uiController: UiController?, view: View?) {
        if (view !is EditText) throw IllegalStateException("View element must be Edittext for selection op.")
        if (selection < 0) throw IndexOutOfBoundsException("Index cannot be lower 0. Your index was $selection")
        when (actionType) {
            is ActionType.Adding -> {
                val text = StringBuilder(view.text.toString())
                try {
                    text.substring(0, selection).also {
                        val addedSub = StringBuilder(it).append(stringToBeTyped)
                        val frontPart = text.substring(selection, text.length)
                        if (frontPart.isNotEmpty()) addedSub.append(frontPart)
                        view.setText(addedSub)
                    }
                } catch (e: IndexOutOfBoundsException) {
                    text.append(stringToBeTyped).also { view.setText(it) }
                }
            }
            is ActionType.Deleting -> {
                if (selection-actionType.count<0) throw IllegalStateException("Deleting part size slower than deleting count. Size was deleting count: $selection deleting count was ${actionType.count}")
                view.text.delete(selection - actionType.count, selection)
            }
        }

    }
}

