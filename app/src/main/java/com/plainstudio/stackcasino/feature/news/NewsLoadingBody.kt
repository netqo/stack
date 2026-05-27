package com.plainstudio.stackcasino.feature.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plainstudio.stackcasino.ui.components.Skeleton

/**
 * Loading state: skeleton placeholders shaped like the FEATURED hero
 * and three list rows so the layout doesn't pop when the first fetch
 * resolves.
 */
@Composable
internal fun NewsLoadingBody() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(SectionVerticalPadding),
    ) {
        FeaturedSkeleton()
        Spacer(modifier = Modifier.height(4.dp))
        repeat(LIST_SKELETON_COUNT) {
            ListRowSkeleton()
        }
    }
}

@Composable
private fun FeaturedSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Skeleton(modifier = Modifier.fillMaxWidth().height(FeaturedSkeletonHeight))
        Skeleton(modifier = Modifier.fillMaxWidth(fraction = 0.75f).height(SkeletonTextLineHeight))
        Skeleton(modifier = Modifier.fillMaxWidth(fraction = 0.50f).height(SkeletonTextLineHeight))
    }
}

@Composable
private fun ListRowSkeleton() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Skeleton(modifier = Modifier.size(ListRowThumbSize))
        Column(
            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Skeleton(modifier = Modifier.fillMaxWidth(fraction = 0.40f).height(SkeletonTextLineHeight))
            Skeleton(modifier = Modifier.fillMaxWidth().height(SkeletonTextLineHeight))
            Skeleton(modifier = Modifier.fillMaxWidth(fraction = 0.65f).height(SkeletonTextLineHeight))
        }
    }
}

private val FeaturedSkeletonHeight = 200.dp
private val ListRowThumbSize = 96.dp
private val SkeletonTextLineHeight = 10.dp
private const val LIST_SKELETON_COUNT = 3
