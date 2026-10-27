package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GamificationConfig
import com.example.ui.HabitUiState
import com.example.ui.components.BadgeItem
import com.example.ui.components.LevelProgressCard
import com.example.ui.components.ShareHelper

@Composable
fun RewardsScreen(
    uiState: HabitUiState,
    onBuyFreeze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val unlockedBadgeIds = uiState.userProfile?.badges ?: emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "GAMIFIED REWARDS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Level & Achievements",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Level and XP Card
        item {
            LevelProgressCard(
                userProfile = uiState.userProfile,
                onBuyFreeze = onBuyFreeze
            )
        }

        // Gamification rules explanation card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "HOW REWARDS WORK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RewardRuleRow("⚡ Habit Checkoff", "+20 XP per completed habit")
                    RewardRuleRow("🌟 Perfect Day Bonus", "+50 XP bonus when completing all daily habits")
                    RewardRuleRow("🛡️ Streak Freeze", "Protects your streak if you miss a day (150 XP)")
                    RewardRuleRow("🎖️ Badges", "Permanent milestone accolades unlockable by consistency")
                }
            }
        }

        item {
            Text(
                text = "ACHIEVEMENT SHOWCASE (${unlockedBadgeIds.size}/${GamificationConfig.BADGES.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        items(GamificationConfig.BADGES, key = { it.id }) { badge ->
            val isUnlocked = unlockedBadgeIds.contains(badge.id)
            BadgeItem(
                badge = badge,
                isUnlocked = isUnlocked,
                onShareBadge = {
                    ShareHelper.shareBadgeUnlock(
                        context = context,
                        badgeTitle = badge.title,
                        badgeDesc = badge.description,
                        userProfile = uiState.userProfile
                    )
                }
            )
        }
    }
}

@Composable
private fun RewardRuleRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
