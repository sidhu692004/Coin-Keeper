package com.example.foreverrusher

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class SharedBudgetFragment : Fragment() {

    private val memberViews = mutableListOf<Pair<EditText, EditText>>() // Name & Expense

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_shared_budget, container, false)
        val containerLayout = view.findViewById<LinearLayout>(R.id.memberInputContainer)

        val count = arguments?.getInt("member_count") ?: 0

        // Create inputs dynamically
        repeat(count) { index ->
            val nameField = EditText(requireContext()).apply {
                hint = "Member ${index + 1} Name"
            }
            val expenseField = EditText(requireContext()).apply {
                hint = "Expense ₹"
                inputType = InputType.TYPE_CLASS_NUMBER
            }
            containerLayout.addView(nameField)
            containerLayout.addView(expenseField)
            memberViews.add(nameField to expenseField)
        }

        // Add action buttons
        fun createButton(text: String, onClick: () -> Unit): Button {
            return Button(requireContext()).apply {
                this.text = text
                setOnClickListener { onClick() }
            }
        }

        containerLayout.addView(createButton("Show Expenses") { showExpenses() })
        containerLayout.addView(createButton("Reset All") { resetAll() })
        containerLayout.addView(createButton("Divide Equally") { divideExpensesEqually() })

        return view
    }

    private fun showExpenses() {
        val result = buildString {
            memberViews.forEach {
                val name = it.first.text.toString()
                val expense = it.second.text.toString().toIntOrNull() ?: 0
                append("$name: ₹$expense\n")
            }
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Expenses")
            .setMessage(result)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun resetAll() {
        memberViews.forEach { it.second.setText("") }
    }

    private fun divideExpensesEqually() {
        val total = memberViews.sumOf { it.second.text.toString().toIntOrNull() ?: 0 }
        val each = if (memberViews.isNotEmpty()) total / memberViews.size else 0
        AlertDialog.Builder(requireContext())
            .setTitle("Equal Division")
            .setMessage("Total: ₹$total\nEach Pays: ₹$each")
            .setPositiveButton("OK", null)
            .show()
    }
}
