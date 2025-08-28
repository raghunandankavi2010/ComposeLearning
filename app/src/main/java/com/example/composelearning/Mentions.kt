import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Dummy data for suggestions
data class User(val id: String, val username: String, val fullName: String)
data class Hashtag(val name: String)

val users = listOf(
    User("1", "alice", "Alice Smith"),
    User("2", "bob", "Bob Johnson"),
    User("3", "charlie", "Charlie Brown")
)
val hashtags = listOf(
    Hashtag("compose"),
    Hashtag("androiddev"),
    Hashtag("kotlin")
)

@Composable
fun MentionTextField(
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(AnnotatedString(""))) }
    var suggestionPopupShown by remember { mutableStateOf(false) }
    var currentSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentTrigger by remember { mutableStateOf<Char?>(null) } // '@' or '#'
    var currentSearchTerm by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var searchJob: Job? by remember { mutableStateOf(null) }

    Column(modifier = modifier.fillMaxSize(),

        verticalArrangement = Arrangement.Center) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue // Update the value immediately

                val text = newValue.text
                val cursorPosition = newValue.selection.end

                // Find the word preceding the cursor
                val beforeCursor = text.substring(0, cursorPosition)
                // --- FIX APPLIED HERE ---
                val delimiters = listOf(' ', '\n', '\t')
                var lastDelimiterIndex = -1
                delimiters.forEach { delimiterChar ->
                    val index = beforeCursor.lastIndexOf(delimiterChar)
                    if (index > lastDelimiterIndex) {
                        lastDelimiterIndex = index
                    }
                }
                val wordStart = if (lastDelimiterIndex != -1) lastDelimiterIndex + 1 else 0
                val currentWord = beforeCursor.substring(wordStart)
                // ------------------------

                if (currentWord.startsWith("@") && currentWord.length > 1) {
                    currentTrigger = '@'
                    currentSearchTerm = currentWord.substring(1)
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(300) // Debounce search
                        val filteredUsers = users.filter { it.username.contains(currentSearchTerm, ignoreCase = true) }
                        currentSuggestions = filteredUsers.map { "@${it.username}" }
                        suggestionPopupShown = currentSuggestions.isNotEmpty()
                    }
                } else if (currentWord.startsWith("#") && currentWord.length > 1) {
                    currentTrigger = '#'
                    currentSearchTerm = currentWord.substring(1)
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(300) // Debounce search
                        val filteredHashtags = hashtags.filter { it.name.contains(currentSearchTerm, ignoreCase = true) }
                        currentSuggestions = filteredHashtags.map { "#${it.name}" }
                        suggestionPopupShown = currentSuggestions.isNotEmpty()
                    }
                } else {
                    currentTrigger = null
                    currentSearchTerm = ""
                    currentSuggestions = emptyList()
                    suggestionPopupShown = false
                    searchJob?.cancel()
                }

                // Apply styling to mentions and hashtags
                textFieldValue = TextFieldValue(
                    text = highlightMentionsAndHashtags(text).toString(),
                    selection = newValue.selection // Preserve the cursor position
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(100.dp),
            // Custom text style if needed
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
        )

        // Suggestion Popup
        if (suggestionPopupShown) {
            DropdownMenu(
                expanded = suggestionPopupShown,
                onDismissRequest = { suggestionPopupShown = false },
                modifier = Modifier.fillMaxWidth(0.8f) // Adjust width as needed
            ) {
                currentSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            val text = textFieldValue.text
                            val cursorPosition = textFieldValue.selection.end
                            val beforeCursor = text.substring(0, cursorPosition)
                            val lastTriggerIndex = beforeCursor.lastIndexOf(currentTrigger!!) // Assumed to be non-null here

                            if (lastTriggerIndex != -1) {
                                val newText = text.substring(0, lastTriggerIndex) +
                                        suggestion + " " + // Add a space after inserting
                                        text.substring(cursorPosition)

                                val newSelection = TextRange(lastTriggerIndex + suggestion.length + 1) // Move cursor past the inserted text + space

                                textFieldValue = TextFieldValue(
                                    text = highlightMentionsAndHashtags(newText).toString(),
                                    selection = newSelection
                                )
                            }
                            suggestionPopupShown = false
                            currentSuggestions = emptyList() // Clear suggestions after selection
                            currentTrigger = null // Reset trigger
                            currentSearchTerm = "" // Reset search term
                        }
                    )
                }
            }
        }
    }
}

// Helper function to apply styling
fun highlightMentionsAndHashtags(text: String): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        // Regex to find @mentions and #hashtags
        val mentionRegex = Regex("""@\w+""")
        val hashtagRegex = Regex("""#\w+""")

        mentionRegex.findAll(text).forEach { matchResult ->
            val (start, end) = matchResult.range.first to matchResult.range.last + 1
            withStyle(style = SpanStyle(color = Color.Blue, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                // Add an annotation to store the actual mention content if needed later
                addStringAnnotation("mention", matchResult.value.substring(1), start, end)
            }
        }

        hashtagRegex.findAll(text).forEach { matchResult ->
            val (start, end) = matchResult.range.first to matchResult.range.last + 1
            withStyle(style = SpanStyle(color = Color.Green, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                // Add an annotation to store the actual hashtag content if needed later
                addStringAnnotation("hashtag", matchResult.value.substring(1), start, end)
            }
        }
    }
}

@Composable
fun MyScreen() {
    Column {
        Text("Compose Mention Input Example", modifier = Modifier.padding(16.dp))
        MentionTextField(modifier = Modifier.fillMaxWidth())
    }
}