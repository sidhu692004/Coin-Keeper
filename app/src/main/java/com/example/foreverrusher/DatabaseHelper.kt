package com.example.foreverrusher

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "solution"
        private const val DB_VERSION = 2  // Incremented version to 2

        private const val TABLE_BUDGET = "budget_table"
        private const val TABLE_EXPENSE = "expense_table"

        private const val COL_MONTH = "month"  // yyyy-MM format
        private const val COL_BUDGET = "budget"

        private const val COL_DATE = "date"    // yyyy-MM-dd format
        private const val COL_AMOUNT = "amount"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DatabaseHelper", "Creating tables...")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_BUDGET (" +
                    "$COL_MONTH TEXT PRIMARY KEY, " +
                    "$COL_BUDGET INTEGER)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_EXPENSE (" +
                    "$COL_DATE TEXT PRIMARY KEY, " +
                    "$COL_AMOUNT INTEGER)"
        )
        Log.d("DatabaseHelper", "Tables created successfully.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("DatabaseHelper", "Upgrading database from version $oldVersion to $newVersion")
        // For now, simple drop and recreate (you can add migration logic here if needed)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSE")
        onCreate(db)
    }

    // Budget operations
    fun isBudgetExists(month: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_BUDGET WHERE $COL_MONTH = ?", arrayOf(month)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun insertBudget(month: String, budget: Int): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_MONTH, month)
            put(COL_BUDGET, budget)
        }
        val result = db.insert(TABLE_BUDGET, null, cv)
        return result != -1L
    }

    fun updateBudget(month: String, budget: Int): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_BUDGET, budget)
        }
        val result = db.update(TABLE_BUDGET, cv, "$COL_MONTH=?", arrayOf(month))
        return result > 0
    }

    fun getAllBudgets(): Map<String, Int> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM budget_table", null)
        val map = mutableMapOf<String, Int>()
        if (cursor.moveToFirst()) {
            do {
                val month = cursor.getString(0) // yyyy-MM
                val budget = cursor.getInt(1)
                map[month] = budget
            } while (cursor.moveToNext())
        }
        cursor.close()
        return map
    }

    fun getMonthlyExpenses(): Map<String, Int> {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUBSTR(date, 1, 7) AS month, SUM(amount) FROM Expenses GROUP BY month ORDER BY month DESC",
            null
        )

        val monthlyExpenses = mutableMapOf<String, Int>()

        if (cursor.moveToFirst()) {
            do {
                val month = cursor.getString(0)
                val total = cursor.getInt(1)
                monthlyExpenses[month] = total
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return monthlyExpenses
    }


    fun getBudgetForMonth(month: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_BUDGET FROM $TABLE_BUDGET WHERE $COL_MONTH=?", arrayOf(month)
        )
        var budget = 0
        if (cursor.moveToFirst()) {
            budget = cursor.getInt(0)
        }
        cursor.close()
        return budget
    }

    // Expense operations
    fun insertExpense(date: String, amount: Int): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_AMOUNT FROM $TABLE_EXPENSE WHERE $COL_DATE=?", arrayOf(date)
        )

        return if (cursor.moveToFirst()) {
            // Update existing: add amount
            val oldAmount = cursor.getInt(0)
            val newAmount = oldAmount + amount
            cursor.close()

            val cv = ContentValues().apply {
                put(COL_AMOUNT, newAmount)
            }
            val res = db.update(TABLE_EXPENSE, cv, "$COL_DATE=?", arrayOf(date))
            res > 0
        } else {
            cursor.close()
            // Insert new
            val cv = ContentValues().apply {
                put(COL_DATE, date)
                put(COL_AMOUNT, amount)
            }
            val res = db.insert(TABLE_EXPENSE, null, cv)
            res != -1L
        }
    }

    fun readAllExpenses(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_EXPENSE ORDER BY $COL_DATE DESC", null)
    }
}
