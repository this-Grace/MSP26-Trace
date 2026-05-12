package it.unibo.trace.ui.screen.auth.signup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.koin.androidx.compose.koinViewModel
import it.unibo.trace.R
import it.unibo.trace.ui.composable.button.SocialSignInButton
import it.unibo.trace.ui.composable.button.TraceButton
import it.unibo.trace.ui.composable.input.EmailField
import it.unibo.trace.ui.composable.input.PasswordField
import it.unibo.trace.ui.composable.separator.TraceSeparator

/**
 * Screen for new user registration.
 */
@Composable
fun SignUpScreen(
    navController: NavHostController,
    viewModel: SignUpViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.join_trace, stringResource(R.string.app_name)),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            EmailField(
                value = uiState.email,
                onValueChange = { viewModel.updateEmail(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = uiState.password,
                label = stringResource(R.string.password_label),
                placeholder = stringResource(R.string.password_placeholder),
                onValueChange = { viewModel.updatePassword(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = uiState.confirmPassword,
                label = stringResource(R.string.confirm_password_label),
                placeholder = stringResource(R.string.confirm_password_placeholder),
                onValueChange = { viewModel.updateConfirmPassword(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            TraceButton(
                text = stringResource(R.string.sign_up),
                isLoading = uiState.isLoading,
                onClick = { viewModel.signUp(onSuccess = {
                    navController.popBackStack()
                }) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TraceSeparator(text = stringResource(R.string.or_separator))

            Spacer(modifier = Modifier.height(16.dp))

            SocialSignInButton(
                text = stringResource(R.string.sign_in_github),
                iconRes = R.drawable.ic_github_logo,
                onClick = { viewModel.signUpWithGithub() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Text(
                    stringResource(R.string.already_have_account),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    stringResource(R.string.sign_in),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
