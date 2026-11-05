package com.moinofactory.moinogeo.demo.shared.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
internal val Icons.flatMap: ImageVector
    get() {
        if (_flatMap != null) {
            return _flatMap!!
        }
        _flatMap =
            ImageVector.Builder(
                name = "map",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(15f, 21f)
                        lineTo(9f, 18.9f)
                        lineTo(4.35f, 20.7f)
                        quadTo(3.85f, 20.9f, 3.43f, 20.59f)
                        reflectiveQuadTo(3f, 19.75f)
                        verticalLineToRelative(-14f)
                        quadTo(3f, 5.43f, 3.19f, 5.18f)
                        reflectiveQuadTo(3.7f, 4.8f)
                        lineTo(9f, 3f)
                        lineToRelative(6f, 2.1f)
                        lineTo(19.65f, 3.3f)
                        quadToRelative(0.5f, -0.2f, 0.93f, 0.11f)
                        reflectiveQuadTo(21f, 4.25f)
                        verticalLineToRelative(14f)
                        quadToRelative(0f, 0.32f, -0.19f, 0.57f)
                        reflectiveQuadTo(20.3f, 19.2f)
                        lineTo(15f, 21f)
                        close()
                        moveTo(14f, 18.55f)
                        verticalLineTo(6.85f)
                        lineTo(10f, 5.45f)
                        verticalLineToRelative(11.7f)
                        lineToRelative(4f, 1.4f)
                        close()
                        moveToRelative(2f, 0f)
                        lineToRelative(3f, -1f)
                        verticalLineTo(5.7f)
                        lineTo(16f, 6.85f)
                        verticalLineToRelative(11.7f)
                        close()
                        moveTo(5f, 18.3f)
                        lineTo(8f, 17.15f)
                        verticalLineTo(5.45f)
                        lineToRelative(-3f, 1f)
                        verticalLineTo(18.3f)
                        close()
                        moveTo(16f, 6.85f)
                        verticalLineToRelative(11.7f)
                        verticalLineTo(6.85f)
                        close()
                        moveTo(8f, 5.45f)
                        verticalLineToRelative(11.7f)
                        verticalLineTo(5.45f)
                        close()
                    }
                }
                .build()
        return _flatMap!!
    }

private var _flatMap: ImageVector? = null

@Suppress("CheckReturnValue")
internal val Icons.azimuthalMap: ImageVector
    get() {
        if (_azimuthalMap != null) {
            return _azimuthalMap!!
        }
        _azimuthalMap =
            ImageVector.Builder(
                name = "public",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(8.1f, 21.21f)
                        quadTo(6.28f, 20.43f, 4.93f, 19.08f)
                        quadTo(3.58f, 17.73f, 2.79f, 15.9f)
                        reflectiveQuadTo(2f, 12f)
                        quadTo(2f, 9.92f, 2.79f, 8.1f)
                        quadTo(3.58f, 6.27f, 4.93f, 4.93f)
                        quadTo(6.28f, 3.57f, 8.1f, 2.79f)
                        quadTo(9.93f, 2f, 12f, 2f)
                        reflectiveQuadToRelative(3.9f, 0.79f)
                        reflectiveQuadToRelative(3.17f, 2.14f)
                        quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
                        quadTo(22f, 9.92f, 22f, 12f)
                        reflectiveQuadToRelative(-0.79f, 3.9f)
                        reflectiveQuadToRelative(-2.14f, 3.17f)
                        quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
                        reflectiveQuadTo(12f, 22f)
                        quadTo(9.93f, 22f, 8.1f, 21.21f)
                        close()
                        moveTo(11f, 19.95f)
                        verticalLineTo(18f)
                        quadTo(10.18f, 18f, 9.59f, 17.41f)
                        reflectiveQuadTo(9f, 16f)
                        verticalLineTo(15f)
                        lineTo(4.2f, 10.2f)
                        quadTo(4.13f, 10.65f, 4.06f, 11.1f)
                        reflectiveQuadTo(4f, 12f)
                        quadToRelative(0f, 3.03f, 1.99f, 5.3f)
                        reflectiveQuadTo(11f, 19.95f)
                        close()
                        moveTo(17.9f, 17.4f)
                        quadToRelative(1.02f, -1.13f, 1.56f, -2.51f)
                        reflectiveQuadTo(20f, 12f)
                        quadTo(20f, 9.55f, 18.64f, 7.52f)
                        quadTo(17.28f, 5.5f, 15f, 4.6f)
                        verticalLineTo(5f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(13f, 7f)
                        horizontalLineTo(11f)
                        verticalLineTo(9f)
                        quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                        reflectiveQuadTo(10f, 10f)
                        horizontalLineTo(8f)
                        verticalLineToRelative(2f)
                        horizontalLineToRelative(6f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(15f, 13f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(1f)
                        quadToRelative(0.65f, 0f, 1.18f, 0.39f)
                        reflectiveQuadTo(17.9f, 17.4f)
                        close()
                    }
                }
                .build()
        return _azimuthalMap!!
    }

private var _azimuthalMap: ImageVector? = null

@Suppress("CheckReturnValue")
internal val Icons.sphereMap: ImageVector
    get() {
        if (_sphereMap != null) {
            return _sphereMap!!
        }
        _sphereMap =
            ImageVector.Builder(
                name = "language",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(8.13f, 21.21f)
                        quadTo(6.3f, 20.43f, 4.94f, 19.06f)
                        quadTo(3.58f, 17.7f, 2.79f, 15.88f)
                        reflectiveQuadTo(2f, 11.99f)
                        reflectiveQuadTo(2.79f, 8.11f)
                        reflectiveQuadTo(4.94f, 4.94f)
                        reflectiveQuadTo(8.13f, 2.79f)
                        reflectiveQuadTo(12.01f, 2f)
                        reflectiveQuadToRelative(3.88f, 0.79f)
                        reflectiveQuadToRelative(3.17f, 2.15f)
                        reflectiveQuadToRelative(2.15f, 3.17f)
                        reflectiveQuadTo(22f, 11.99f)
                        reflectiveQuadToRelative(-0.79f, 3.89f)
                        reflectiveQuadToRelative(-2.15f, 3.19f)
                        reflectiveQuadToRelative(-3.17f, 2.15f)
                        reflectiveQuadTo(12.01f, 22f)
                        reflectiveQuadTo(8.13f, 21.21f)
                        close()
                        moveTo(12f, 19.95f)
                        quadToRelative(0.65f, -0.9f, 1.13f, -1.88f)
                        reflectiveQuadTo(13.9f, 16f)
                        horizontalLineTo(10.1f)
                        quadToRelative(0.3f, 1.1f, 0.77f, 2.07f)
                        reflectiveQuadTo(12f, 19.95f)
                        close()
                        moveTo(9.4f, 19.55f)
                        quadTo(8.95f, 18.73f, 8.61f, 17.84f)
                        reflectiveQuadTo(8.05f, 16f)
                        horizontalLineTo(5.1f)
                        quadToRelative(0.72f, 1.25f, 1.81f, 2.18f)
                        reflectiveQuadTo(9.4f, 19.55f)
                        close()
                        moveToRelative(5.2f, 0f)
                        quadTo(16f, 19.1f, 17.09f, 18.18f)
                        reflectiveQuadTo(18.9f, 16f)
                        horizontalLineTo(15.95f)
                        quadToRelative(-0.22f, 0.95f, -0.56f, 1.84f)
                        quadToRelative(-0.34f, 0.89f, -0.79f, 1.71f)
                        close()
                        moveTo(4.25f, 14f)
                        horizontalLineToRelative(3.4f)
                        quadTo(7.58f, 13.5f, 7.54f, 13.01f)
                        reflectiveQuadTo(7.5f, 12f)
                        reflectiveQuadTo(7.54f, 10.99f)
                        reflectiveQuadTo(7.65f, 10f)
                        horizontalLineTo(4.25f)
                        quadTo(4.13f, 10.5f, 4.06f, 10.99f)
                        quadTo(4f, 11.48f, 4f, 12f)
                        quadToRelative(0f, 0.52f, 0.06f, 1.01f)
                        reflectiveQuadTo(4.25f, 14f)
                        close()
                        moveToRelative(5.4f, 0f)
                        horizontalLineToRelative(4.7f)
                        quadToRelative(0.07f, -0.5f, 0.11f, -0.99f)
                        reflectiveQuadTo(14.5f, 12f)
                        reflectiveQuadTo(14.46f, 10.99f)
                        reflectiveQuadTo(14.35f, 10f)
                        horizontalLineTo(9.65f)
                        quadTo(9.58f, 10.5f, 9.54f, 10.99f)
                        quadTo(9.5f, 11.48f, 9.5f, 12f)
                        quadToRelative(0f, 0.52f, 0.04f, 1.01f)
                        quadTo(9.58f, 13.5f, 9.65f, 14f)
                        close()
                        moveToRelative(6.7f, 0f)
                        horizontalLineToRelative(3.4f)
                        quadToRelative(0.13f, -0.5f, 0.19f, -0.99f)
                        reflectiveQuadTo(20f, 12f)
                        reflectiveQuadTo(19.94f, 10.99f)
                        reflectiveQuadTo(19.75f, 10f)
                        horizontalLineToRelative(-3.4f)
                        quadToRelative(0.07f, 0.5f, 0.11f, 0.99f)
                        quadTo(16.5f, 11.48f, 16.5f, 12f)
                        quadToRelative(0f, 0.52f, -0.04f, 1.01f)
                        reflectiveQuadTo(16.35f, 14f)
                        close()
                        moveTo(15.95f, 8f)
                        horizontalLineTo(18.9f)
                        quadTo(18.18f, 6.75f, 17.09f, 5.82f)
                        reflectiveQuadTo(14.6f, 4.45f)
                        quadToRelative(0.45f, 0.82f, 0.79f, 1.71f)
                        reflectiveQuadTo(15.95f, 8f)
                        close()
                        moveTo(10.1f, 8f)
                        horizontalLineToRelative(3.8f)
                        quadTo(13.6f, 6.9f, 13.13f, 5.93f)
                        reflectiveQuadTo(12f, 4.05f)
                        quadToRelative(-0.65f, 0.9f, -1.13f, 1.88f)
                        reflectiveQuadTo(10.1f, 8f)
                        close()
                        moveToRelative(-5f, 0f)
                        horizontalLineTo(8.05f)
                        quadTo(8.28f, 7.05f, 8.61f, 6.16f)
                        reflectiveQuadTo(9.4f, 4.45f)
                        quadTo(8f, 4.9f, 6.91f, 5.82f)
                        reflectiveQuadTo(5.1f, 8f)
                        close()
                    }
                }
                .build()
        return _sphereMap!!
    }

private var _sphereMap: ImageVector? = null