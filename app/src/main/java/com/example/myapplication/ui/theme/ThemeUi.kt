package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Standard page / scaffold background (follows light & dark scheme). */
@Composable
fun appPageBackground(): Color = MaterialTheme.colorScheme.background

/** Cards, sheets, app bars. */
@Composable
fun appCardSurface(): Color = MaterialTheme.colorScheme.surface

/** Muted blocks, rows, placeholders. */
@Composable
fun appMutedSurface(): Color = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun appHairline(): Color = MaterialTheme.colorScheme.outlineVariant
