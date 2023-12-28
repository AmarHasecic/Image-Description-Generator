package ba.unsa.etf.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ba.unsa.etf.components.LabelConfidenceItem
import ba.unsa.etf.generateDescription
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch

import ba.unsa.etf.getLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Handler


private fun extractTextFromResponse(responseString: String): String {
    val startIndex = responseString.indexOf("\"text\": \"") + "\"text\": \"".length
    val endIndex = responseString.indexOf("\"", startIndex)

    if (startIndex in 0 until endIndex) {
        val text = responseString.substring(startIndex, endIndex)
        return text.replace("\\n\\n", "")
    }

    return "No description available"
}
fun extractLabelsAndConfidences(labelStrings: List<String>): Pair<List<String>, List<Float>> {
    val labels = mutableListOf<String>()
    val confidences = mutableListOf<Float>()

    labelStrings.forEach {
        val (label, confidenceStr) = it.split(" - ")
        labels.add(label)
        confidences.add(confidenceStr.toFloat())
    }

    return Pair(labels, confidences)
}


@Composable
fun ImageClassificationScreen() {

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var description by remember { mutableStateOf("") }
    var labelsExtracted by remember { mutableStateOf(emptyList<String>()) }
    var confidences by remember { mutableStateOf(emptyList<Float>()) }


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
        if (imageUri==null) {
            Text(
                text = "Click to upload image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                    .clickable {
                        launcher.launch("image/*")
                    }
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        } else {
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
        }

    Button(
            onClick = {
                if (imageUri != null) {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val labels = getLabels(imageUri!!, context)

                            val (extractedLabels, extractedConfidences) = extractLabelsAndConfidences(labels)
                            labelsExtracted = extractedLabels
                            confidences = extractedConfidences

                            description = generateDescription(labelsExtracted)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "What do you see?")
        }

        Column {

            if (description != "") {

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Text(
                            text = "Description",
                            fontSize = 35.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = extractTextFromResponse(description),
                            color = Color.White
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(30.dp))

            if (labelsExtracted.isNotEmpty()) {

                LazyColumn {
                    items(labelsExtracted.size) { index ->
                        LabelConfidenceItem(
                            label = labelsExtracted[index],
                            confidence = confidences.getOrElse(index) { 0f }
                        )
                    }
                }

            }

        }
    }
}

