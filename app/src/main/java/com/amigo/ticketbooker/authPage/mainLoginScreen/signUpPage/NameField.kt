package com.amigo.ticketbooker.authPage.mainLoginScreen.signUpPage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
fun NameField(
    fullName: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean = false
) {
    OutlinedTextField(
        value = fullName,
        onValueChange = onValueChange,
        label = { Text("Full Name") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = fontFamily,
            fontSize = 18.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = "Full Name",
                tint = Color.Gray
            )
        },
        enabled = !isLoading
    )
}