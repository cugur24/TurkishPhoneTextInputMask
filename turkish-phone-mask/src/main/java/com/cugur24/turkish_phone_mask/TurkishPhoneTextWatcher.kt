package com.cugur24.turkish_phone_mask

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.IndexOutOfBoundsException


class TurkishPhoneTextWatcher : TextWatcher {

    private var prevPhone: String = ""
        get() = field.ifEmpty { phoneMask }

    /**
     * Gets phone number without mask and blank
     * */
    val phoneNumber: String get() = cleanPhone(prevPhone)
    private var editTextRef: EditText? = null
    private var prefix: Char = '_'
    private lateinit var phoneMask: String
    private val defaultMask = "(5__) ___ __ __"
    private val maxInputLength = 15
    private val maxDigitCount = 10
    private var isUserInput = true
    private var action: Action = Action(ActionTypes.NONE, -1, -1)

    constructor()

    constructor(editTextRef: EditText) {
        this.editTextRef = editTextRef
        phoneMask = defaultMask
    }

    constructor(editTextRef: EditText, prefix: Char) {
        this.editTextRef = editTextRef
        this.prefix = prefix
        phoneMask = if (prefix == '_') {
            defaultMask
        } else {
            val newMask = StringBuilder(defaultMask)
            newMask.replace('_'.toString().toRegex(), prefix.toString())
        }
    }

    class Action(val actionType: ActionTypes, val cursorPosition: Int, val count: Int)
    enum class ActionTypes { NONE, DELETING, ADDING }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (isUserInput) {
            action = if (after < count) {
                Action(
                    ActionTypes.DELETING,
                    editTextRef?.selectionEnd ?: -1,
                    count
                )
            } else {
                Action(ActionTypes.ADDING, cursorPosition = editTextRef?.selectionEnd ?: -1, after)
            }
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        //
    }

    override fun afterTextChanged(s: Editable?) {
        if (isUserInput) {
            isUserInput = false
            editTextRef?.setText(generateMask(s.toString()))
        } else {
            setCursor()
            isUserInput = true
        }
    }

    fun cleanPhone(phone: String): String = phone.filter { it.isDigit() }

    /*
    *  Using for force to mask generation.
    *
     */
    fun forceToMask() = editTextRef?.setText(generateMask(editTextRef?.text.toString()))

    private fun deletePart(inputText: String): String {
        val startingIndex = action.cursorPosition - action.count
        return when {
            startingIndex < 0 -> {
                "oops"
            }
            else -> {
                StringBuilder(inputText).delete(
                    action.cursorPosition - action.count,
                    action.cursorPosition
                )
                    .toString()
            }
        }
    }


    fun generateMask(inputText: String, action: Action = this.action): String {
        return when (val input = createValidInputIfReachedEnd(inputText)) {
            null -> {
                when (action.actionType) {
                    ActionTypes.ADDING -> {
                        cleanPhone(inputText).let { digits ->
                            when {
                                digits.isEmpty() -> {
                                    prevPhone = phoneMask
                                }
                                isAddingStart(action) && prevPhone.isNotEmpty() -> {
                                    return@let
                                }
                                isPhoneStartsWith5(digits) -> {
                                    prevPhone = buildMask(digits)
                                }
                            }
                        }
                        prevPhone
                    }
                    ActionTypes.DELETING -> {
                        cleanPhone(inputText).let { digits ->
                            prevPhone = buildMask(digits)
                        }
                        prevPhone
                    }
                    else -> {
                        //TODO: Delete this.
                        "oops"
                    }
                }

            }
            else -> input
        }
    }

    private fun isAddingStart(action: Action): Boolean =
        action.actionType == ActionTypes.ADDING && action.cursorPosition < 2 && action.count <= 1

    private fun isPhoneStartsWith5(inputText: String) =
        if (inputText.isNotEmpty()) inputText[0] == '5' else false

    private fun buildMask(digits: String): String {
        val newMask = StringBuilder(phoneMask)
        digits.forEachIndexed { index, digit ->
            when (index) {
                0 -> {
                    if (digit != '5') newMask.setCharAt(newMask.indexOf(prefix.toString()), digit)
                    else return@forEachIndexed
                }
                maxDigitCount -> return newMask.toString()
                else -> newMask.setCharAt(newMask.indexOf(prefix.toString()), digit)
            }
        }
        return newMask.toString()
    }

    private fun isInputReachedEnd(inputText: String, prevPhone: String): Boolean =
        inputText.length > maxInputLength && prevPhone.last().isDigit()

    /**
     * If input reaches [maxInputLength] returns first [maxInputLength] of character for [inputText].
     * Otherwise return null
     * @param inputText param to be check reaching end
     *
     */
    private fun createValidInputIfReachedEnd(inputText: String): String? =
        if (isInputReachedEnd(inputText, prevPhone)) prevPhone
        else null

    private fun setCursor() {
        when (action.actionType) {
            ActionTypes.DELETING -> {
                if (isThereAnyDigitFront(editTextRef)) {
                    val behindPart =
                        editTextRef?.text.toString().subSequence(0, action.cursorPosition)
                            .toString()
                    behindPart.let {
                        val findLastDigitIndex = it.indexOfLast { it.isDigit() }
                        if (findLastDigitIndex != -1) {
                            if (findLastDigitIndex + 1 != it.length)
                                editTextRef?.setSelection(findLastDigitIndex + 1)
                            else {
                                editTextRef?.setSelection(findLastDigitIndex)
                            }
                        }
                    }
                } else {
                    val text = editTextRef?.text.toString()
                    val indexOfLastDigit = text.indexOfLast { it.isDigit() }
                    if (indexOfLastDigit != -1) {
                        if (indexOfLastDigit + 1 != text.length)
                            editTextRef?.setSelection(indexOfLastDigit + 1)
                        else {
                            editTextRef?.setSelection(indexOfLastDigit)
                        }
                    }

                }
            }
            ActionTypes.ADDING -> {
                if (isThereAnyDigitFront(editTextRef) && isNextCharIsDigit()) {
                    editTextRef?.let {
                        if (action.cursorPosition + 1 != it.text.toString().length)
                            it.setSelection(action.cursorPosition + 1)
                        else it.setSelection(it.text.toString().length)
                    }
                } else {
                    val text = editTextRef?.text.toString()
                    text?.let {
                        if (isThereAnyDigitFront(editTextRef)) {
                            val selection = action.cursorPosition
                            selection.let { cursor ->
                                if (cursor + 1 == text.length) editTextRef?.setSelection(it.length)
                                (text.subSequence(cursor+1, text.length)
                                    .indexOfFirst { it.isDigit() } + 1).also {
                                    editTextRef?.setSelection(cursor+1+it)
                                }
                            }
                        } else {
                            val indexOfLastDigit = text.indexOfLast { it.isDigit() }
                            if (indexOfLastDigit + 1 != it.length)
                                editTextRef?.setSelection(indexOfLastDigit + 1)
                            else
                                editTextRef?.setSelection(it.length)
                        }
                    }
                }
            }
            else -> {

            }
        }
    }

    private fun isNextCharIsDigit(): Boolean {
        if (editTextRef == null) return false
        editTextRef?.text.toString().let {
            with(it) {
                return if (action.cursorPosition + 1 != length)
                    it[action.cursorPosition].isDigit()
                else return false
            }
        }
    }


    private fun isThereAnyDigitFront(editTextRef: EditText?): Boolean {
        val text = editTextRef?.text.toString()
        with(text) {
            try {
                //Needs to create mask
                if (action.cursorPosition == 0) return false
                if (action.actionType == ActionTypes.DELETING) {
                    if (action.cursorPosition + 1 == length) {
                        subSequence(action.cursorPosition - 1, length).let {
                            return it.contains("\\d+".toRegex())
                        }
                    } else {
                        if (prevPhone.isNotEmpty()) prevPhone.subSequence(
                            action.cursorPosition - 1,
                            length
                        ).let {
                            return it.contains("\\d+".toRegex())
                        }
                        subSequence(action.cursorPosition, length).let {
                            return it.contains("\\d+".toRegex())
                        }
                    }

                }

                subSequence(action.cursorPosition + 1, length).let {
                    return it.contains("\\d+".toRegex())
                }
            } catch (e: IndexOutOfBoundsException) {
                return false
            }
        }
    }
}