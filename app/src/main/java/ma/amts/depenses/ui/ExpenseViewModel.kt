package ma.amts.depenses.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ma.amts.depenses.data.AmtsDatabase
import ma.amts.depenses.data.Expense
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AmtsDatabase.get(app).expenseDao()
    val expenses = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun add(amountCentimes: Long, description: String, chantier: String, funding: String, receipt: String) {
        viewModelScope.launch {
            dao.insert(Expense(amountCentimes = amountCentimes, description = description.trim(),
                chantier = chantier.trim(), funding = funding, receiptStatus = receipt))
        }
    }
}
