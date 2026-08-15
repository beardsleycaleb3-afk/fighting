package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffectsEngine
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TapoutBrightRed
import com.example.ui.theme.TapoutCrimson

@Composable
fun RecoveryScreen(
    soundEngine: SoundEffectsEngine?,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "RECOVERY MODE",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 4.sp,
            color = TapoutBrightRed,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .shadow(elevation = 16.dp, ambientColor = TapoutCrimson, spotColor = TapoutBrightRed)
                .testTag("fallback_title")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "System diagnostics active. Tap below to reload the modular engine runtime cleanly and reset state.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                color = Color(0xBBFFFFFF),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                soundEngine?.playSelect()
                onReload()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp), ambientColor = TapoutCrimson, spotColor = TapoutBrightRed)
                .testTag("fallback_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = TapoutCrimson,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "RELOAD ENGINE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}

