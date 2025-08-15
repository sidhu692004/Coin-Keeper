package com.example.foreverrusher

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

data class MonthlyExpense(
    val month: String,
    val totalExpense: Int,
    val budget: Int
)

class MonthlyExpenseAdapter(private val expenseList: List<MonthlyExpense>) :
    RecyclerView.Adapter<MonthlyExpenseAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthText: TextView = itemView.findViewById(R.id.monthText)
        val totalText: TextView = itemView.findViewById(R.id.totalText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = expenseList[position]
        holder.monthText.text = item.month
        holder.totalText.text = "Total: ₹${item.totalExpense}, Budget: ₹${item.budget}"
    }

    override fun getItemCount(): Int = expenseList.size
}

class PersonalBudgetFragment : Fragment() {

    private lateinit var mydb: DatabaseHelper
    private lateinit var money: EditText
    private lateinit var budgetInput: EditText
    private lateinit var insert: Button
    private lateinit var display: Button
    private lateinit var logout: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var monthlyRecycler: RecyclerView
    private val monthlyExpenseList = mutableListOf<MonthlyExpense>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_personal_budget, container, false)


        mydb = DatabaseHelper(requireContext())

        money = view.findViewById(R.id.money)
        budgetInput = view.findViewById(R.id.budget)
        insert = view.findViewById(R.id.button)
        display = view.findViewById(R.id.button4)
        logout = view.findViewById(R.id.logoutBtn)
        monthlyRecycler = view.findViewById(R.id.monthlyExpenseRecycler)

        monthlyRecycler.layoutManager = LinearLayoutManager(requireContext())
        monthlyRecycler.adapter = MonthlyExpenseAdapter(monthlyExpenseList)

        setupListeners()
        resetBudgetIfNewMonth()
        loadMonthlyExpenses()

        return view
    }

    private fun loadMonthlyExpenses() {
        monthlyExpenseList.clear()

        val grouped = mutableMapOf<String, Int>()
        val res = mydb.readAllExpenses()
        while (res.moveToNext()) {
            val date = res.getString(0)
            val amount = res.getInt(1)
            val month = date.substring(0, 7) // yyyy-MM

            grouped[month] = grouped.getOrDefault(month, 0) + amount
        }

        for ((month, total) in grouped) {
            val budget = mydb.getBudgetForMonth(month)
            monthlyExpenseList.add(MonthlyExpense(month, total, budget))
        }

        monthlyExpenseList.sortByDescending { it.month }
        monthlyRecycler.adapter?.notifyDataSetChanged()
    }

    private fun setupListeners() {
        insert.setOnClickListener {
            val expenseStr = money.text.toString().trim()
            val budgetStr = budgetInput.text.toString().trim()
            val today = getTodayDate()
            val month = getCurrentMonth()

            if (budgetStr.isNotEmpty()) {
                if (mydb.isBudgetExists(month)) {
                    mydb.updateBudget(month, budgetStr.toInt())
                } else {
                    mydb.insertBudget(month, budgetStr.toInt())
                }
            }

            if (expenseStr.isNotEmpty()) {
                val result = mydb.insertExpense(today, expenseStr.toInt())
                if (result) {
                    Toast.makeText(requireContext(), "Expense Added", Toast.LENGTH_SHORT).show()
                    money.text.clear()
                    loadMonthlyExpenses()
                } else {
                    Toast.makeText(requireContext(), "Error inserting expense", Toast.LENGTH_SHORT).show()
                }
            }
        }

        display.setOnClickListener {
            val sb = StringBuilder()
            val today = getTodayDate()
            val yesterday = getYesterdayDate()

            var grandTotal = 0
            var todayExpense = 0
            var yesterdayExpense = 0

            val res = mydb.readAllExpenses()
            while (res.moveToNext()) {
                val date = res.getString(0)
                val amount = res.getInt(1)
                grandTotal += amount
                if (date == today) todayExpense += amount
                if (date == yesterday) yesterdayExpense += amount
            }

            val summary = """
                🧾 Summary:
                🔸 Grand Total Expense: ₹$grandTotal
                🔸 Today's Expense: ₹$todayExpense
                🔸 Yesterday's Expense: ₹$yesterdayExpense
            """.trimIndent()

            showMessage("Summary", summary)
        }

        logout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun resetBudgetIfNewMonth() {
        val currentMonth = getCurrentMonth()
        if (!mydb.isBudgetExists(currentMonth)) {
            mydb.insertBudget(currentMonth, 0)
        }
    }

    private fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun getCurrentMonth(): String =
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    private fun showMessage(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .show()
    }
}
