package it.unibo.trace.ui.screen.auth

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
import it.unibo.trace.ui.Route
import it.unibo.trace.ui.composable.button.SocialSignInButton
import it.unibo.trace.ui.composable.button.TraceButton
import it.unibo.trace.ui.composable.input.EmailField
import it.unibo.trace.ui.composable.input.PasswordField
import it.unibo.trace.ui.composable.separator.TraceSeparator
import it.unibo.trace.ui.viewmodel.auth.LoginViewModel

/**
 * Screen for user login via email/password or third-party providers.
 */
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = koinViewModel()
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
                text = stringResource(R.string.app_name),
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

            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
                    .clickable { navController.navigate(Route.ForgotPassword) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            TraceButton(
                text = "Sign In",
                isLoading = uiState.isLoading,
                onClick = { viewModel.signIn() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TraceSeparator(text = "OR")

            Spacer(modifier = Modifier.height(16.dp))

            SocialSignInButton(
                text = "Sign in with GitHub",
                iconRes = R.drawable.ic_github_logo,
                onClick = { viewModel.signInWithGithub() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(
                    "New to TRACE? ",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Sign Up",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Route.Registration)
                    }
                )
            }
        }
    }
}
