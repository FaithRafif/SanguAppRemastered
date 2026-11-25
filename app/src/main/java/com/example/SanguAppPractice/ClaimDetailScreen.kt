
package com.example.SanguAppPractice

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SanguAppPractice.helper.ApiHelper
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class ClaimDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val claimId = intent.getIntExtra("claimId", -1)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                ClaimDetailScreen(claimId)
            }
        }
    }
}

@Composable
fun ClaimDetailScreen(claimId: Int) {

    val context = LocalContext.current

    var claim by remember { mutableStateOf<JSONObject?>(null) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    var showEdit by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }

    val token = remember {
        context.getSharedPreferences(
            "SanguSession",
            Context.MODE_PRIVATE
        ).getString("token", null)
    }

    val currency = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("id-ID")
    )

    LaunchedEffect(claimId, refresh) {

        if (claimId <= 0) {
            loading = false
            message = "Invalid claim ID"
        } else if (token.isNullOrEmpty()) {
            loading = false
            message = "Please login again"
        } else {

            loading = true
            message = ""

            ApiHelper.request(
                path = "/claims/$claimId",
                method = "GET",
                token = token,
                callback = { ok, res ->

                    loading = false

                    if (ok) {
                        try {
                            claim = JSONObject(res)
                        } catch (e: Exception) {
                            claim = null
                            message = "Cannot read claim details"
                        }
                    } else {
                        claim = null
                        message = try {
                            JSONObject(res).optString(
                                "message",
                                "Failed to load claim"
                            )
                        } catch (e: Exception) {
                            "Failed to load claim"
                        }
                    }
                }
            )
        }
    }

    if (showEdit && claim != null && !token.isNullOrEmpty()) {

        EditClaimScreen(
            claimId = claimId,
            original = claim!!,
            token = token,
            onBack = {
                showEdit = false
            },
            onSaved = {
                showEdit = false
                refresh++
            }
        )

    } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp)
        ) {

            Text(
                text = "← Claim Detail",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.clickable {
                    (context as? ComponentActivity)?.finish()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (loading) {
                Text("Loading claim details...")
            }

            if (message.isNotEmpty()) {
                Text(message, color = Color.Red)
            }

            val data = claim

            if (data != null && !loading) {

                val isDraft = data.optString("status")
                    .equals("Draft", ignoreCase = true)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.LightGray
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = data.optString("claimNumber"),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(data.optString("status"))
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = data.optString("title"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(data.optString("description"))

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "Date: ${data.optString("claimDate").take(10)}"
                            )

                            val category = data.optJSONObject("category")

                            Text(
                                "Category: ${category?.optString("name") ?: "-"}"
                            )

                            val employee = data.optJSONObject("submittedBy")

                            Text(
                                "Employee: ${employee?.optString("name") ?: "-"}"
                            )

                            Text(
                                "Department: ${employee?.optString("department") ?: "-"}"
                            )

                            Text(
                                text = "Total: Rp ${currency.format(data.optDouble("totalAmount"))}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Line Items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    val items = data.optJSONArray("items")

                    if (items == null || items.length() == 0) {
                        Text("No line items")
                    } else {

                        for (i in 0 until items.length()) {

                            val item = items.optJSONObject(i) ?: continue

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = item.optString("description"),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = "Rp ${currency.format(item.optDouble("amount"))}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        "Date: ${item.optString("spentOn").take(10)}"
                                    )

                                    Text(
                                        "Receipt: ${item.optString("receiptNumber")}"
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Rp ${currency.format(data.optDouble("totalAmount"))}",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Approval History",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    val approvals = data.optJSONArray("approvals")

                    if (approvals == null || approvals.length() == 0) {
                        Text("No approval history")
                    } else {

                        for (i in 0 until approvals.length()) {

                            val approval = approvals.optJSONObject(i) ?: continue

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {

                                    Text(
                                        text = "${approval.optString("actor")} - ${approval.optString("action")}",
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = approval.optString("actedAt")
                                            .replace("T", " ")
                                            .take(16)
                                    )

                                    Text(
                                        text = "Note: ${approval.optString("note")}"
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = {
                            showEdit = true
                        },
                        enabled = isDraft && !busy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit")
                    }

                    Button(
                        onClick = {
                            showDeleteDialog = true
                        },
                        enabled = isDraft && !busy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }

                    Button(
                        onClick = {

                            if (!token.isNullOrEmpty()) {

                                busy = true
                                message = ""

                                ApiHelper.request(
                                    path = "/claims/$claimId/submit",
                                    method = "POST",
                                    token = token,
                                    callback = { ok, res ->

                                        busy = false

                                        if (ok) {
                                            Toast.makeText(
                                                context,
                                                "Claim submitted successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                           refresh++

                                        } else {
                                            message = try {
                                                JSONObject(res).optString(
                                                    "message",
                                                    "Submit failed"
                                                )
                                            } catch (e: Exception) {
                                                "Submit failed"
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        enabled = isDraft && !busy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit")
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Edit, Delete and Submit only work for Draft claims.",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }
    }

    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Delete Claim")
            },
            text = {
                Text("Are you sure you want to delete this claim?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false

                        if (!token.isNullOrEmpty() && !busy) {

                            busy = true
                            message = ""

                            ApiHelper.request(
                                path = "/claims/$claimId",
                                method = "DELETE",
                                token = token,
                                callback = { ok, res ->

                                    busy = false

                                    if (ok) {
                                        Toast.makeText(
                                            context,
                                            "Claim deleted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        (context as? ComponentActivity)?.finish()

                                    } else {
                                        message = try {
                                            JSONObject(res).optString(
                                                "message",
                                                "Delete failed"
                                            )
                                        } catch (e: Exception) {
                                            "Delete failed"
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
