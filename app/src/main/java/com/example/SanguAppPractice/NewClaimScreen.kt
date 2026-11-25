
package com.example.SanguAppPractice

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import org.json.JSONTokener
import java.text.NumberFormat
import java.util.Locale

@Composable
fun NewClaimScreen(
    token: String?,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var claimDate by remember { mutableStateOf("") }

    var categoryId by remember { mutableIntStateOf(0) }
    var categoryName by remember { mutableStateOf("Select Category") }
    var expanded by remember { mutableStateOf(false) }

    var categories by remember {
        mutableStateOf<List<JSONObject>>(emptyList())
    }

    var saving by remember { mutableStateOf(false) }
    var loadingCategories by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val items = remember {
        mutableStateListOf(
            JSONObject().apply {
                put("description", "")
                put("amount", "")
                put("receiptNumber", "")
            }
        )
    }

    LaunchedEffect(token) {

        if (token.isNullOrEmpty()) {
            message = "Please login again"
        } else {

            loadingCategories = true

            ApiHelper.request(
                path = "/categories",
                method = "GET",
                token = token,
                callback = { ok, res ->

                    loadingCategories = false

                    if (ok) {
                        try {
                            val response = JSONTokener(res).nextValue()

                            val array = when (response) {
                                is JSONArray -> response
                                is JSONObject -> {
                                    response.optJSONArray("items")
                                        ?: response.optJSONArray("categories")
                                }
                                else -> null
                            }

                            val list = mutableListOf<JSONObject>()

                            if (array != null) {
                                for (i in 0 until array.length()) {
                                    list.add(array.getJSONObject(i))
                                }
                            }

                            categories = list

                            if (list.isEmpty()) {
                                message = "No categories available"
                            }

                        } catch (e: Exception) {
                            message = "Cannot read categories"
                        }

                    } else {
                        message = "Failed to load categories"
                    }
                }
            )
        }
    }

    fun updateItem(
        index: Int,
        key: String,
        value: String
    ) {
        val updated = JSONObject(items[index].toString())

        updated.put(key, value)

        items[index] = updated
    }

    val total = items.sumOf {
        it.optString("amount").toDoubleOrNull() ?: 0.0
    }

    val formattedTotal = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("id-ID")
    ).format(total)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {

        Text(
            text = "← New Claim",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.clickable {
                if (!saving) {
                    onBack()
                }
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                text = "Title",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Description",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Date",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    OutlinedTextField(
                        value = claimDate,
                        onValueChange = { claimDate = it },
                        placeholder = {
                            Text("YYYY-MM-DD", fontSize = 11.sp)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Category",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Box {
                        OutlinedButton(
                            onClick = {
                                expanded = true
                            },
                            enabled = !loadingCategories &&
                                    categories.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = categoryName,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {

                            for (category in categories) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            category.optString("name")
                                        )
                                    },
                                    onClick = {
                                        categoryId = category.optInt("id")
                                        categoryName = category.optString("name")
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (loadingCategories) {
                Text(
                    text = "Loading categories...",
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Line Items",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                OutlinedButton(
                    onClick = {

                        items.add(
                            JSONObject().apply {
                                put("description", "")
                                put("amount", "")
                                put("receiptNumber", "")
                            }
                        )

                    },
                    enabled = !saving
                ) {
                    Text("+ Add Item", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            for (i in items.indices) {

                val item = items[i]

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, Color.Gray)
                        .padding(8.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Description",
                                fontSize = 11.sp
                            )

                            OutlinedTextField(
                                value = item.optString("description"),
                                onValueChange = {
                                    updateItem(i, "description", it)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Column(
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text(
                                text = "Amount",
                                fontSize = 11.sp
                            )

                            OutlinedTextField(
                                value = item.optString("amount"),
                                onValueChange = {
                                    updateItem(i, "amount", it)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )
                        }

                        TextButton(
                            onClick = {
                                items.removeAt(i)
                            },
                            enabled = !saving,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.width(36.dp)
                        ) {
                            Text("X", color = Color.Red)
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Receipt No",
                            fontSize = 11.sp
                        )

                        Spacer(Modifier.width(8.dp))

                        OutlinedTextField(
                            value = item.optString("receiptNumber"),
                            onValueChange = {
                                updateItem(i, "receiptNumber", it)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Rp $formattedTotal",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {

                    if (token.isNullOrEmpty()) {
                        message = "Please login again"
                        return@Button
                    }

                    if (title.isBlank()) {
                        message = "Title is required"
                        return@Button
                    }

                    if (description.isBlank()) {
                        message = "Description is required"
                        return@Button
                    }

                    val dateFormat = Regex("\\d{4}-\\d{2}-\\d{2}")

                    if (!dateFormat.matches(claimDate)) {
                        message = "Date must be YYYY-MM-DD"
                        return@Button
                    }

                    if (categoryId <= 0) {
                        message = "Please select a category"
                        return@Button
                    }

                    if (items.isEmpty()) {
                        message = "Add at least one line item"
                        return@Button
                    }

                    val itemArray = JSONArray()

                    for (item in items) {

                        val itemDescription =
                            item.optString("description").trim()

                        val amount =
                            item.optString("amount").toDoubleOrNull()

                        val receipt =
                            item.optString("receiptNumber").trim()

                        if (itemDescription.isBlank()) {
                            message = "Item description is required"
                            return@Button
                        }

                        if (
                            amount == null ||
                            !amount.isFinite() ||
                            amount <= 0
                        ) {
                            message = "Amount must be greater than zero"
                            return@Button
                        }

                        itemArray.put(
                            JSONObject().apply {
                                put("description", itemDescription)
                                put("amount", amount)
                                put("spentOn", claimDate)
                                put("receiptNumber", receipt)
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

                    saving = true
                    message = ""

                    ApiHelper.request(
                        path = "/claims",
                        method = "POST",
                        body = body,
                        token = token,
                        callback = { ok, res ->

                            saving = false

                            if (ok) {
                                Toast.makeText(
                                    context,
                                    "Claim created successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                onCreated()

                            } else {

                                message = try {
                                    JSONObject(res).optString(
                                        "message",
                                        "Failed to create claim"
                                    ).ifBlank {
                                        "Failed to create claim"
                                    }
                                } catch (e: Exception) {
                                    "Failed to create claim"
                                }
                            }
                        }
                    )
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (saving) "Saving..." else "Save"
                )
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}
