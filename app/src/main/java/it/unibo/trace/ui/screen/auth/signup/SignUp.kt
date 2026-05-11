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
                text = "Join " + stringResource(R.string.app_name),
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
                label = "Password",
                placeholder = "Insert Password",
                onValueChange = { viewModel.updatePassword(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = uiState.confirmPassword,
                label = "Confirm Password",
                placeholder = "Confirm Password",
                onValueChange = { viewModel.updateConfirmPassword(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            TraceButton(
                text = "Sign Up",
                isLoading = uiState.isLoading,
                onClick = { viewModel.signUp(onSuccess = {
                    navController.popBackStack()
                }) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TraceSeparator(text = "OR")

            Spacer(modifier = Modifier.height(16.dp))

            SocialSignInButton(
                text = "Sign in with GitHub",
                iconRes = R.drawable.ic_github_logo,
                onClick = { viewModel.signUpWithGithub() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Text(
                    "Already have an account? ",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Sign In",
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
