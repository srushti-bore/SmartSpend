package com.smartspend.app.domain.usecase.savingsgoal

import com.smartspend.app.domain.model.ContributionType
import com.smartspend.app.domain.usecase.FakeSavingsGoalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class SavingsGoalUseCaseTest {

    private lateinit var fakeSavingsGoalRepository: FakeSavingsGoalRepository
    private lateinit var createSavingsGoalUseCase: CreateSavingsGoalUseCase
    private lateinit var contributeToSavingsGoalUseCase: ContributeToSavingsGoalUseCase
    private lateinit var getSavingsGoalsUseCase: GetSavingsGoalsUseCase

    private val profileId = "test_profile_123"

    @Before
    fun setUp() {
        fakeSavingsGoalRepository = FakeSavingsGoalRepository()
        createSavingsGoalUseCase = CreateSavingsGoalUseCase(fakeSavingsGoalRepository)
        contributeToSavingsGoalUseCase = ContributeToSavingsGoalUseCase(fakeSavingsGoalRepository)
        getSavingsGoalsUseCase = GetSavingsGoalsUseCase(fakeSavingsGoalRepository)
    }

    @Test
    fun `create savings goal and record deposits and withdrawals`() = runTest {
        val futureDate = System.currentTimeMillis() + 86400000L * 90 // 90 days in future

        val createResult = createSavingsGoalUseCase(
            profileId = profileId,
            name = "Emergency Fund",
            targetAmount = BigDecimal("100000.00"),
            targetDate = futureDate,
            initialAmount = BigDecimal("10000.00")
        )
        assertTrue(createResult.isSuccess)
        val goal = createResult.getOrNull()!!

        // Deposit 15,000
        val depositResult = contributeToSavingsGoalUseCase(
            profileId = profileId,
            goalId = goal.id,
            amount = BigDecimal("15000.00"),
            type = ContributionType.DEPOSIT
        )
        assertTrue(depositResult.isSuccess)

        val updatedGoal = fakeSavingsGoalRepository.getSavingsGoalById(profileId, goal.id)!!
        assertEquals(BigDecimal("25000.00"), updatedGoal.currentAmount)

        // Withdraw 5,000
        val withdrawResult = contributeToSavingsGoalUseCase(
            profileId = profileId,
            goalId = goal.id,
            amount = BigDecimal("5000.00"),
            type = ContributionType.WITHDRAWAL
        )
        assertTrue(withdrawResult.isSuccess)

        val finalGoal = fakeSavingsGoalRepository.getSavingsGoalById(profileId, goal.id)!!
        assertEquals(BigDecimal("20000.00"), finalGoal.currentAmount)
    }

    @Test
    fun `cannot withdraw more than current saved amount`() = runTest {
        val futureDate = System.currentTimeMillis() + 86400000L * 30
        val goal = createSavingsGoalUseCase(
            profileId = profileId,
            name = "Small Goal",
            targetAmount = BigDecimal("5000.00"),
            targetDate = futureDate,
            initialAmount = BigDecimal("1000.00")
        ).getOrNull()!!

        val withdrawResult = contributeToSavingsGoalUseCase(
            profileId = profileId,
            goalId = goal.id,
            amount = BigDecimal("2000.00"),
            type = ContributionType.WITHDRAWAL
        )
        assertTrue(withdrawResult.isFailure)
    }
}
