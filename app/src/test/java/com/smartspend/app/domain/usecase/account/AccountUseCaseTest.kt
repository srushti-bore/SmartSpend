package com.smartspend.app.domain.usecase.account

import com.smartspend.app.domain.model.AccountType
import com.smartspend.app.domain.usecase.FakeAccountRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class AccountUseCaseTest {

    private lateinit var fakeAccountRepository: FakeAccountRepository
    private lateinit var createAccountUseCase: CreateAccountUseCase
    private lateinit var getAccountsUseCase: GetAccountsUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase

    private val profileId = "test_profile_123"

    @Before
    fun setUp() {
        fakeAccountRepository = FakeAccountRepository()
        createAccountUseCase = CreateAccountUseCase(fakeAccountRepository)
        getAccountsUseCase = GetAccountsUseCase(fakeAccountRepository)
        deleteAccountUseCase = DeleteAccountUseCase(fakeAccountRepository)
    }

    @Test
    fun `createAccount with valid details succeeds`() = runTest {
        val result = createAccountUseCase(
            profileId = profileId,
            name = "ICICI Salary Account",
            type = AccountType.BANK,
            initialBalance = BigDecimal("50000.00")
        )

        assertTrue(result.isSuccess)
        val accounts = getAccountsUseCase(profileId).first()
        assertEquals(1, accounts.size)
        assertEquals("ICICI Salary Account", accounts[0].name)
        assertEquals(AccountType.BANK, accounts[0].type)
    }

    @Test
    fun `createAccount with blank name fails`() = runTest {
        val result = createAccountUseCase(
            profileId = profileId,
            name = "   ",
            type = AccountType.CASH
        )
        assertTrue(result.isFailure)
    }
}
