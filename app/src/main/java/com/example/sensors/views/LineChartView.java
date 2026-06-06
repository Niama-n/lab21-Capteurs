package com.example.sensors.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

import com.example.sensors.utils.UiPalette;

import java.util.ArrayDeque;

public class LineChartView extends View {

    private static final int HISTORY_LENGTH = 80;
    private static final int GRID_DIVISIONS = 4;
    private static final float PAD_LEFT   = 56f;
    private static final float PAD_RIGHT  = 24f;
    private static final float PAD_TOP    = 48f;
    private static final float PAD_BOTTOM = 48f;

    private final ArrayDeque<Float> sampleBuffer = new ArrayDeque<>(HISTORY_LENGTH);
    private final Paint backdropBrush = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisBrush     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridBrush     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokeBrush   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadeBrush    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerBrush   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint captionBrush  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private String emptyHint = "Awaiting incoming samples…";

    public LineChartView(Context ctx) {
        super(ctx);
        configureBrushes();
    }

    public void setEmptyHint(String hint) {
        emptyHint = hint;
    }

    public void addValue(float sample) {
        if (sampleBuffer.size() >= HISTORY_LENGTH) {
            sampleBuffer.removeFirst();
        }
        sampleBuffer.addLast(sample);
        invalidate();
    }

    private void configureBrushes() {
        backdropBrush.setColor(UiPalette.SURFACE_DEEP);
        axisBrush.setColor(UiPalette.AXIS_LINE);
        axisBrush.setStrokeWidth(2f);
        gridBrush.setColor(UiPalette.GRID_LINE);
        gridBrush.setStrokeWidth(1f);
        strokeBrush.setColor(UiPalette.ACCENT_VIOLET);
        strokeBrush.setStrokeWidth(4f);
        strokeBrush.setStyle(Paint.Style.STROKE);
        strokeBrush.setStrokeCap(Paint.Cap.ROUND);
        strokeBrush.setStrokeJoin(Paint.Join.ROUND);
        shadeBrush.setStyle(Paint.Style.FILL);
        markerBrush.setColor(UiPalette.ACCENT_GOLD);
        markerBrush.setStyle(Paint.Style.FILL);
        captionBrush.setColor(UiPalette.TEXT_DIM);
        captionBrush.setTextSize(28f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width  = getWidth();
        int height = getHeight();

        canvas.drawRect(0, 0, width, height, backdropBrush);
        drawGrid(canvas, width, height);
        drawAxes(canvas, width, height);

        if (sampleBuffer.size() < 2) {
            drawPlaceholder(canvas, width, height);
            return;
        }

        float[] bounds = computeVerticalBounds();
        float plotWidth  = width - PAD_LEFT - PAD_RIGHT;
        float plotHeight = height - PAD_TOP - PAD_BOTTOM;

        Path trendPath = new Path();
        Path areaPath  = new Path();
        float lastX = PAD_LEFT;
        float lastY = height - PAD_BOTTOM;

        int idx = 0;
        for (float sample : sampleBuffer) {
            float xPos = PAD_LEFT + idx * (plotWidth / (HISTORY_LENGTH - 1));
            float ratio = (sample - bounds[0]) / (bounds[1] - bounds[0]);
            float yPos = height - PAD_BOTTOM - ratio * plotHeight;

            if (idx == 0) {
                trendPath.moveTo(xPos, yPos);
                areaPath.moveTo(xPos, height - PAD_BOTTOM);
                areaPath.lineTo(xPos, yPos);
            } else {
                trendPath.lineTo(xPos, yPos);
                areaPath.lineTo(xPos, yPos);
            }

            lastX = xPos;
            lastY = yPos;
            idx++;
        }

        areaPath.lineTo(lastX, height - PAD_BOTTOM);
        areaPath.close();

        shadeBrush.setShader(new LinearGradient(
                0, PAD_TOP, 0, height - PAD_BOTTOM,
                Color.parseColor("#66A855F7"),
                Color.parseColor("#006B21A8"),
                Shader.TileMode.CLAMP));
        canvas.drawPath(areaPath, shadeBrush);
        canvas.drawPath(trendPath, strokeBrush);
        drawHighlight(canvas, lastX, lastY);
        drawBoundLabels(canvas, height, bounds[0], bounds[1]);
    }

    private void drawGrid(Canvas canvas, int width, int height) {
        for (int row = 1; row <= GRID_DIVISIONS; row++) {
            float gridY = PAD_TOP + (height - PAD_TOP - PAD_BOTTOM) * row / (float) GRID_DIVISIONS;
            canvas.drawLine(PAD_LEFT, gridY, width - PAD_RIGHT, gridY, gridBrush);
        }
    }

    private void drawAxes(Canvas canvas, int width, int height) {
        canvas.drawLine(PAD_LEFT, height - PAD_BOTTOM, width - PAD_RIGHT, height - PAD_BOTTOM, axisBrush);
        canvas.drawLine(PAD_LEFT, PAD_TOP, PAD_LEFT, height - PAD_BOTTOM, axisBrush);
    }

    private void drawPlaceholder(Canvas canvas, int width, int height) {
        captionBrush.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(emptyHint, width / 2f, height / 2f, captionBrush);
    }

    private float[] computeVerticalBounds() {
        float lower = Float.MAX_VALUE;
        float upper = -Float.MAX_VALUE;
        for (float sample : sampleBuffer) {
            lower = Math.min(lower, sample);
            upper = Math.max(upper, sample);
        }
        if (upper == lower) {
            upper = lower + 1f;
        }
        return new float[]{lower, upper};
    }

    private void drawHighlight(Canvas canvas, float xPos, float yPos) {
        Paint glowBrush = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowBrush.setColor(Color.parseColor("#44FBBF24"));
        canvas.drawCircle(xPos, yPos, 16f, glowBrush);
        canvas.drawCircle(xPos, yPos, 7f, markerBrush);
    }

    private void drawBoundLabels(Canvas canvas, int height, float lower, float upper) {
        captionBrush.setTextAlign(Paint.Align.LEFT);
        captionBrush.setTextSize(24f);
        canvas.drawText(String.format("%.2f", lower), PAD_LEFT + 4, height - PAD_BOTTOM - 6, captionBrush);
        canvas.drawText(String.format("%.2f", upper), PAD_LEFT + 4, PAD_TOP + 24, captionBrush);
    }
}
