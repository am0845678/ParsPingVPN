package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ParsButtonBlue
import com.example.ui.theme.ParsConnectedGreen
import com.example.ui.theme.ParsCyanNeon
import com.example.ui.theme.ParsElectricBlue
import com.example.ui.theme.ParsErrorRed
import com.example.ui.theme.ParsNavyCard
import com.example.ui.theme.ParsNavyCardBorder
import com.example.ui.theme.ParsTextMuted
import com.example.ui.theme.ParsTextPrimary
import com.example.vpn.VpnStatus

@Composable
fun ConnectButton(
    status: VpnStatus,
    formattedDuration: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = status == VpnStatus.CONNECTED
    val isConnecting = status == VpnStatus.CONNECTING
    val isFailed = status == VpnStatus.FAILED

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_rings")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected || isConnecting) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isConnected || isConnecting) 0.5f else 0.15f,
        targetValue = if (isConnected || isConnecting) 0.1f else 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status text above button
        val statusTitle = when (status) {
            VpnStatus.CONNECTED -> "متصل"
            VpnStatus.CONNECTING -> "در حال اتصال..."
            VpnStatus.DISCONNECTING -> "در حال قطع..."
            VpnStatus.FAILED -> "خطا در اتصال"
            VpnStatus.DISCONNECTED -> "قطع شده"
        }

        val statusColor = when (status) {
            VpnStatus.CONNECTED -> ParsConnectedGreen
            VpnStatus.CONNECTING -> ParsCyanNeon
            VpnStatus.FAILED -> ParsErrorRed
            else -> ParsTextMuted
        }

        Text(
            text = statusTitle,
            color = statusColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("vpn_status_text")
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Large Glowing Connect Button with Ripple Rings
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer Pulse Ring
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        when {
                            isConnected -> ParsConnectedGreen.copy(alpha = pulseAlpha)
                            isConnecting -> ParsCyanNeon.copy(alpha = pulseAlpha)
                            isFailed -> ParsErrorRed.copy(alpha = pulseAlpha)
                            else -> ParsElectricBlue.copy(alpha = pulseAlpha)
                        }
                    )
            )

            // Middle Subtle Ring
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = when {
                            isConnected -> ParsConnectedGreen.copy(alpha = 0.4f)
                            isConnecting -> ParsCyanNeon.copy(alpha = 0.4f)
                            else -> ParsNavyCardBorder
                        },
                        shape = CircleShape
                    )
            )

            // Inner Main Circular Button (160dp)
            val buttonGradient = when {
                isConnected -> Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00C853),
                        Color(0xFF007E33)
                    )
                )
                isConnecting -> Brush.verticalGradient(
                    colors = listOf(
                        ParsCyanNeon,
                        ParsElectricBlue
                    )
                )
                isFailed -> Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF5252),
                        Color(0xFFB71C1C)
                    )
                )
                else -> Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5),
                        Color(0xFF0D47A1)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(buttonGradient)
                    .border(
                        width = 3.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable(onClick = onClick)
                    .testTag("main_connect_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = Color.White,
                        strokeWidth = 3.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "اتصال یا قطع VPN",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Connection Timer (00:00:00)
        Text(
            text = formattedDuration,
            color = if (isConnected) ParsTextPrimary else ParsTextMuted,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier.testTag("connection_timer_text")
        )
    }
}
