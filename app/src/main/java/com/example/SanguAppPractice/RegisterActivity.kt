package com.example.SanguAppPractice

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SanguAppPractice.helper.ApiHelper
import com.example.SanguAppPractice.ui.theme.Gray
import com.example.SanguAppPractice.ui.theme.Module2_PC21Theme
import com.example.SanguAppPractice.ui.theme.Primary
import org.json.JSONObject

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Module2_PC21Theme {
                Scaffold (modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegisterScreen()
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(){
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ){
        Image(
            painter = painterResource(R.drawable.logo_name),
            null
        )

        Text(
            "Register",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,

            )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = name,
            placeholder = { Text("Name", color = Color.LightGray) },
            onValueChange = {
                name = it
            },
            textStyle = TextStyle(
                color = Color.White
            ),
            colors = TextFieldDefaults.colors().copy(
                unfocusedContainerColor = Gray,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Gray,
                focusedLabelColor = Color.White

            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()

        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            placeholder = { Text("Email", color = Color.LightGray) },
            onValueChange = {
                email = it
            },
            textStyle = TextStyle(
                color = Color.White
            ),
            colors = TextFieldDefaults.colors().copy(
                unfocusedContainerColor = Gray,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Gray,
                focusedLabelColor = Color.White

            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()

        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = age,
            placeholder = { Text("Age", color = Color.LightGray) },
            onValueChange = {
                age = it
            },
            textStyle = TextStyle(
                color = Color.White
            ),
            colors = TextFieldDefaults.colors().copy(
                unfocusedContainerColor = Gray,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Gray,
                focusedLabelColor = Color.White

            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()

        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            placeholder = { Text("Password", color = Color.LightGray) },
            onValueChange = {
                password = it
            },
            textStyle = TextStyle(
                color = Color.White
            ),
            colors = TextFieldDefaults.colors().copy(
                unfocusedContainerColor = Gray,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Gray,
                focusedLabelColor = Color.White

            ),
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()

        )

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = {
                val body = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                    put("fullname", name)
                    put("age", age)
                }
                ApiHelper.request(
                    "/Auth/register",
                    method = "POST",
                    body = body,
                    callback = {ok, res ->

                        if(age.toInt() < 12){
                            Toast.makeText(
                                context,
                                "Age must be at least 13 years old",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@request
                        }

                        if(password.length < 6){
                            Toast.makeText(
                                context,
                                "Password length must be at least 6",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@request
                        }

                        if(ok) {
                            Toast.makeText(context, JSONObject(res).optString("message"), Toast.LENGTH_SHORT).show()


                            context.startActivity(Intent(context, LoginActivity::class.java))
                        } else {
                            Toast.makeText(context, JSONObject(res).optString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SIGN UP")
        }

        Row {
            Text("Already have an account?")
            Spacer(modifier = Modifier.width(10.dp))
            Text("Log in", color = Primary,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                })
        }

    }
}