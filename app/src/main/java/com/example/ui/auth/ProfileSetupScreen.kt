package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.ProfileRole
import com.example.model.UserProfile
import com.example.ui.theme.LocalFestaColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    initialProfile: UserProfile?,
    authDisplayName: String,
    authEmail: String,
    isSaving: Boolean,
    onSaveProfile: (
        displayName: String,
        email: String,
        familyOrHostName: String,
        phoneWhatsApp: String,
        profileRole: ProfileRole
    ) -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val festaColors = LocalFestaColors.current
    val initialName = initialProfile?.displayName?.takeIf { it.isNotBlank() } ?: authDisplayName
    val resolvedEmail = initialProfile?.email?.takeIf { it.isNotBlank() } ?: authEmail

    var displayName by remember(initialProfile?.id, authDisplayName) { mutableStateOf(initialName) }
    var familyOrHostName by remember(initialProfile?.id) {
        mutableStateOf(initialProfile?.familyOrHostName.orEmpty())
    }
    var phoneWhatsApp by remember(initialProfile?.id) {
        mutableStateOf(initialProfile?.phoneWhatsApp.orEmpty())
    }
    var selectedRole by remember(initialProfile?.id) {
        mutableStateOf(initialProfile?.role ?: ProfileRole.PARENT)
    }

    val canSubmit = displayName.trim().length >= 2 && !isSaving

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, festaColors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Criar Perfil de Acesso",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = resolvedEmail.ifBlank { "Conta Google conectada" },
                            style = MaterialTheme.typography.bodySmall,
                            color = festaColors.indigoText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = festaColors.emeraldContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = festaColors.emeraldText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Autenticação concluída! Complete seu perfil abaixo para salvar na base de dados e liberar o painel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = festaColors.emeraldText
                        )
                    }
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Seu nome completo *") },
                    placeholder = { Text("Ex: Ana Paula Mendes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_display_name_input")
                )

                OutlinedTextField(
                    value = familyOrHostName,
                    onValueChange = { familyOrHostName = it },
                    label = { Text("Assinatura padrão nos convites (opcional)") },
                    placeholder = { Text("Ex: Com carinho, Família Mendes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_family_name_input")
                )

                OutlinedTextField(
                    value = phoneWhatsApp,
                    onValueChange = { phoneWhatsApp = it },
                    label = { Text("WhatsApp de contato (opcional)") },
                    placeholder = { Text("Ex: 11 99999-0000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_phone_input")
                )

                Text(
                    text = "Perfil de atuação",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProfileRole.entries.forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role.label) }
                        )
                    }
                }

                Button(
                    onClick = {
                        onSaveProfile(
                            displayName,
                            resolvedEmail,
                            familyOrHostName,
                            phoneWhatsApp,
                            selectedRole
                        )
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("create_profile_submit_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvando perfil...")
                    } else {
                        Text("Criar Perfil e Acessar Painel")
                    }
                }

                TextButton(
                    onClick = onSignOutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trocar conta Google")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileModal(
    profile: UserProfile?,
    fallbackEmail: String,
    onDismiss: () -> Unit,
    onConfirmSave: (
        displayName: String,
        email: String,
        familyOrHostName: String,
        phoneWhatsApp: String,
        profileRole: ProfileRole
    ) -> Unit
) {
    val resolvedEmail = profile?.email?.takeIf { it.isNotBlank() } ?: fallbackEmail
    var displayName by remember(profile?.id) { mutableStateOf(profile?.displayName.orEmpty()) }
    var familyOrHostName by remember(profile?.id) { mutableStateOf(profile?.familyOrHostName.orEmpty()) }
    var phoneWhatsApp by remember(profile?.id) { mutableStateOf(profile?.phoneWhatsApp.orEmpty()) }
    var selectedRole by remember(profile?.id) { mutableStateOf(profile?.role ?: ProfileRole.PARENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Meu Perfil",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (resolvedEmail.isNotBlank()) {
                    Text(
                        text = "Conta: $resolvedEmail",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalFestaColors.current.mutedText
                    )
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nome completo *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = familyOrHostName,
                    onValueChange = { familyOrHostName = it },
                    label = { Text("Assinatura padrão nos convites") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneWhatsApp,
                    onValueChange = { phoneWhatsApp = it },
                    label = { Text("WhatsApp de contato") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Perfil de atuação",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProfileRole.entries.forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmSave(
                        displayName,
                        resolvedEmail,
                        familyOrHostName,
                        phoneWhatsApp,
                        selectedRole
                    )
                },
                enabled = displayName.trim().length >= 2
            ) {
                Text("Salvar Perfil")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
