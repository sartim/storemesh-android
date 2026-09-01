package com.storemesh.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse

private const val REDIRECT_URI = "com.storemesh.android://oauth/callback"

/** Native Authorization Code + PKCE client for the StoreMesh Keycloak realm. */
class OidcAuthClient(private val activity: Activity) {
    private val service = AuthorizationService(activity)

    fun authorizationIntent(onError: (String) -> Unit): Intent? {
        val issuer = BuildConfig.KEYCLOAK_ISSUER.trimEnd('/')
        val configuration = AuthorizationServiceConfiguration(
            Uri.parse("$issuer/protocol/openid-connect/auth"),
            Uri.parse("$issuer/protocol/openid-connect/token")
        )
        val request = AuthorizationRequest.Builder(
            configuration,
            BuildConfig.KEYCLOAK_CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        ).setScope("openid profile email").build()
        return service.getAuthorizationRequestIntent(request)
    }

    fun exchange(responseIntent: Intent, onSuccess: (LoginResult) -> Unit, onError: (String) -> Unit) {
        val response = AuthorizationResponse.fromIntent(responseIntent)
        val exception = AuthorizationException.fromIntent(responseIntent)
        if (response == null) {
            onError(exception?.errorDescription ?: "OIDC sign-in was cancelled")
            return
        }
        val tokenRequest = response.createTokenExchangeRequest()
        service.performTokenRequest(tokenRequest) { tokenResponse: TokenResponse?, tokenException: AuthorizationException? ->
            if (tokenResponse?.accessToken != null && tokenResponse.refreshToken != null) {
                onSuccess(LoginResult(tokenResponse.accessToken!!, tokenResponse.refreshToken!!))
            } else {
                onError(tokenException?.errorDescription ?: "Unable to exchange OIDC authorization code")
            }
        }
    }

    fun dispose() = service.dispose()
}

@Composable
fun rememberOidcSignIn(onLoggedIn: (LoginResult) -> Unit, onError: (String) -> Unit): () -> Unit {
    val activity = androidx.compose.ui.platform.LocalContext.current as? Activity
    val client = remember(activity) { activity?.let(::OidcAuthClient) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null && client != null) {
            client.exchange(result.data!!, onLoggedIn, onError)
        } else if (result.resultCode != Activity.RESULT_CANCELED) {
            onError("OIDC sign-in did not return a result")
        }
    }
    val scope = rememberCoroutineScope()
    return {
        val intent = client?.authorizationIntent(onError)
        if (intent == null) onError("OIDC is unavailable in this build") else scope.launch { launcher.launch(intent) }
    }
}
