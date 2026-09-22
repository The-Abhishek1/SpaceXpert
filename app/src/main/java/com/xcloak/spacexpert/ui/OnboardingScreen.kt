package com.xcloak.spacexpert.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcloak.spacexpert.ui.components.AdvancedGlassCard
import com.xcloak.spacexpert.ui.components.SpaceButton
import com.xcloak.spacexpert.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: String,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val colors = LocalSpaceXpertColors.current
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            title = "Welcome to SpaceXpert",
            description = "Embark on a journey to optimize and secure your device's storage across the cosmos.",
            icon = "star",
            color = CyanAccent
        ),
        OnboardingPage(
            title = "Cosmic Cleaning",
            description = "Scan and eliminate space debris, cache files, and residual junk hiding in deep space.",
            icon = "cleaning",
            color = NebulaPurple
        ),
        OnboardingPage(
            title = "Galaxy Explorer",
            description = "Navigate through your files and large documents with an advanced, intuitive cosmic interface.",
            icon = "folder",
            color = CometBlue
        ),
        OnboardingPage(
            title = "Quantum Vault",
            description = "Secure your sensitive data and private files inside an encrypted, biometric-protected vault.",
            icon = "lock",
            color = MarsRed
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // Skip button top right
                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onFinished() }
                        .padding(8.dp)
                )
            }

            // Pager for screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AdvancedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (pageIndex == 0) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.xcloak.spacexpert.R.drawable.spacexpert_logo_lockup_transparent),
                                contentDescription = "SpaceXpert Logo",
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(120.dp)
                                    .align(Alignment.CenterHorizontally),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        } else {
                            val iconVector = when (page.icon) {
                                "star" -> Icons.Filled.Star
                                "cleaning" -> Icons.Filled.CleaningServices
                                "folder" -> Icons.Filled.Folder
                                "lock" -> Icons.Filled.Lock
                                else -> Icons.Filled.Star
                            }

                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(96.dp)
                                    .align(Alignment.CenterHorizontally),
                                tint = page.color
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = page.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                repeat(4) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) CyanAccent 
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Next / Get Started button
            val isLastPage = pagerState.currentPage == pages.size - 1
            SpaceButton(
                text = if (isLastPage) "Get Started" else "Next",
                onClick = {
                    if (isLastPage) {
                        onFinished()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }
    }
}
