
package com.example.SanguAppPractice
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SanguAppPractice.helper.ApiHelper
import com.example.SanguAppPractice.ui.theme.Module2_PC21Theme
import org.json.JSONObject
import androidx.core.content.edit

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Module2_PC21Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedBorderColor = Color.DarkGray,
        unfocusedBorderColor = Color.DarkGray
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Sangu",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Expense Claim App",
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(50.dp))

        Text(
            text = "Email",
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Password",
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = fieldColors
        )

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = {
                val loginParams = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }
                ApiHelper.request(
                    "/auth/login",
                    "POST",
                    body = loginParams,
                    callback = { ok, res ->
                        if (ok) {
                            Toast.makeText(
                                context,
                                JSONObject(res).optString("message","No message"),
                                Toast.LENGTH_SHORT
                            ).show()
                            val token = JSONObject(res).getString("token")
                            context.getSharedPreferences(
                                "SanguSession",
                                android.content.Context.MODE_PRIVATE
                            ).edit {
                                putString("token", token)
                                apply()
                            }
                            val intent = Intent(
                                context,
                                ClaimListActivity::class.java
                            )
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(
                                context,
                                JSONObject(res).optString("message","oknya false"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.DarkGray),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray,
                contentColor = Color.Black
            )) {
            Text("Login", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Row {
            Text(
                text = "Don't have an account? ",
                color = Color.Black,
                fontSize = 12.sp
            )

            Text(
                text = "Register",
                color = Color.Blue,
                fontSize = 12.sp,
                modifier = Modifier.clickable {
                    context.startActivity(
                        Intent(context, RegisterActivity::class.java)
                    )
                }
            )
        }
    }
}
