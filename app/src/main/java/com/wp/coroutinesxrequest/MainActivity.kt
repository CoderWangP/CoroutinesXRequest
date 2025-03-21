package com.wp.coroutinesxrequest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wp.coroutinesxrequest.http.Status
import com.wp.coroutinesxrequest.sample.MainViewModelPlan
import com.wp.coroutinesxrequest.ui.theme.CoroutinesXRequestTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModelPlan>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoroutinesXRequestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val singleResponse by viewModel.singleResponse.observeAsState()
                    val multResponse1 by viewModel.response1.observeAsState()
                    val multResponse2 by viewModel.response2.observeAsState()
                    val zipResponse1 by viewModel.zipResponse1.observeAsState()
                    val zipResponse2 by viewModel.zipResponse2.observeAsState()
                    val requestStatus by viewModel.requestStatus.collectAsStateWithLifecycle(lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current)
                    RequestActionAndContent(modifier = Modifier.padding(innerPadding),
                        singleResponse = singleResponse,
                        multResponse1 = multResponse1,
                        multResponse2 = multResponse2,
                        zipResponse1 = zipResponse1,
                        zipResponse2 = zipResponse2,
                        requestStatus = requestStatus,
                        onSingleClick = {
                            viewModel.requestSingle()
                        },
                        onMultClick = {
                            viewModel.requestMultiple()
                        },
                        onZipClick = {
                            viewModel.requestMultipleZip()
                        })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RequestActionAndContentPreview() {
    RequestActionAndContent(
        singleResponse = "Single response",
        multResponse1 = "multResponse1",
        multResponse2 = "multResponse2",
        zipResponse1 = "zipResponse1",
        zipResponse2 = "zipResponse2"
    )
}

@Composable
fun RequestActionAndContent(
    modifier: Modifier = Modifier,
    singleResponse: String? = "",
    multResponse1: String? = "",
    multResponse2: String? = "",
    zipResponse1: String? = "",
    zipResponse2: String? = "",
    requestStatus: Status? = Status.NONE,
    onSingleClick: () -> Unit = {},
    onMultClick: () -> Unit = {},
    onZipClick: () -> Unit = {}
) {

    Column(
        modifier = modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopCenter)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
            onClick = onSingleClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            )
        ) {
            Text(text = "launch single request")
        }
        Text(
            text = "response data:${requestStatus?.name}",
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
        Text(
            text = singleResponse ?: "",
            color = Color.Red,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )

        Button(
            modifier = Modifier
                .padding(top = 20.dp)
                .align(alignment = Alignment.CenterHorizontally),
            onClick = onMultClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            )
        ) {
            Text(text = "launch multiple request")
        }
        Text(
            text = "response data:${requestStatus?.name}",
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
        Text(
            text = multResponse1 ?: "",
            color = Color.Red,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
        Text(
            modifier = Modifier
                .padding(top = 10.dp)
                .align(alignment = Alignment.CenterHorizontally),
            text = multResponse2 ?: "",
            color = Color.Red
        )

        Button(
            modifier = Modifier
                .padding(top = 20.dp)
                .align(alignment = Alignment.CenterHorizontally),
            onClick = onZipClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            )
        ) {
            Text(text = "launch zip request")
        }
        Text(
            text = "response data:${requestStatus?.name}",
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
        Text(
            text = zipResponse1 ?: "",
            color = Color.Red,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
        Text(
            modifier = Modifier
                .padding(top = 10.dp)
                .align(alignment = Alignment.CenterHorizontally),
            text = zipResponse2 ?: "",
            color = Color.Red
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CoroutinesXRequestTheme {
        Greeting("Android")
    }
}