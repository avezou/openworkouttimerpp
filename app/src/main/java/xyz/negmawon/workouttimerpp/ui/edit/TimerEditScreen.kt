package xyz.negmawon.workouttimerpp.ui.edit

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.negmawon.workouttimerpp.R
import xyz.negmawon.workouttimerpp.data.repo.TimerRepo
import xyz.negmawon.workouttimerpp.ui.components.Stepper
import xyz.negmawon.workouttimerpp.ui.components.TimeStepperRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerEditScreen(
    timerId: String,
    onDone: () -> Unit,
) {
    val ctx = LocalContext.current
    val vm: TimeEditViewModel = viewModel(factory = TimeEditViewModel.factory(ctx, timerId))
    val form by vm.form.collectAsState()

    val repo = remember { TimerRepo.get(ctx) }

    var showNameDialog by remember { mutableStateOf(false) }
    var nameListTemp by remember { mutableStateOf("") }


    val timers by repo.timers.collectAsState(initial = null)
    val editing = timers?.find { it.id == timerId }
    val isNew = editing == null
    val scope = rememberCoroutineScope()
    val existingNames = timers?.mapNotNull { it.name.trim().lowercase() }
    val trimmedName = form.name.trim()
    val isDuplicate = existingNames?.contains(trimmedName.lowercase()) == true &&
            trimmedName.lowercase() != editing?.name?.trim()?.lowercase()
    val isNameValid = trimmedName.isNotBlank() && !isDuplicate
    val nameFocus = remember { FocusRequester() }
    var warmMin by rememberSaveable { mutableIntStateOf(0) }
    var warmSec by rememberSaveable { mutableIntStateOf(30) }

    var setMin by rememberSaveable { mutableIntStateOf(0) }
    var setSec by rememberSaveable { mutableIntStateOf(30) }

    var restRepMin by rememberSaveable { mutableIntStateOf(0) }
    var restRepSec by rememberSaveable { mutableIntStateOf(30) }

    var restSetMin by rememberSaveable { mutableIntStateOf(0) }
    var restSetSec by rememberSaveable { mutableIntStateOf(30) }

    var coolMin by rememberSaveable { mutableIntStateOf(0) }
    var coolSec by rememberSaveable { mutableIntStateOf(30) }

    LaunchedEffect(editing) {
        editing ?: return@LaunchedEffect          // new timer, nothing to load

        // update only if user hasn’t already modified the field
        warmMin = editing.warmUp / 60
        warmSec = editing.warmUp % 60
        setMin = editing.setDuration / 60
        setSec = editing.setDuration % 60
        restRepMin = editing.restBetweenReps / 60
        restRepSec = editing.restBetweenReps % 60
        restSetMin = editing.restBetweenSets / 60
        restSetSec = editing.restBetweenSets % 60
        coolMin = editing.coolDown / 60
        coolSec = editing.coolDown % 60

        // also name / sets / reps if you store them locally
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isNew) stringResource(R.string.new_timer) else stringResource(R.string.edit_timer),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cancel
                FloatingActionButton(
                    onClick = onDone,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(start = 24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                }

                Row {
                    // Delete (only if editing)
                    if (!isNew) {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    editing.let {
                                        repo.delete(it.id)
                                        onDone()
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }

                    // Save
                    FloatingActionButton(
                        onClick = {
                            if (saveTimer(form, nameFocus, trimmedName, ctx, warmMin, warmSec, setMin,
                                    setSec, restRepMin, restRepSec, restSetMin, restSetSec, coolMin, coolSec, vm
                                )
                            ) return@FloatingActionButton

                            scope.launch {
                                vm.save(
                                    onDone
                                )
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.save_button),
                            contentDescription = stringResource(R.string.save)
                        )
                    }
                }
            }
        }

    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = form.name.trim(),
                onValueChange = vm::setName,
                label = { Text(stringResource(R.string.name)) },
                isError = !isNameValid,
                supportingText = {
                    if (trimmedName.isBlank()) {
                        Text(stringResource(R.string.name_is_required))
                    } else if (isDuplicate) {
                        Text(stringResource(R.string.this_name_already_exists))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .focusRequester(nameFocus),
                maxLines = 1,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Spacer(Modifier.height(12.dp))
            TimeStepperRow(stringResource(R.string.warm_up_s), warmMin, warmSec, { warmMin = it }, { warmSec = it })
            TimeStepperRow(stringResource(R.string.set_duration_s), setMin, setSec, { setMin = it }, { setSec = it })
            TimeStepperRow(stringResource(R.string.rest_between_reps_s), restRepMin, restRepSec, { restRepMin = it }, { restRepSec = it })
            TimeStepperRow(stringResource(R.string.rest_between_sets_s), restSetMin, restSetSec, { restSetMin = it }, { restSetSec = it })
            Stepper(stringResource(R.string.number_of_sets), form.sets, 1..50, largeRow = true, vm::setSets)
            val filledNames = remember(form.exerciseNames) {
                form.exerciseNames.split(',').count { it.trim().isNotEmpty() }
            }
            val namesMissing = (form.sets - filledNames).coerceAtLeast(0)
            OutlinedTextField(
                value = form.exerciseNames,
                onValueChange = {  },
                label = { Text(stringResource(R.string.exercise_names)) },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        nameListTemp = form.exerciseNames
                        showNameDialog = true
                    }) {
                        Icon(
                            imageVector = if (namesMissing > 0) Icons.Default.Add else Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_list)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        nameListTemp = form.exerciseNames
                        showNameDialog = true
                    }
            )
            Stepper(stringResource(R.string.number_of_reps), form.reps, 1..50, largeRow = true ,vm::setReps)
            TimeStepperRow(stringResource(R.string.cool_down_s), coolMin, coolSec, { coolMin = it }, { coolSec = it })
            Spacer(Modifier.height(64.dp))
        }
    }

    if (showNameDialog) {
        ExerciseNameDialog(
            sets = form.sets,                         // current set count
            initialNames = form.exerciseNames,            // current comma string
            onDone = { txt ->                    // save back
                form.exerciseNames = txt
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }
}

@Composable
fun ExerciseNameDialog(
    sets: Int,
    initialNames: String,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Convert the initial comma-separated string to a mutable list
    val initialList = remember(initialNames, sets) {
        val parsed = initialNames.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        // Ensure list size == sets (fill extras with blank)
        (parsed + List(sets) { "" }).take(sets).toMutableStateList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Build the final string (comma-separated, no empties at the end)
                val result = initialList.joinToString(", ") { it.trim() }
                    .trim().trimEnd(',')
                onDone(result)
            }) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Exercise names") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                repeat(sets) { index ->
                    OutlinedTextField(
                        value = initialList[index],
                        onValueChange = { initialList[index] = it },
                        label = { Text("Set ${index + 1}") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    )
}


private fun saveTimer(
    form: TimerForm,
    nameFocus: FocusRequester,
    trimmedName: String,
    ctx: Context,
    warmMin: Int,
    warmSec: Int,
    setMin: Int,
    setSec: Int,
    restRepMin: Int,
    restRepSec: Int,
    restSetMin: Int,
    restSetSec: Int,
    coolMin: Int,
    coolSec: Int,
    vm: TimeEditViewModel
): Boolean {
    if (form.name.trim().isBlank()) {
        nameFocus.requestFocus()
        return true
    }
    val finalName = trimmedName.ifBlank { ctx.getString(R.string.untitled_timer) }
    val finalSets = if (form.sets <= 0) 1 else form.sets
    val warmUp = warmMin * 60 + warmSec
    val setDur = setMin * 60 + setSec
    val restReps = restRepMin * 60 + restRepSec
    val restSets = restSetMin * 60 + restSetSec
    val coolDown = coolMin * 60 + coolSec
    vm.setWarmUp(warmUp)
    vm.setSetDuration(setDur)
    vm.setRestReps(restReps)
    vm.setRestSets(restSets)
    vm.setCoolDown(coolDown)
    vm.setExerciseNames(form.exerciseNames)
    vm.setSets(finalSets)
    vm.setName(finalName)
    return false
}
