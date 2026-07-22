package app.usefriendly.barely

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.roundToInt

/**
 * Renders launcher icons before Compose displays them.
 *
 * Adaptive icons are rebuilt from their background and foreground layers so the selected mask is
 * applied exactly once. Legacy artwork is inset inside a matching edge-color mask when it reaches
 * the source bounds, which preserves its complete artwork instead of cutting off square corners.
 */
internal object AppIconRenderer {
    fun render(drawable: Drawable, shape: AppIconShape, sizePx: Int): Bitmap {
        require(sizePx > 0)
        if (shape == AppIconShape.ORIGINAL) {
            return drawable.toBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        }
        return if (drawable is AdaptiveIconDrawable) {
            renderAdaptive(drawable, shape, sizePx)
        } else {
            renderLegacy(drawable, shape, sizePx)
        }
    }

    private fun renderAdaptive(
        drawable: AdaptiveIconDrawable,
        shape: AppIconShape,
        sizePx: Int,
    ): Bitmap {
        val unmasked = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(unmasked)
        val layerBounds = Rect(0, 0, sizePx, sizePx)
        drawable.background?.let { drawLayer(canvas, it, layerBounds) }
        drawable.foreground?.let { drawLayer(canvas, it, layerBounds) }
        return applyMask(unmasked, shape.maskPath(sizePx))
    }

    private fun renderLegacy(drawable: Drawable, shape: AppIconShape, sizePx: Int): Bitmap {
        val source = drawable.toBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val mask = shape.maskPath(sizePx)
        if (!source.hasOpaqueCorners()) return applyMask(source, mask)

        val normalized = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(normalized)
        canvas.drawPath(
            mask,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = source.averageOpaqueCornerColor()
                style = Paint.Style.FILL
            },
        )
        val scale = shape.legacyContentScale()
        val inset = ((1f - scale) * sizePx / 2f).roundToInt()
        canvas.drawBitmap(
            source,
            null,
            Rect(inset, inset, sizePx - inset, sizePx - inset),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
        return applyMask(normalized, mask)
    }

    private fun drawLayer(canvas: Canvas, drawable: Drawable, bounds: Rect) {
        val previousBounds = Rect(drawable.bounds)
        drawable.bounds = bounds
        drawable.draw(canvas)
        drawable.bounds = previousBounds
    }

    private fun applyMask(source: Bitmap, mask: Path): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawPath(
            mask,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            },
        )
        return result
    }

    private fun AppIconShape.maskPath(sizePx: Int): Path = Path().apply {
        val bounds = RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
        when (this@maskPath) {
            AppIconShape.CIRCLE -> addOval(bounds, Path.Direction.CW)
            AppIconShape.SQUIRCLE -> {
                val radius = sizePx * 0.32f
                addRoundRect(bounds, radius, radius, Path.Direction.CW)
            }
            AppIconShape.ROUNDED_SQUARE -> {
                val radius = sizePx * 0.18f
                addRoundRect(bounds, radius, radius, Path.Direction.CW)
            }
            AppIconShape.ORIGINAL -> addRect(bounds, Path.Direction.CW)
        }
    }

    private fun Bitmap.hasOpaqueCorners(): Boolean {
        val inset = (width * 0.06f).roundToInt().coerceAtLeast(1)
        val points = listOf(
            inset to inset,
            width - inset - 1 to inset,
            inset to height - inset - 1,
            width - inset - 1 to height - inset - 1,
        )
        return points.count { (x, y) -> Color.alpha(getPixel(x, y)) >= 220 } >= 3
    }

    private fun Bitmap.averageOpaqueCornerColor(): Int {
        val inset = (width * 0.06f).roundToInt().coerceAtLeast(1)
        val colors = listOf(
            getPixel(inset, inset),
            getPixel(width - inset - 1, inset),
            getPixel(inset, height - inset - 1),
            getPixel(width - inset - 1, height - inset - 1),
        ).filter { Color.alpha(it) >= 64 }
        if (colors.isEmpty()) return Color.TRANSPARENT
        return Color.rgb(
            colors.sumOf(Color::red) / colors.size,
            colors.sumOf(Color::green) / colors.size,
            colors.sumOf(Color::blue) / colors.size,
        )
    }

    internal fun AppIconShape.legacyContentScale(): Float = when (this) {
        AppIconShape.ORIGINAL -> 1f
        AppIconShape.CIRCLE -> 0.78f
        AppIconShape.SQUIRCLE -> 0.86f
        AppIconShape.ROUNDED_SQUARE -> 0.9f
    }
}
