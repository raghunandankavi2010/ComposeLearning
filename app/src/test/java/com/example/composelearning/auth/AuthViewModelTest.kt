package com.example.composelearning.auth

import com.example.composelearning.auth.domain.AuthEventLog
import com.example.composelearning.auth.domain.usecase.ForceExpireAccessTokenUseCase
import com.example.composelearning.auth.domain.usecase.GetProfileUseCase
import com.example.composelearning.auth.domain.usecase.LoginUseCase
import com.example.composelearning.auth.domain.usecase.LogoutUseCase
import com.example.composelearning.auth.domain.usecase.RefreshTokensUseCase
import com.example.composelearning.auth.presentation.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeAuthRepository, log: AuthEventLog = AuthEventLog { 0L }) =
        AuthViewModel(
            login = LoginUseCase(repo),
            getProfile = GetProfileUseCase(repo),
            refresh = RefreshTokensUseCase(repo),
            forceExpire = ForceExpireAccessTokenUseCase(repo),
            logout = LogoutUseCase(repo),
            repository = repo,
            eventLog = log,
        )

    /** uiState uses WhileSubscribed, so keep a collector alive for the duration of each test. */
    private fun TestScope.observe(vm: AuthViewModel) {
        backgroundScope.launch { vm.uiState.collect {} }
    }

    @Test
    fun `login populates tokens and marks logged in`() = runTest {
        val repo = FakeAuthRepository()
        val vm = viewModel(repo)
        observe(vm)

        vm.onLogin()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("session is active", state.isLoggedIn)
        assertNotNull("token pair stored", state.tokens)
        assertEquals(1, repo.loginCount)
    }

    @Test
    fun `calling profile stores the returned profile`() = runTest {
        val repo = FakeAuthRepository()
        val vm = viewModel(repo)
        observe(vm)
        vm.onLogin()
        advanceUntilIdle()

        vm.onCallProfile()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("demo", state.profile?.username)
        assertEquals(1, repo.profileCount)
    }

    @Test
    fun `logout clears tokens and profile`() = runTest {
        val repo = FakeAuthRepository()
        val vm = viewModel(repo)
        observe(vm)
        vm.onLogin(); advanceUntilIdle()
        vm.onCallProfile(); advanceUntilIdle()

        vm.onLogout(); advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse("logged out", state.isLoggedIn)
        assertNull("profile dropped", state.profile)
        assertNull("no tokens", state.tokens)
    }

    @Test
    fun `force-expire back-dates the access token so the countdown reads expired`() = runTest {
        val repo = FakeAuthRepository()
        val vm = viewModel(repo)
        observe(vm)
        vm.onLogin(); advanceUntilIdle()

        vm.onForceExpire(); advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, repo.forceExpireCount)
        assertTrue("still logged in", state.isLoggedIn)
        assertTrue("access token now reads expired", state.tokens!!.isAccessExpired(nowMillis = 1L))
    }

    @Test
    fun `a failing call surfaces an error and clears busy`() = runTest {
        val repo = FakeAuthRepository(failWith = IllegalStateException("boom"))
        val vm = viewModel(repo)
        observe(vm)

        vm.onLogin()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("boom", state.error)
        assertFalse("busy reset", state.isBusy)
        assertFalse("still logged out", state.isLoggedIn)
    }
}
