package com.cugur24.turkishphonetextinputmask

import com.cugur24.turkishphonetextinputmask.testutils.utilstests.CursorSetTests
import com.cugur24.turkishphonetextinputmask.testutils.utilstests.TypeTextToSelectionTests
import org.junit.runner.RunWith
import org.junit.runners.Suite


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
