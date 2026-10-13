package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CivicRepository
import com.example.data.auth.FirebaseAuthService
import com.example.model.AuthScreenMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Civic Nexus AI", appName)
    }

    @Test
    fun `google services configuration provides web client id`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authService = FirebaseAuthService(context)
        val clientId = authService.getWebClientId()
        assertNotNull(clientId)
        assertEquals("954633763870-ghn2le6b21eru94ekquo3l7ht6kt1iso.apps.googleusercontent.com", clientId)
    }

    @Test
    fun `application id matches expected firebase package name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(BuildConfig.APPLICATION_ID, context.packageName)
    }

    @Test
    fun `auth mode toggles between login and register`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = CivicRepository(context)

        assertEquals(AuthScreenMode.LOGIN, repository.authUiState.value.authScreenMode)

        repository.setAuthScreenMode(AuthScreenMode.REGISTER)
        assertEquals(AuthScreenMode.REGISTER, repository.authUiState.value.authScreenMode)

        repository.setAuthScreenMode(AuthScreenMode.LOGIN)
        assertEquals(AuthScreenMode.LOGIN, repository.authUiState.value.authScreenMode)
    }

    @Test
    fun `signOut clears authenticated user session`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = CivicRepository(context)

        val result = repository.signOut()
        assertTrue(result.isSuccess)
        assertFalse(repository.authUiState.value.isAuthenticated)
        assertNull(repository.authUiState.value.currentUser)
    }

    @Test
    fun `theme mode persists across repository instances and is preserved on signOut`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // 1. Initialize repository and verify default or set theme
        val repo1 = CivicRepository(context)
        repo1.setThemeMode(com.example.model.AppThemeMode.DARK)
        assertEquals(com.example.model.AppThemeMode.DARK, repo1.themeMode.value)

        // 2. Re-instantiate repository to verify persistence
        val repo2 = CivicRepository(context)
        assertEquals(com.example.model.AppThemeMode.DARK, repo2.themeMode.value)

        // 3. Switch to LIGHT mode
        repo2.setThemeMode(com.example.model.AppThemeMode.LIGHT)
        assertEquals(com.example.model.AppThemeMode.LIGHT, repo2.themeMode.value)

        // 4. Verify signOut does not reset theme preference
        repo2.signOut()
        assertEquals(com.example.model.AppThemeMode.LIGHT, repo2.themeMode.value)

        // 5. Re-instantiate to verify LIGHT remains stored
        val repo3 = CivicRepository(context)
        assertEquals(com.example.model.AppThemeMode.LIGHT, repo3.themeMode.value)
    }
}
