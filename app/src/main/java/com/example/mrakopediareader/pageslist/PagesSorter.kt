package com.example.mrakopediareader.pageslist

import com.example.mrakopediareader.api.dto.Page
import com.example.mrakopediareader.metainfo.PagesMetaInfoSource

class PagesSorter(private val pagesMetaInfoSource: PagesMetaInfoSource) {
    private val sortersMap = mapOf<SortID, (pages: List<Page>) -> List<Page>>(
        SortID.ALPHA_ASC to (::alphaAsc),
        SortID.ALPHA_DESC to (::alphaDesc),
        SortID.READING_TIME_ASC to (::readingTimeAsc),
        SortID.READING_TIME_DESC to (::readingTimeDesc),
        SortID.RATING_ASC to (::ratingAsc),
        SortID.RATING_DESC to (::ratingDesc),
        SortID.VOTED_ASC to (::votedAsc),
        SortID.VOTED_DESC to (::votedDesc)
    )

    private fun alphaAsc(pages: List<Page>): List<Page> {
        return pages.sortedWith { page1, page2 -> page1.title.compareTo(page2.title) }
    }

    private fun alphaDesc(pages: List<Page>): List<Page> {
        return pages.sortedWith { page1, page2 -> page2.title.compareTo(page1.title) }
    }

    private fun readingTimeAsc(pages: List<Page>): List<Page> {
        return pages.sortedWith { page1, page2 ->
            val readingTime1 =
                pagesMetaInfoSource.getMetaInfoByPageTitle(page1.title)?.readableCharacters ?: 0
            val readingTime2 =
                pagesMetaInfoSource.getMetaInfoByPageTitle(page2.title)?.readableCharacters ?: 0
            readingTime1 - readingTime2
        }
    }

    private fun readingTimeDesc(pages: List<Page>): List<Page> {
        return pages.sortedWith { page1, page2 ->
            val readingTime1 =
                pagesMetaInfoSource.getMetaInfoByPageTitle(page1.title)?.readableCharacters ?: 0
            val readingTime2 =
                pagesMetaInfoSource.getMetaInfoByPageTitle(page2.title)?.readableCharacters ?: 0
            readingTime2 - readingTime1
        }
    }

    private fun ratingAsc(pages: List<Page>): List<Page> {
        return pages.sortedWith { page1, page2 ->
            val rating1 = pagesMetaInfoSource.getMetaInfoByPageTitle(page1.title)?.rating ?: 0
            val rating2 = pagesMetaInfoSource.getMetaInfoByPageTitle(page2.title)?.rating ?: 0
            rating1 - rating2
        }
    }

    private fun ratingDesc(pages: List<Page>): List<Page> {
        return pages.sortedWith { page1, page2 ->
            val rating1 = pagesMetaInfoSource.getMetaInfoByPageTitle(page1.title)?.rating ?: 0
            val rating2 = pagesMetaInfoSource.getMetaInfoByPageTitle(page2.title)?.rating ?: 0
            rating2 - rating1
        }
    }

    private fun votedAsc(pages: List<Page>): List<Page> {
        return pages.sortedWith { page1, page2 ->
            val voted1 = pagesMetaInfoSource.getMetaInfoByPageTitle(page1.title)?.voted ?: 0
            val voted2 = pagesMetaInfoSource.getMetaInfoByPageTitle(page2.title)?.voted ?: 0
            voted1 - voted2
        }
    }

    private fun votedDesc(pages: List<Page>): List<Page> {
        return pages.sortedWith { page1, page2 ->
            val voted1 = pagesMetaInfoSource.getMetaInfoByPageTitle(page1.title)?.voted ?: 0
            val voted2 = pagesMetaInfoSource.getMetaInfoByPageTitle(page2.title)?.voted ?: 0
            voted2 - voted1
        }
    }

    fun sorted(sortID: SortID, pages: List<Page>): List<Page> {
        val sorter = sortersMap[sortID]
        return sorter?.let { it(pages) } ?: pages
    }

    companion object {
        private val opposites = mapOf(
            SortID.ALPHA_ASC to SortID.ALPHA_DESC,
            SortID.ALPHA_DESC to SortID.ALPHA_ASC,
            SortID.READING_TIME_ASC to SortID.READING_TIME_DESC,
            SortID.READING_TIME_DESC to SortID.READING_TIME_ASC,
            SortID.RATING_ASC to SortID.RATING_DESC,
            SortID.RATING_DESC to SortID.RATING_ASC,
            SortID.VOTED_ASC to SortID.VOTED_DESC,
            SortID.VOTED_DESC to SortID.VOTED_ASC
        )

        enum class SortID {
            ALPHA_ASC,
            ALPHA_DESC,
            READING_TIME_ASC,
            READING_TIME_DESC,
            RATING_ASC,
            RATING_DESC,
            VOTED_ASC,
            VOTED_DESC
        }

        fun nextAlpha(current: SortID?): SortID {
            return if (current == SortID.ALPHA_ASC || current == SortID.ALPHA_DESC) {
                opposites[current] ?: SortID.ALPHA_ASC
            } else {
                SortID.ALPHA_ASC
            }
        }

        fun nextReadingTime(current: SortID?): SortID {
            return if (current == SortID.READING_TIME_ASC || current == SortID.READING_TIME_DESC) {
                opposites[current] ?: SortID.READING_TIME_ASC
            } else {
                SortID.READING_TIME_ASC
            }
        }

        fun nextRating(current: SortID?): SortID {
            return if (current == SortID.RATING_ASC || current == SortID.RATING_DESC) {
                opposites[current] ?: SortID.RATING_ASC
            } else {
                SortID.RATING_ASC
            }
        }

        fun nextVoted(current: SortID?): SortID {
            return if (current == SortID.VOTED_ASC || current == SortID.VOTED_DESC) {
                opposites[current] ?: SortID.VOTED_ASC
            } else {
                SortID.VOTED_ASC
            }
        }
    }
}