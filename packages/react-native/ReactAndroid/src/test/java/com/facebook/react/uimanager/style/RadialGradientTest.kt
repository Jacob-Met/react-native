/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.uimanager.style

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.DisplayMetrics
import com.facebook.react.uimanager.DisplayMetricsHolder
import com.facebook.react.uimanager.LengthPercentage
import com.facebook.react.uimanager.LengthPercentageType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RadialGradientTest {
  @Before
  fun setUp() {
    DisplayMetricsHolder.setScreenDisplayMetrics(DisplayMetrics().apply { density = 1f })
  }

  @Test
  fun lengthStopOnTallEllipseUsesHorizontalRadius() {
    val bitmap =
        renderGradient(
            50f,
            150f,
            LengthPercentage(40f, LengthPercentageType.POINT),
        )

    // CSS's gradient line extends rightward, so 40px is 40px along the 50px x-radius.
    // Along the y-axis that same 80% stop appears 120px from the center.
    assertThat(bitmap.getPixel(75, 150)).isEqualTo(Color.RED)
    assertThat(bitmap.getPixel(50, 225)).isEqualTo(Color.RED)
    assertThat(bitmap.getPixel(97, 150)).isEqualTo(Color.BLUE)
    assertThat(bitmap.getPixel(50, 285)).isEqualTo(Color.BLUE)
  }

  @Test
  fun percentageStopsOnTallEllipseKeepTheirProportionalPositions() {
    val bitmap =
        renderGradient(
            50f,
            150f,
            LengthPercentage(40f, LengthPercentageType.PERCENT),
        )

    assertThat(bitmap.getPixel(65, 150)).isEqualTo(Color.RED)
    assertThat(bitmap.getPixel(80, 150)).isEqualTo(Color.BLUE)
    assertThat(bitmap.getPixel(50, 190)).isEqualTo(Color.RED)
    assertThat(bitmap.getPixel(50, 245)).isEqualTo(Color.BLUE)
  }

  @Test
  fun lengthStopOnWideEllipseIsUnchanged() {
    val bitmap =
        renderGradient(
            150f,
            50f,
            LengthPercentage(40f, LengthPercentageType.POINT),
        )

    assertThat(bitmap.getPixel(175, 50)).isEqualTo(Color.RED)
    assertThat(bitmap.getPixel(197, 50)).isEqualTo(Color.BLUE)
    assertThat(bitmap.getPixel(150, 60)).isEqualTo(Color.RED)
    assertThat(bitmap.getPixel(150, 75)).isEqualTo(Color.BLUE)
  }

  private fun renderGradient(
      radiusX: Float,
      radiusY: Float,
      stop: LengthPercentage,
  ): Bitmap {
    val width = (radiusX * 2f).toInt()
    val height = (radiusY * 2f).toInt()
    val gradient =
        RadialGradient(
            shape = RadialGradient.Shape.ELLIPSE,
            size =
                RadialGradient.GradientSize.Dimensions(
                    LengthPercentage(radiusX, LengthPercentageType.POINT),
                    LengthPercentage(radiusY, LengthPercentageType.POINT),
                ),
            position = RadialGradient.Position(),
            colorStops = listOf(ColorStop(Color.RED, stop), ColorStop(Color.BLUE, stop)),
        )
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    Canvas(bitmap)
        .drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Paint().apply { shader = gradient.getShader(width.toFloat(), height.toFloat()) },
        )
    return bitmap
  }
}
