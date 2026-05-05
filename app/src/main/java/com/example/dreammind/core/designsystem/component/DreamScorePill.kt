package com.example.dreammind.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dreammind.ui.theme.DreamPrimary
import com.example.dreammind.ui.theme.DreamPrimarySoft

@Composable
fun ScorePill(score: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DreamPrimary.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, DreamPrimary.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(DreamPrimary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$score/100",
                color = DreamPrimarySoft,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
