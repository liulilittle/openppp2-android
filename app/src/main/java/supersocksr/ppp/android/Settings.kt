package supersocksr.ppp.android

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink


const val TEST_LINK_KEY = "test_link"
const val TEST_LINK_DEFAULT = "http://cp.cloudflare.com"
const val TIMEOUT_KEY = "timeout"
const val TIMEOUT_DEFAULT = 3000


class Settings(val context: Context, val preferences: SharedPreferences) {
  private val groupPaddingValues = PaddingValues(8.dp)

  @Composable
  fun SettingsScreen() {
    val context = LocalContext.current
    val state = rememberLazyListState()
    var testOptionsnDialogOpened by remember { mutableStateOf(false) }
    var LogDialogOpened by remember { mutableStateOf(false) }

    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp),
      state = state
    ) {
      item {
        SettingsGroup(
          modifier = Modifier,
          title = { Text("Other Settings") },
          contentPadding = groupPaddingValues,
          enabled = true
        ) {
          SettingsMenuLink(
            title = { Text(context.getString(R.string.test_options)) },
            subtitle = { Text(context.getString(R.string.test_options_subtitle)) }
          ) {
            testOptionsnDialogOpened = true
          }

          SettingsMenuLink(
            title = { Text(context.getString(R.string.show_logs)) },
          ) {
            LogDialogOpened = true
          }
        }
        if (testOptionsnDialogOpened) {
          TestOptionsDialog { testOptionsnDialogOpened = false }
        }
        if (LogDialogOpened) {
          LogDialog { LogDialogOpened = false }
        }
      }
    }
  }

  @Composable
  fun TestOptionsDialog(onDismiss: () -> Unit) {
    var link by remember {
      mutableStateOf(
        TextFieldValue(
          preferences.getString(TEST_LINK_KEY, TEST_LINK_DEFAULT)!!
        )
      )
    }
    var timeout by remember {
      mutableStateOf(
        TextFieldValue(
          preferences.getInt(TIMEOUT_KEY, TIMEOUT_DEFAULT).toString()
        )
      )
    }
    val context = LocalContext.current
    val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current
    val dialogKeyboardActions =
      KeyboardActions(onDone = { localSoftwareKeyboardController?.hide() })
    val dialogKeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
    val dialogTextFieldModifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)

    AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = context.getString(R.string.test_options),
            modifier = Modifier
              .weight(1f)
              .padding(end = 8.dp),
            fontSize = 22.sp
          )
        }
      },
      text = {
        val lazyListState = rememberLazyListState()
        LazyColumn(state = lazyListState, contentPadding = PaddingValues(4.dp)) {
          item {
            OutlinedTextField(
              modifier = dialogTextFieldModifier,
              value = link,
              onValueChange = { link = it },
              label = { Text(context.getString(R.string.test_link)) },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = dialogKeyboardActions
            )
          }
          item {
            OutlinedTextField(
              modifier = dialogTextFieldModifier,
              value = timeout,
              onValueChange = { timeout = it },
              label = { Text(context.getString(R.string.test_timeout)) },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = dialogKeyboardActions
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            try {
              preferences.edit().putString(TEST_LINK_KEY, link.text.trim()).apply()
              preferences.edit().putInt(TIMEOUT_KEY, timeout.text.trim().toInt()).apply()
              onDismiss()
            } catch (e: Exception) {
              Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
          }
        ) {
          Text(context.getString(R.string.save))
        }
      },
      dismissButton = {
        Button(
          onClick = onDismiss
        ) {
          Text(context.getString(R.string.cancel))
        }
      }
    )
  }

  @Composable
  fun LogDialog(onDismiss: () -> Unit) {
    var logLines by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = { onDismiss() }) {
      Column(
        modifier = Modifier
          .padding(8.dp)
          .fillMaxSize()
          .verticalScroll(scrollState)
      ) {
        SelectionContainer { Text(text = logLines) }

        LaunchedEffect(Unit) {
          try {
            val process = Runtime.getRuntime().exec("logcat -d *:E")
            val bufferedReader = process.inputStream.bufferedReader()
            while (true) {
              val line = bufferedReader.readLine() ?: break
              logLines += "$line\n"
              scrollState.scrollTo(scrollState.maxValue)
            }
            bufferedReader.close()
          } catch (e: Exception) {
            Log.e("LogActivity", "Error reading logs", e)
          }
        }
      }
    }
  }
}