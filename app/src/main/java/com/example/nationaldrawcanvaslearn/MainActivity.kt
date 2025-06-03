package com.example.nationaldrawcanvaslearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.room.Room
import com.example.nationaldrawcanvaslearn.ui.theme.NationalDrawCanvaslearnTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NationalDrawCanvaslearnTheme {
                val db = Room.databaseBuilder(this, AppDatabase::class.java, "db")
                    .fallbackToDestructiveMigration().build()
                val draw = db.drawDao()
                DrawCanvas(draw)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawCanvas(db: DrawDao) {
    val lines = remember { mutableStateListOf<Line>() }
    val colors = listOf(Color.Black, Color(0xff685FFF), Color(0xffFFD886), Color(0xffFF5185))
    var selectedColor by remember { mutableIntStateOf(0) }
    var selectedStroke by remember { mutableFloatStateOf(10f) }
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val draws by db.getAll().collectAsState(emptyList())
    var name by remember { mutableStateOf("untitled") }
    var id by remember { mutableIntStateOf(-1) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog)
        Dialog(
            onDismissRequest = { showDialog = false }
        ) {
            var text by remember { mutableStateOf("") }
            Column(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(10.dp)
            ) {
                Text("Rename")
                OutlinedTextField(value = text, onValueChange = { text = it })
                Button(onClick = {
                    name = text
                    showDialog = false
                }) {
                    Text("Save")
                }
            }
        }

    if (showBottomSheet)
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text("Draws", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    if (draws.isEmpty())
                        Text("Nothing is here")
                }
                items(draws) {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp, horizontal = 15.dp)
                            .clickable {
                                if (draws.isNotEmpty()) {
                                    id = it.id
                                    name = it.name
                                    lines.clear()
                                    lines.addAll(it.draw)
                                }
                            }
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(it.name, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                scope.launch {
                                    db.delete(it.id)
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 15.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                modifier = Modifier
                    .clickable {
                        showDialog = true
                    },
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {
                lines.clear()
                id = -1
                name = "untitled"
            }) { Icon(Icons.Default.Add, contentDescription = null) }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(true) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val line = Line(
                            start = change.position - dragAmount,
                            end = change.position,
                            color = colors.get(selectedColor), strokeWidth = selectedStroke
                        )
                        lines.add(line)
                    }
                },
        ) {
            lines.forEach { line ->
                drawLine(
                    color = line.color,
                    start = line.start,
                    end = line.end,
                    line.strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .systemBarsPadding(),
        ) {
            LazyRow(
                Modifier
                    .height(80.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                itemsIndexed(colors) { index, item ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(item)
                            .clickable {
                                selectedColor = index
                            }
                            .animateContentSize()
                            .size(if (selectedColor == index) 65.dp else 55.dp))
                }
            }
            LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                item {
                    IconButton(onClick = {
                        showBottomSheet = true
                    }) { Icon(Icons.Default.Menu, contentDescription = "") }
                }
                item {
                    IconButton(onClick = {
                        scope.launch {
                            if (id >= 0) db.update(id, lines)
                            else {
                                val returnId = db.insert(Draw(name = name, draw = lines))
                                id = returnId.toInt()
                            }
                        }
                    }) { Icon(Icons.Default.Done, contentDescription = "") }
                }
                item {
                    IconButton(onClick = {
                        lines.clear()
                    }) { Icon(Icons.Default.Delete, contentDescription = "") }
                }
                item {
                    Slider(
                        value = selectedStroke,
                        onValueChange = { selectedStroke = it },
                        modifier = Modifier.width(200.dp),
                        valueRange = 5f..50f
                    )
                }
            }
        }
    }
}

data class Line(
    val start: Offset,
    val end: Offset,
    val color: Color = Color.Black,
    val strokeWidth: Float = 20f
)