package com.sksulai.checksite.ui

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.sksulai.checksite.db.WorkerModel
import com.sksulai.checksite.db.WorkerRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject


@HiltViewModel class MainScreenViewModel @Inject constructor(
    private val repo: WorkerRepo
) : ViewModel() {
    fun getAll() = repo.getAll()

    suspend fun start(work: WorkerModel) = repo.start(work)
    suspend fun stop(work: WorkerModel)  = repo.stop(work)

    suspend fun create(
        name: String,
        description: String,
        frequency: Duration,
        url: Uri,
    ) = repo.create(name, description, frequency, url)

    suspend fun delete(work: WorkerModel) = repo.delete(work)
}

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
) @Composable fun MainScreen(
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val content = @Composable { Scaffold(
        bottomBar = { BottomAppBar { } },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { scope.launch { sheetState.show() } },
                content = { Icon(Icons.Default.Add, "Create checker") }
            )
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center
    ) {
        val tasks by viewModel.getAll().collectAsState(initial = emptyList())

        if(tasks.isEmpty()) Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text      = "We got no checkers",
                style     = MaterialTheme.typography.h3,
                textAlign = TextAlign.Center
            )
            Text(
                text      = "Create one by pressing the '+' button",
                textAlign = TextAlign.Center
            )
        } else LazyColumn {
            items(tasks) { item ->
                ListItem(
                    text = { Text(item.name) },
                    secondaryText = { Text(item.description) },
                    trailing = { Row {
                        IconToggleButton(
                            checked = item.running,
                            onCheckedChange = { scope.launch {
                                if(it) viewModel.start(item)
                                else viewModel.stop(item)
                            } }
                        ) {
                            if(item.running)
                                Icon(Icons.Default.Stop, "Stop checker")
                            else Icon(Icons.Default.PlayArrow, "Start checked")
                        }
                        IconButton(onClick = { scope.launch {
                            viewModel.delete(item)
                        }}) {
                            Icon(Icons.Default.Delete, "Delete checker")
                        }
                    } }
                )
            }
        }
    } }

    val sheetContent: @Composable ColumnScope.() -> Unit = { Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val name        = rememberFieldState()
        val description = rememberFieldState()
        val url         = rememberFieldState()
        var freq        by remember { mutableStateOf(Duration.ofDays(1)) }

        TextField(
            modifier      = Modifier.padding(top = 16.dp, bottom = 4.dp),
            label         = { Text("Checker name") },
            value         = name.text,
            onValueChange = { name.text = it },
            singleLine    = true,
            isError       = !name.valid
        )
        if(!name.valid) Text(name.error)

        TextField(
            modifier      = Modifier.padding(top = 4.dp, bottom = 4.dp),
            label         = { Text("Description") },
            value         = description.text,
            onValueChange = { description.text = it },
            singleLine    = true,
            isError       = !description.valid
        )
        if(!description.valid) Text(description.error)

        TextField(
            modifier      = Modifier.padding(top = 4.dp, bottom = 8.dp),
            label         = { Text("URL") },
            value         = url.text,
            onValueChange = { url.text = it },
            singleLine    = true,
            isError       = !url.valid
        )
        if(!url.valid) Text(url.error)

        DurationField(
            value = freq,
            onValueChange = { freq = it },
            label =  { Text("Frequency") }
        )

        Button(
            content = { Text("Create") },
            onClick = { scope.launch {
                // TODO: Validate input
                viewModel.create(
                    name.text,
                    description.text,
                    freq,
                    Uri.parse(url.text)
                )
                sheetState.hide()
            } }
        )

    } }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = sheetContent,
        content = content
    )
}
