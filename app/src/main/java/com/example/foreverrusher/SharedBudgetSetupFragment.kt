package com.example.foreverrusher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class SharedBudgetSetupFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_shared_budget_setup, container, false)

        val input = view.findViewById<EditText>(R.id.memberCountInput)
        val nextBtn = view.findViewById<Button>(R.id.nextBtn)

        nextBtn.setOnClickListener {
            val count = input.text.toString().toIntOrNull()
            if (count != null && count > 0) {
                val fragment = SharedBudgetFragment()
                fragment.arguments = Bundle().apply { putInt("member_count", count) }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            } else {
                Toast.makeText(context, "Enter valid number", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
