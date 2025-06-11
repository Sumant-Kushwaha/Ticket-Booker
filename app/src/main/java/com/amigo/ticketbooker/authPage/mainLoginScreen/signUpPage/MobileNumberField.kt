package com.amigo.ticketbooker.authPage.mainLoginScreen.signUpPage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.font.fontFamily

@Composable
fun MobileNumberField(
    mobileNumber: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean = false
) {
    OutlinedTextField(
        value = mobileNumber,
        onValueChange = { input ->
            if (input.length <= 10 && input.all { it.isDigit() }) {
                onValueChange(input)
            }
        },
        label = { Text("Mobile Number") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = Color.Black, fontFamily = fontFamily, fontSize = 18.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF3949AB),
            focusedLabelColor = Color(0xFF3949AB),
            cursorColor = Color(0xFF3949AB)
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_phone),
                contentDescription = "Mobile",
                tint = Color.Gray
            )
        },
        prefix = { Text("+91 ") },
        enabled = !isLoading
    )
}