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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VpnConfigEntity
import com.example.ui.theme.ParsConnectedGreen
import com.example.ui.theme.ParsCyanNeon
import com.example.ui.theme.ParsElectricBlue
import com.example.ui.theme.ParsErrorRed
import com.example.ui.theme.ParsNavyCard
import com.example.ui.theme.ParsNavyCardBorder
import com.example.ui.theme.ParsNavyDark
import com.example.ui.theme.ParsNavySurface
import com.example.ui.theme.ParsTextMuted
import com.example.ui.theme.ParsTextPrimary
import com.example.ui.theme.ParsTextSecondary
import com.example.ui.theme.ParsWarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectorSheet(
    configs: List<VpnConfigEntity>,
    selectedConfig: VpnConfigEntity?,
    isTestingPingForId: Long?,
    onSelectConfig: (Long) -> Unit,
    onTestPing: (VpnConfigEntity) -> Unit,
    onOpenAddSubscription: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ParsNavySurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ParsNavyCardBorder)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = ParsTextSecondary
                    )
                }

                Text(
                    text = "انتخاب سرور",
                    color = ParsTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onOpenAddSubscription) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "افزودن اشتراک",
                        tint = ParsCyanNeon
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (configs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = ParsTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "هیچ سروری اضافه نشده است",
                        color = ParsTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenAddSubscription,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParsElectricBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "افزودن اشتراک جدید")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(configs, key = { it.id }) { config ->
                        val isSelected = config.id == selectedConfig?.id
                        val isTestingThis = isTestingPingForId == config.id

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) ParsNavyCard else ParsNavyDark)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) ParsCyanNeon else ParsNavyCardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectConfig(config.id) }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("server_item_${config.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) ParsCyanNeon.copy(alpha = 0.2f)
                                                else ParsNavyCardBorder.copy(alpha = 0.5f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Dns,
                                            contentDescription = null,
                                            tint = if (isSelected) ParsCyanNeon else ParsTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = config.name,
                                            color = ParsTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(ParsElectricBlue.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = config.protocol,
                                                    color = ParsCyanNeon,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = "${config.serverAddress}:${config.serverPort}",
                                                color = ParsTextMuted,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Ping test button
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isTestingThis) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = ParsCyanNeon,
                                            strokeWidth = 2.dp
                                        )
                                    } else if (config.latencyMs != null) {
                                        val color = when {
                                            config.latencyMs < 120 -> ParsConnectedGreen
                                            config.latencyMs < 250 -> ParsWarningAmber
                                            else -> ParsErrorRed
                                        }
                                        Text(
                                            text = "${config.latencyMs} ms",
                                            color = color,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(
                                        onClick = { onTestPing(config) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NetworkCheck,
                                            contentDescription = "تست پینگ",
                                            tint = ParsCyanNeon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
