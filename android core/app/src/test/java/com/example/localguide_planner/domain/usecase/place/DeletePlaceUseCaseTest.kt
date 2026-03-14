package com.example.localguide_planner.domain.usecase.place

import com.example.localguide_planner.domain.repository.PlacesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeletePlaceUseCaseTest {

    private val repository: PlacesRepository = mockk()
    private val useCase = DeletePlaceUseCase(repository)

    @Test
    fun `invoke calls repository deletePlace with correct id`() = runTest {
        coEvery { repository.deletePlace("test-id") } returns Unit

        useCase("test-id")

        coVerify(exactly = 1) { repository.deletePlace("test-id") }
    }
}
