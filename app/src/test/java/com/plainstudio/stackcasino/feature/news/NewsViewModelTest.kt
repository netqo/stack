package com.plainstudio.stackcasino.feature.news

import app.cash.turbine.test
import com.plainstudio.stackcasino.domain.news.NewsArticle
import com.plainstudio.stackcasino.domain.news.NewsRepository
import com.plainstudio.stackcasino.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NewsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<NewsRepository>()

    private fun viewModel(): NewsViewModel = NewsViewModel(repository)

    private val sampleArticles =
        listOf(
            article(id = "a", title = "Bitcoin surges past 70k", source = "CryptoNews"),
            article(id = "b", title = "Polygon zkEVM milestone", source = "Polygon Post"),
            article(id = "c", title = "Blackjack splitting math", source = "CryptoNews"),
        )

    private fun article(
        id: String,
        title: String,
        source: String,
    ): NewsArticle =
        NewsArticle(
            id = id,
            title = title,
            source = source,
            description = null,
            url = "https://example.com/$id",
            imageUrl = null,
            publishedAtIso = "2026-04-16T07:30:00Z",
        )

    // --- initial state ----------------------------------------------------

    @Test
    fun `initial state is Loading while the first refresh is in flight`() =
        runTest {
            // Gate the repository so Loading is observable.
            val gate = CompletableDeferred<Result<List<NewsArticle>>>()
            coEvery { repository.refresh() } coAnswers { gate.await() }
            val vm = viewModel()

            vm.uiState.test {
                assertEquals(NewsUiState.Loading, awaitItem())
                gate.complete(Result.success(sampleArticles))
                val success = awaitItem() as NewsUiState.Success
                assertEquals(3, success.allArticles.size)
                assertEquals(3, success.filtered.size)
            }
        }

    @Test
    fun `success builds the sources list deduped and sorted case-insensitive`() =
        runTest {
            coEvery { repository.refresh() } returns Result.success(sampleArticles)
            val vm = viewModel()

            val state = vm.uiState.value as NewsUiState.Success
            assertEquals(listOf("CryptoNews", "Polygon Post"), state.sources)
        }

    // --- search filtering ------------------------------------------------

    @Test
    fun `onQueryChange filters by title substring case-insensitive`() =
        runTest {
            coEvery { repository.refresh() } returns Result.success(sampleArticles)
            val vm = viewModel()

            vm.onQueryChange("polygon")

            val state = vm.uiState.value as NewsUiState.Success
            assertEquals(listOf("b"), state.filtered.map { it.id })
        }

    @Test
    fun `onQueryChange matches source name in addition to titles`() =
        runTest {
            coEvery { repository.refresh() } returns Result.success(sampleArticles)
            val vm = viewModel()

            vm.onQueryChange("cryptonews")

            val state = vm.uiState.value as NewsUiState.Success
            assertEquals(listOf("a", "c"), state.filtered.map { it.id })
        }

    @Test
    fun `blank query restores the unfiltered list`() =
        runTest {
            coEvery { repository.refresh() } returns Result.success(sampleArticles)
            val vm = viewModel()

            vm.onQueryChange("polygon")
            vm.onQueryChange("")

            val state = vm.uiState.value as NewsUiState.Success
            assertEquals(3, state.filtered.size)
        }

    // --- source filtering ------------------------------------------------

    @Test
    fun `onSourceChange narrows the list to the picked source`() =
        runTest {
            coEvery { repository.refresh() } returns Result.success(sampleArticles)
            val vm = viewModel()

            vm.onSourceChange(SourceFilter.Named("CryptoNews"))

            val state = vm.uiState.value as NewsUiState.Success
            assertEquals(listOf("a", "c"), state.filtered.map { it.id })
        }

    @Test
    fun `source filter and search compose so both must match`() =
        runTest {
            coEvery { repository.refresh() } returns Result.success(sampleArticles)
            val vm = viewModel()

            vm.onSourceChange(SourceFilter.Named("CryptoNews"))
            vm.onQueryChange("blackjack")

            val state = vm.uiState.value as NewsUiState.Success
            assertEquals(listOf("c"), state.filtered.map { it.id })
        }

    @Test
    fun `SourceFilter All restores the cross-source list`() =
        runTest {
            coEvery { repository.refresh() } returns Result.success(sampleArticles)
            val vm = viewModel()

            vm.onSourceChange(SourceFilter.Named("Polygon Post"))
            vm.onSourceChange(SourceFilter.All)

            val state = vm.uiState.value as NewsUiState.Success
            assertEquals(3, state.filtered.size)
        }

    // --- error + retry --------------------------------------------------

    @Test
    fun `refresh failure surfaces Error state with the underlying message`() =
        runTest {
            coEvery { repository.refresh() } returns Result.failure(RuntimeException("network down"))

            val vm = viewModel()

            val state = vm.uiState.value as NewsUiState.Error
            assertTrue(state.message.contains("network down"))
        }

    @Test
    fun `refresh after an error retries the repository`() =
        runTest {
            coEvery { repository.refresh() } returnsMany
                listOf(Result.failure(RuntimeException("first")), Result.success(sampleArticles))
            val vm = viewModel()
            assertTrue(vm.uiState.value is NewsUiState.Error)

            vm.refresh()

            assertTrue(vm.uiState.value is NewsUiState.Success)
        }
}
