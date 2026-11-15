package ma.amts.depenses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ma.amts.depenses.ui.ExpenseViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AmtsApp() } }
    }
}

private fun money(centimes: Long): String = "%.2f DH".format(java.util.Locale.FRANCE, centimes / 100.0)

@Composable
fun AmtsApp(vm: ExpenseViewModel = viewModel()) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    var page by remember { mutableStateOf("accueil") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var chantier by remember { mutableStateOf("") }
    var funding by remember { mutableStateOf("Caisse AMTS") }
    var receipt by remember { mutableStateOf("À fournir") }
    var error by remember { mutableStateOf("") }

    fun parseCentimes(text: String): Long? = try {
        val normalized = text.trim().replace(',', '.')
        val decimal = BigDecimal(normalized)
        if (decimal <= BigDecimal.ZERO || decimal.scale() > 2) null
        else decimal.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact()
    } catch (_: Exception) { null }

    Scaffold(topBar = {
        Surface(tonalElevation = 3.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("AMTS Dépenses", style = MaterialTheme.typography.titleLarge)
                Text("Fouad • Mode local hors ligne", style = MaterialTheme.typography.bodySmall)
            }
        }
    }, bottomBar = {
        NavigationBar {
            NavigationBarItem(selected = page == "accueil", onClick = { page = "accueil" }, icon = { Text("⌂") }, label = { Text("Accueil") })
            NavigationBarItem(selected = page == "saisie", onClick = { page = "saisie" }, icon = { Text("+") }, label = { Text("Dépense") })
            NavigationBarItem(selected = page == "historique", onClick = { page = "historique" }, icon = { Text("≡") }, label = { Text("Historique") })
        }
    }) { padding ->
        when (page) {
            "accueil" -> Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Bonjour Fouad", style = MaterialTheme.typography.headlineSmall)
                Text("Dépenses enregistrées localement : ${expenses.size}")
                Text("Total des dépenses enregistrées : ${money(expenses.sumOf { it.amountCentimes })}", style = MaterialTheme.typography.titleMedium)
                Text("Ce total n'est PAS un solde de caisse : les mouvements et avances seront ajoutés dans la prochaine version.", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { page = "saisie" }, modifier = Modifier.fillMaxWidth()) { Text("+ Nouvelle dépense") }
                OutlinedButton(onClick = { page = "historique" }, modifier = Modifier.fillMaxWidth()) { Text("Voir l'historique") }
                Text("Aucune synchronisation Drive dans cette version. Les données restent sur ce téléphone.", style = MaterialTheme.typography.bodySmall)
            }
            "saisie" -> Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nouvelle dépense", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(amount, { amount = it }, label = { Text("Montant en DH") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text("Motif") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(chantier, { chantier = it }, label = { Text("Chantier (facultatif)") }, modifier = Modifier.fillMaxWidth())
                Text("Financement")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Caisse AMTS", "Personnel").forEach { option ->
                        FilterChip(selected = funding == option, onClick = { funding = option }, label = { Text(option) })
                    }
                }
                Text("Justificatif")
                listOf("À fournir", "Sans justificatif", "Disponible").forEach { option ->
                    Row { RadioButton(selected = receipt == option, onClick = { receipt = option }); Text(option, Modifier.padding(top = 12.dp)) }
                }
                if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
                Button(onClick = {
                    val cents = parseCentimes(amount)
                    when {
                        cents == null -> error = "Indiquez un montant valide, positif, avec 2 décimales maximum."
                        description.isBlank() -> error = "Indiquez un motif."
                        else -> {
                            vm.add(cents, description, chantier, funding, receipt)
                            amount = ""; description = ""; chantier = ""; error = ""; page = "historique"
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Enregistrer sur ce téléphone") }
                Text("La photo du justificatif sera ajoutée à la prochaine version. 'Disponible' est seulement une déclaration pour l'instant.", style = MaterialTheme.typography.bodySmall)
            }
            else -> LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text("Historique local", style = MaterialTheme.typography.headlineSmall) }
                if (expenses.isEmpty()) item { Text("Aucune dépense enregistrée.") }
                items(expenses, key = { it.id }) { expense ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("${money(expense.amountCentimes)} — ${expense.description}", style = MaterialTheme.typography.titleMedium)
                            Text("${expense.chantier.ifBlank { "Sans chantier" }} • ${expense.funding}")
                            Text("${expense.receiptStatus} • ${DateFormat.getDateTimeInstance().format(Date(expense.createdAt))}", style = MaterialTheme.typography.bodySmall)
                            Text("Statut : local, non synchronisé", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
