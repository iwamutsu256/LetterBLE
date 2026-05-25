package com.example.letterble.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onReceivedClicked: () -> Unit,
    onCarryClicked: () -> Unit,
    onCreateLetterClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                HomeNavigationEvent.NavigateToReceived -> onReceivedClicked()
                HomeNavigationEvent.NavigateToCarry -> onCarryClicked()
                HomeNavigationEvent.NavigateToEditLetter -> onCreateLetterClicked()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ホーム",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = viewModel::onReceivedClicked
        ) {
            Text("受信した手紙")
        }
        Button(
            modifier = Modifier.padding(top = 8.dp),
            onClick = viewModel::onCarryClicked
        ) {
            Text("運搬中の手紙")
        }
        Button(
            modifier = Modifier.padding(top = 8.dp),
            onClick = viewModel::onCreateLetterClicked
        ) {
            Text("手紙を書く")
        }
    }
}
