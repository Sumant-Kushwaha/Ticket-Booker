package com.amigo.ticketbooker.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amigo.ticketbooker.home.serviceCard.cards.ElevatedColorCard
import com.amigo.ticketbooker.home.serviceCard.cards.ModernGradientCard
import com.amigo.ticketbooker.home.serviceCard.cards.OutlinedIconCard

@Composable
fun ServiceCard(
    title: String,
    icon: Int,
    modifier: Modifier = Modifier,
    index: Int = 0, // Use index to create different design variations
    onClick: () -> Unit = {}
) {
    // Different design variations based on index
    val designType = index % 3

    when (designType) {
        0 -> ModernGradientCard(title, icon, modifier, onClick)
        1 -> OutlinedIconCard(title, icon, modifier, onClick)
        2 -> ElevatedColorCard(title, icon, modifier, onClick)
    }
}