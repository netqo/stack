package com.plainstudio.stackcasino.data.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsArticleMapperTest {
    @Test
    fun `maps a well-formed dto into the domain article using the url as id`() {
        val dto =
            NewsApiArticleDto(
                source = NewsApiSourceDto(id = "cn", name = "CryptoNews"),
                title = "Bitcoin surges past 70k",
                description = "Institutional flows continue to drive demand.",
                url = "https://cryptonews.example/btc-70k",
                urlToImage = "https://cryptonews.example/cover.jpg",
                publishedAt = "2026-04-16T07:30:00Z",
            )

        val article = dto.toDomainOrNull()

        assertEquals("https://cryptonews.example/btc-70k", article?.id)
        assertEquals("https://cryptonews.example/btc-70k", article?.url)
        assertEquals("Bitcoin surges past 70k", article?.title)
        assertEquals("CryptoNews", article?.source)
    }

    @Test
    fun `drops the article when the title is missing`() {
        val dto = baseDto().copy(title = null)
        assertNull(dto.toDomainOrNull())
    }

    @Test
    fun `drops the article when the title is the Removed sentinel`() {
        val dto = baseDto().copy(title = "[Removed]")
        assertNull(dto.toDomainOrNull())
    }

    @Test
    fun `drops the article when the url is blank`() {
        val dto = baseDto().copy(url = "   ")
        assertNull(dto.toDomainOrNull())
    }

    @Test
    fun `drops the article when the source name is missing`() {
        val dto = baseDto().copy(source = NewsApiSourceDto(id = null, name = null))
        assertNull(dto.toDomainOrNull())
    }

    @Test
    fun `keeps the article and drops the description when description is the Removed sentinel`() {
        val article = baseDto().copy(description = "[Removed]").toDomainOrNull()
        assertNull(article?.description)
        assertEquals("Bitcoin surges past 70k", article?.title)
    }

    @Test
    fun `keeps the article and drops the imageUrl when the image is blank`() {
        val article = baseDto().copy(urlToImage = "").toDomainOrNull()
        assertNull(article?.imageUrl)
    }

    private fun baseDto(): NewsApiArticleDto =
        NewsApiArticleDto(
            source = NewsApiSourceDto(id = "cn", name = "CryptoNews"),
            title = "Bitcoin surges past 70k",
            description = "Summary.",
            url = "https://cryptonews.example/btc-70k",
            urlToImage = "https://cryptonews.example/cover.jpg",
            publishedAt = "2026-04-16T07:30:00Z",
        )
}
