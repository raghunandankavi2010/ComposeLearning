package com.example.composelearning.protobufdemo

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.proto.ContactList
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/** A contact mapped from the generated protobuf [com.example.composelearning.proto.Contact]. */
@Immutable
data class ContactUi(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val active: Boolean
)

@Immutable
sealed interface ContactsUiState {
    data object Idle : ContactsUiState
    data object Loading : ContactsUiState
    data class Success(
        val contacts: List<ContactUi>,
        /** Size of the protobuf payload we decoded — shown so the saving is visible. */
        val payloadBytes: Int
    ) : ContactsUiState

    data class Error(val message: String) : ContactsUiState
}

/**
 * Fetches the protobuf bytes from the desktop server and decodes them with the
 * generated [ContactList.parseFrom]. The ONLY protobuf-specific line is the
 * parseFrom call — everything else is ordinary HTTP.
 */
class ContactsRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
) {
    data class Result(val contacts: List<ContactUi>, val payloadBytes: Int)

    suspend fun fetchContacts(baseUrl: String): Result = withContext(Dispatchers.IO) {
        val url = baseUrl.trimEnd('/') + "/contacts"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Server returned HTTP ${response.code}")
            }
            val bytes = response.body?.bytes()
                ?: throw IOException("Empty response body")

            // ---- the protobuf decode: bytes -> strongly typed message ----
            val contactList = ContactList.parseFrom(bytes)

            val contacts = contactList.contactsList.map { c ->
                ContactUi(
                    id = c.id,
                    name = c.name,
                    email = c.email,
                    phone = c.phone,
                    role = c.role.name.replace("ROLE_UNSPECIFIED", "Unknown")
                        .lowercase().replaceFirstChar { it.uppercase() },
                    active = c.active
                )
            }
            Result(contacts, bytes.size)
        }
    }
}

class ProtobufContactsViewModel(
    private val repository: ContactsRepository = ContactsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun load(baseUrl: String) {
        _uiState.value = ContactsUiState.Loading
        viewModelScope.launch {
            runCatching { repository.fetchContacts(baseUrl) }
                .onSuccess { _uiState.value = ContactsUiState.Success(it.contacts, it.payloadBytes) }
                .onFailure {
                    _uiState.value = ContactsUiState.Error(it.message ?: "Failed to reach server")
                }
        }
    }
}
