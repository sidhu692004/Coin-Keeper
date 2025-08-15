package com.example.foreverrusher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        view.findViewById<Button>(R.id.personalBudgetBtn).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PersonalBudgetFragment())
                .addToBackStack(null).commit()
        }

        view.findViewById<Button>(R.id.sharedBudgetBtn).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SharedBudgetSetupFragment())
                .addToBackStack(null).commit()
        }

        return view
    }
}
