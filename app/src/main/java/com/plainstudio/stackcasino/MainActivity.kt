package com.plainstudio.stackcasino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plainstudio.stackcasino.engine.NativeGameEngine
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StackcasinoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    JniSmokeTest(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun JniSmokeTest(modifier: Modifier = Modifier) {
    val engine = remember { NativeGameEngine() }
    val coinflip = remember {
        engine.evaluateCoinflip(
            serverSeed = "test_server_seed",
            clientSeed = "test_client_seed",
            nonce = 1L,
        )
    }
    val crash = remember {
        engine.evaluateCrashPoint(
            serverSeed = "test_server_seed",
            clientSeed = "test_client_seed",
            nonce = 1L,
        )
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "JNI smoke test")
        Text(text = "Coinflip: $coinflip")
        Text(text = "Crash:    $crash")
    }
}

@Preview(showBackground = true)
@Composable
fun JniSmokeTestPreview() {
    StackcasinoTheme {
        Text(text = "Preview does not exercise JNI")
    }
}
