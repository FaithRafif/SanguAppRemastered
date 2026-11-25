package com.example.SanguAppPractice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

class ClaimListActivity : ComponentActivity() {

    private var refreshCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                ClaimListScreen(refreshCount)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCount++
    }
}

@Composable
fun ClaimListScreen(refreshCount: Int = 0) {

    val context = LocalContext.current
    var page by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }


    var status by remember { mutableStateOf("All") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }

    var claims by remember {
        mutableStateOf<List<JSONObject>>(emptyList())
    }
    //kita pelajarin nanti beserta methodnya dibawah
    var showCreate by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var appliedSearch by remember { mutableStateOf("") }


    val token = remember {
        context.getSharedPreferences(
            "SanguSession",
            Context.MODE_PRIVATE
        ).getString("token", null)
    }

    LaunchedEffect(
        page,
        status,
        appliedSearch,
        refresh,
        refreshCount
    ){
        if (token.isNullOrEmpty()) {
            claims = emptyList()
            loading = false
            message = "Please login again"
        } else {

            loading = true
            message = ""

            ApiHelper.request(
                path = "/claims",
                method = "GET",
                token = token,
                query = mapOf(
                    "status" to if (status == "All") "" else status,
                    "search" to appliedSearch,
                    "page" to page.toString(),
                    "pageSize" to "10"
                ),
                callback = { ok, res ->
                    loading = false
                    if (ok) {
                        try {
                            val json = JSONObject(res)
                            val items = json.optJSONArray("items")

                            val list = mutableListOf<JSONObject>()

                            if (items != null) {
                                for (i in 0 until items.length()) {
                                    list.add(items.getJSONObject(i))
                                }
                            }
                            claims = list

                            totalPages = json.optInt(
                                "totalPages",
                                1
                            ).coerceAtLeast(1)

                            if (claims.isEmpty()) {
                                message = "No claims found"
                            }

                        } catch (e: Exception) {
                            claims = emptyList()
                            message = "Cannot read API response"
                        }

                    } else {
                        claims = emptyList()
                        totalPages = 1

                        message = try {
                            JSONObject(res).optString(
                                "message",
                                "Failed to load claims"
                            ).ifBlank {
                                "Failed to load claims"
                            }
                        } catch (e: Exception) {
                            "Failed to load claims"
                        }
                    }
                }
            )
        }
    }
    if (showCreate) {

        NewClaimScreen(
            token = token,

            onBack = {
                showCreate = false
            },

            onCreated = {
                showCreate = false

                search = ""
                appliedSearch = ""
                status = "All"
                page = 1

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
                text = "My Claims",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = {
                    Text("Search claim...")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    page = 1
                    appliedSearch = search
                    refresh++
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                val statuses = listOf(
                    "All",
                    "Draft",
                    "Submitted",
                    "Approved",
                    "Rejected"
                )

                for (item in statuses) {

                    Button(
                        onClick = {
                            status = item
                            page = 1
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == item) {
                                Color.DarkGray
                            } else {
                                Color.LightGray
                            },
                            contentColor = if (status == item) {
                                Color.White
                            } else {
                                Color.Black
                            }
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        )
                    ) {
                        Text(
                            text = item,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(claims) { claim ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                                val intent = Intent(
                                    context,
                                    ClaimDetailActivity::class.java
                                )

                                intent.putExtra(
                                    "claimId",
                                    claim.optInt("id")
                                )

                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(
                            1.dp,
                            Color.Gray
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    text = claim.optString("claimNumber"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = claim.optString("status"),
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = claim.optString("title"),
                                fontSize = 13.sp,
                                color = Color.Black
                            )

                            Spacer(Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    text = "${claim.optString("claimDate").take(10)} · ${claim.optString("categoryName")}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.weight(1f)
                                )

                                val amount = NumberFormat.getNumberInstance(
                                    Locale.forLanguageTag("id-ID")
                                ).format(
                                    claim.optDouble("totalAmount")
                                )

                                Text(
                                    text = "Rp $amount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Button(
                    onClick = {
                        page--
                    },
                    enabled = !loading && page > 1
                ) {
                    Text("Prev")
                }

                Text(
                    text = "Page $page of $totalPages",
                    fontSize = 13.sp,
                    color = Color.Black
                )

                Button(
                    onClick = {
                        page++
                    },
                    enabled = !loading && page < totalPages
                ) {
                    Text("Next")
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                Button(
                    onClick = {
                        showCreate = true
                    }
                ) {
                    Text("+ New Claim")
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = "Claims",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Categories",
                    color = Color.Black
                )

                Text(
                    text = "Summary",
                    color = Color.Black
                )
            }
        }
    }
}
