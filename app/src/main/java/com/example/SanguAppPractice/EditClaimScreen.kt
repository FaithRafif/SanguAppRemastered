package com.example.SanguAppPractice

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SanguAppPractice.helper.ApiHelper
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EditClaimScreen(
    claimId: Int,
    original: JSONObject,
    token: String,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current

    var title by remember {
        mutableStateOf(original.optString("title"))
    }

    var description by remember {
        mutableStateOf(original.optString("description"))
    }

    var claimDate by remember {
        mutableStateOf(original.optString("claimDate").take(10))
    }

    // Category
    val originalCategory = original.optJSONObject("category")
    var categoryId by remember {
        mutableStateOf(originalCategory?.optInt("id") ?: 1)
    }
    var categoryName by remember {
        mutableStateOf(originalCategory?.optString("name") ?: "Transport")
    }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Example category list
    // Change these IDs/names if your backend uses different categories
    val categories = listOf(
        1 to "Transport",
        2 to "Meals",
        3 to "Accommodation",
        4 to "Office Supplies"
    )

    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Line items
    val items = remember {
        mutableStateListOf<JSONObject>().apply {
            val originalItems = original.optJSONArray("items")

            if (originalItems != null) {
                for (i in 0 until originalItems.length()) {
                    val item = originalItems.optJSONObject(i)
                    if (item != null) {
                        add(JSONObject(item.toString()))
                    }
                }
            }
        }
    }

    fun updateItem(index: Int, key: String, value: String) {
        val newItem = JSONObject(items[index].toString())
        newItem.put(key, value)
        items[index] = newItem
    }

    val totalAmount = items.sumOf {
        it.optString("amount").toDoubleOrNull() ?: 0.0
    }

    val rupiah = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("id-ID")
    ).format(totalAmount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "← Edit Claim",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .border(1.dp, Color.Gray)
                .padding(12.dp)
        ) {

            // TITLE
            Text(
                text = "Title",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // DESCRIPTION
            Text(
                text = "Description",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // DATE + CATEGORY
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Date",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = claimDate,
                        onValueChange = { claimDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box {
                        OutlinedButton(
                            onClick = { categoryExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(categoryName)
                        }

                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            for ((id, name) in categories) {
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        categoryId = id
                                        categoryName = name
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LINE ITEMS HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Line Items",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        items.add(
                            JSONObject().apply {
                                put("description", "")
                                put("amount", "")
                                put("spentOn", claimDate)
                                put("receiptNumber", "")
                            }
                        )
                    },
                    enabled = !loading
                ) {
                    Text("+ Add Item")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ITEM LIST
            for (i in items.indices) {
                val item = items[i]

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .border(1.dp, Color.Gray)
                        .padding(8.dp)
                ) {
                    // Row 1: Description + Amount
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = item.optString("description"),
                            onValueChange = {
                                updateItem(i, "description", it)
                            },
                            label = { Text("Description") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = item.optString("amount"),
                            onValueChange = {
                                updateItem(i, "amount", it)
                            },
                            label = { Text("Amount") },
                            modifier = Modifier.width(110.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Receipt No + Remove button
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = item.optString("receiptNumber"),
                            onValueChange = {
                                updateItem(i, "receiptNumber", it)
                            },
                            label = { Text("Receipt No") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = {
                                items.removeAt(i)
                            },
                            modifier = Modifier.width(60.dp),
                            enabled = !loading
                        ) {
                            Text("X")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // TOTAL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Rp $rupiah",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    val dateRegex = Regex("\\d{4}-\\d{2}-\\d{2}")

                    if (title.isBlank()) {
                        message = "Title cannot be empty"
                        return@Button
                    }

                    if (claimDate.isBlank() || !dateRegex.matches(claimDate)) {
                        message = "Claim date must be YYYY-MM-DD"
                        return@Button
                    }

                    if (items.isEmpty()) {
                        message = "Add at least one item"
                        return@Button
                    }

                    val itemArray = JSONArray()

                    for (item in items) {
                        val itemDescription = item.optString("description").trim()
                        val itemAmount = item.optString("amount").toDoubleOrNull()
                        val itemSpentOn = item.optString("spentOn").ifBlank { claimDate }
                        val itemReceipt = item.optString("receiptNumber").trim()

                        if (itemDescription.isBlank()) {
                            message = "Item description cannot be empty"
                            return@Button
                        }

                        if (itemAmount == null || itemAmount < 0) {
                            message = "Item amount is invalid"
                            return@Button
                        }

                        itemArray.put(
                            JSONObject().apply {
                                put("description", itemDescription)
                                put("amount", itemAmount)
                                put("spentOn", itemSpentOn)
                                put("receiptNumber", itemReceipt)
                            }
                        )
                    }

                    val body = JSONObject().apply {
                        put("title", title.trim())
                        put("description", description.trim())
                        put("claimDate", claimDate)
                        put("categoryId", categoryId)
                        put("items", itemArray)
                    }

                    loading = true
                    message = ""

                    ApiHelper.request(
                        path = "/claims/$claimId",
                        method = "PUT",
                        body = body,
                        token = token,
                        callback = { ok, res ->
                            loading = false

                            if (ok) {
                                Toast.makeText(
                                    context,
                                    "Claim updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                onSaved()
                            } else {
                                message = try {
                                    JSONObject(res).optString(
                                        "message",
                                        "Failed to update claim"
                                    )
                                } catch (e: Exception) {
                                    "Failed to update claim"
                                }
                            }
                        }
                    )
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (loading) "Saving..." else "Save"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (message.isBlank()) "message area" else message,
                        color = if (message.isBlank()) Color.Gray else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBack,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}