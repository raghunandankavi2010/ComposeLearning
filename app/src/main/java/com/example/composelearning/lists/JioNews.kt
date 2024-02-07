package com.example.composelearning.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.composelearning.ui.theme.ComposeLearningTheme


@Composable
fun JioNews() {
    Row(
        modifier = Modifier
            .padding(top=16.dp)
            .fillMaxWidth()
            .height(148.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = "https://example.com/image.jpg",
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .size(112.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomEnd = 8.dp,
                        bottomStart = 8.dp
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp),

        ) {

            TextHeaderFooter(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .weight(0.5f),
                text = "Your Text 1"
            )

            Text(
                text = "Your Text Content\n2\n3\n4",
                maxLines = 4,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, true)
            )

            TextHeaderFooter(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .padding(bottom = 16.dp),
                text = "Your Text 2"
            )

        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewImageWithRedDot() {
    ComposeLearningTheme {
        JioNews()
    }
}

@Composable
fun TextHeaderFooter(modifier: Modifier = Modifier, text: String) {
    Text(
        maxLines = 1,
        text = text,
        color = Color(0xFF7A7A7A),
        fontSize = 14.sp,
        modifier = modifier
    )
}