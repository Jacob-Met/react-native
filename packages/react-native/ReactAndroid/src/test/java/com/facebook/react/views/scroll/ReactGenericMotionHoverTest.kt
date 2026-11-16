/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.views.scroll

import android.app.Activity
import android.content.Context
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlagsForTests
import com.facebook.react.uimanager.PointerEvents
import com.facebook.react.views.view.ReactViewGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReactGenericMotionHoverTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    ReactNativeFeatureFlagsForTests.setUp()
    context = Robolectric.buildActivity(Activity::class.java).setup().get()
  }

  @Test
  fun boxOnlyViewReceivesHoverWithoutDispatchingToItsChildren() {
    val parent = ReactViewGroup(context)
    val child = View(context)
    var parentHovers = 0
    var childHovers = 0
    parent.setOnHoverListener { _, _ ->
      parentHovers++
      true
    }
    child.setOnHoverListener { _, _ ->
      childHovers++
      true
    }
    mount(parent, child)
    parent.pointerEvents = PointerEvents.BOX_ONLY

    assertThat(dispatch(parent, MotionEvent.ACTION_HOVER_ENTER)).isTrue()
    assertThat(parentHovers).isEqualTo(1)
    assertThat(childHovers).isZero()
  }

  @Test
  fun noneViewDoesNotReceiveHoverOrDispatchToItsChildren() {
    val parent = ReactViewGroup(context)
    val child = View(context)
    var parentHovers = 0
    var childHovers = 0
    parent.setOnHoverListener { _, _ ->
      parentHovers++
      true
    }
    child.setOnHoverListener { _, _ ->
      childHovers++
      true
    }
    mount(parent, child)
    parent.pointerEvents = PointerEvents.NONE

    assertThat(dispatch(parent, MotionEvent.ACTION_HOVER_ENTER)).isFalse()
    assertThat(parentHovers).isZero()
    assertThat(childHovers).isZero()
  }

  @Test
  fun boxOnlyScrollViewsReceiveHoverWithoutForwardingItToChildren() {
    val scrollViews =
        listOf<ViewGroup>(
            ReactScrollView(context).apply {
              pointerEvents = PointerEvents.BOX_ONLY
              scrollEnabled = false
            },
            ReactHorizontalScrollView(context).apply {
              pointerEvents = PointerEvents.BOX_ONLY
              scrollEnabled = false
            },
            ReactNestedScrollView(context).apply {
              pointerEvents = PointerEvents.BOX_ONLY
              scrollEnabled = false
            },
        )

    scrollViews.forEach { scrollView ->
      val child = View(context)
      var parentHovers = 0
      var childHovers = 0
      scrollView.setOnHoverListener { _, _ ->
        parentHovers++
        true
      }
      child.setOnHoverListener { _, _ ->
        childHovers++
        true
      }
      mount(scrollView, child)

      assertThat(dispatch(scrollView, MotionEvent.ACTION_HOVER_ENTER))
          .describedAs(scrollView.javaClass.simpleName)
          .isTrue()
      assertThat(parentHovers).describedAs(scrollView.javaClass.simpleName).isEqualTo(1)
      assertThat(childHovers).describedAs(scrollView.javaClass.simpleName).isZero()
      assertThat(dispatch(scrollView, MotionEvent.ACTION_SCROLL)).isFalse()
    }
  }

  @Test
  fun disabledScrollViewsStillDispatchHoverToContentButNotWheel() {
    val scrollViews =
        listOf<ViewGroup>(
            ReactScrollView(context).apply { scrollEnabled = false },
            ReactHorizontalScrollView(context).apply { scrollEnabled = false },
            ReactNestedScrollView(context).apply { scrollEnabled = false },
        )

    scrollViews.forEach { scrollView ->
      val child = View(context)
      var hovers = 0
      var wheels = 0
      child.setOnHoverListener { _, _ ->
        hovers++
        true
      }
      child.setOnGenericMotionListener { _, _ ->
        wheels++
        true
      }
      mount(scrollView, child)

      assertThat(dispatch(scrollView, MotionEvent.ACTION_HOVER_ENTER))
          .describedAs(scrollView.javaClass.simpleName)
          .isTrue()
      assertThat(hovers).describedAs(scrollView.javaClass.simpleName).isEqualTo(1)
      assertThat(dispatch(scrollView, MotionEvent.ACTION_SCROLL)).isFalse()
      assertThat(wheels).isZero()
    }
  }

  private fun mount(parent: ViewGroup, child: View) {
    parent.addView(child, ViewGroup.LayoutParams(100, 100))
    parent.layout(0, 0, 100, 100)
    child.layout(0, 0, 100, 100)
  }

  private fun dispatch(view: View, action: Int): Boolean {
    val event = MotionEvent.obtain(0, 0, action, 50f, 50f, 0)
    event.source = InputDevice.SOURCE_MOUSE
    return try {
      view.dispatchGenericMotionEvent(event)
    } finally {
      event.recycle()
    }
  }
}
