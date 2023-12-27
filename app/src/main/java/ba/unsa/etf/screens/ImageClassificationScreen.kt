package ba.unsa.etf.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ba.unsa.etf.generateDescription
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch

import ba.unsa.etf.getLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ImageClassificationScreen() {

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var description by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { result: Uri? ->
        if (result != null) {
            imageUri = result
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = rememberImagePainter(data = imageUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                .clickable {
                    launcher.launch("image/*")
                }
        )

        Button(
            onClick = {
                if (imageUri != null) {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val labels = getLabels(imageUri!!, context)
                            description = generateDescription(labels)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "What do you see?")
        }

        if(description != ""){
             Log.d("descripton", description)
        }

    }
}

