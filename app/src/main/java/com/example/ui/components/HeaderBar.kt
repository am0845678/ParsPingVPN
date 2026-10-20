package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ParsConnectedGreen
import com.example.ui.theme.ParsCyanNeon
import com.example.ui.theme.ParsElectricBlue
import com.example.ui.theme.ParsNavyCard
import com.example.ui.theme.ParsNavyCardBorder
import com.example.ui.theme.ParsNavySurface
import com.example.ui.theme.ParsTextMuted
import com.example.ui.theme.ParsTextPrimary
import com.example.vpn.VpnStatus

@Composable
fun HeaderBar(
    vpnStatus: VpnStatus,
    onAddClick: () -> Unit,
    onClipboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Action Buttons: Clipboard & Add (+)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Add Subscription Button (+)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ParsNavyCard)
                    .border(1.dp, ParsNavyCardBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onAddClick)
                    .testTag("header_add_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "افزودن اشتراک",
                    tint = ParsCyanNeon,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Clipboard Paste Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ParsNavyCard)
                    .border(1.dp, ParsNavyCardBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onClipboardClick)
                    .testTag("header_clipboard_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = "چسباندن از کلیپبورد",
                    tint = ParsTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Right Branding & Protection Status
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Pars Ping",
                    color = ParsTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor = when (vpnStatus) {
                        VpnStatus.CONNECTED -> ParsConnectedGreen
                        VpnStatus.CONNECTING -> ParsCyanNeon
                        else -> ParsTextMuted
                    }
                    val statusLabel = when (vpnStatus) {
                        VpnStatus.CONNECTED -> "محافظت فعال"
                        VpnStatus.CONNECTING -> "در حال ایمنسازی"
                        VpnStatus.FAILED -> "خطا در اتصال"
                        else -> "غیرفعال"
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Modern Cyber Shield Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ParsElectricBlue.copy(alpha = 0.35f),
                                ParsNavyCard
                            )
                        )
                    )
                    .border(1.2.dp, ParsCyanNeon.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Pars Ping Shield",
                    tint = ParsCyanNeon,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
