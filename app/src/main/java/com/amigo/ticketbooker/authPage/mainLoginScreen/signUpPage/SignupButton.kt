package com.amigo.ticketbooker.authPage.mainLoginScreen.signUpPage


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel
import com.amigo.ticketbooker.fontFamily

@Composable
fun SignupButton(
    authViewModel: AuthViewModel,
    email: String,
    password: String,
    confirmPassword: String,
    termsAccepted: Boolean,
    isLoading: Boolean = false
) {
    Button(
        onClick = {
            if (validateSignUpForm(email, password, confirmPassword, termsAccepted)) {
                authViewModel.signUpWithEmailPassword(email, password)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3949AB)
        ),
        shape = RoundedCornerShape(8.dp),
        enabled = !isLoading && validateSignUpForm(email, password, confirmPassword, termsAccepted)
    ) {
        Text(
            text = "SIGN UP",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun validateSignUpForm(
    email: String,
    password: String,
    confirmPassword: String,
    termsAccepted: Boolean
): Boolean {
    return email.isNotEmpty() &&
            password.isNotEmpty() &&
            password == confirmPassword &&
            password.length >= 6 &&
            termsAccepted
}