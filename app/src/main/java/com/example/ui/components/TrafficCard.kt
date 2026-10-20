package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ParsConnectedGreen
import com.example.ui.theme.ParsCyanNeon
import com.example.ui.theme.ParsNavyCard
import com.example.ui.theme.ParsNavyCardBorder
import com.example.ui.theme.ParsTextMuted
import com.example.ui.theme.ParsTextPrimary
import com.example.vpn.VpnConnectionInfo

@Composable
fun TrafficMetricsCard(
    connectionInfo: VpnConnectionInfo,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ParsNavyCard)
            .border(1.dp, ParsNavyCardBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricItem(
            icon = Icons.Default.ArrowDownward,
            iconTint = ParsConnectedGreen,
            label = "دانلود",
            value = connectionInfo.formattedDownload
        )

        Box(
            modifier = Modifier
                .size(width = 1.dp, height = 32.dp)
                .background(ParsNavyCardBorder)
        )

        MetricItem(
            icon = Icons.Default.ArrowUpward,
            iconTint = ParsCyanNeon,
            label = "آپلود",
            value = connectionInfo.formattedUpload
        )

        Box(
            modifier = Modifier
                .size(width = 1.dp, height = 32.dp)
                .background(ParsNavyCardBorder)
        )

        MetricItem(
            icon = Icons.Default.Speed,
            iconTint = ParsCyanNeon,
            label = "پروتکل",
            value = connectionInfo.protocol ?: "—"
        )
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = label,
                color = ParsTextMuted,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = ParsTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
