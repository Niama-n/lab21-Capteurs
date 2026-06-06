package com.example.sensors.utils;

public final class MotionHeuristics {

  public static final int RING_CAPACITY = 30;
  private static final float SPIKE_GATE      = 5f * 2f;
  private static final float WALK_DISPERSION = 1f + 0.2f;
  private static final float FLAT_TILT_Z     = 4f * 2f;
  private static final float UPRIGHT_TILT    = 3.5f * 2f;

  private MotionHeuristics() {}

  public enum PostureKind {
    CALIBRATING,
    JUMPING,
    WALKING,
    FLAT,
    UPRIGHT,
    IDLE
  }

  public static PostureKind evaluate(
      float[] energyRing, int sampleCount, float axisX, float axisY, float axisZ) {

    if (sampleCount < RING_CAPACITY) {
      return PostureKind.CALIBRATING;
    }

    float maxImpulse = SignalMath.peakValue(energyRing, sampleCount);
    float dispersion = SignalMath.populationStdDev(energyRing, sampleCount);

    if (maxImpulse > SPIKE_GATE) {
      return PostureKind.JUMPING;
    }
    if (dispersion > WALK_DISPERSION) {
      return PostureKind.WALKING;
    }
    if (Math.abs(axisZ) > FLAT_TILT_Z) {
      return PostureKind.FLAT;
    }
    if (Math.abs(axisY) > UPRIGHT_TILT || Math.abs(axisX) > UPRIGHT_TILT) {
      return PostureKind.UPRIGHT;
    }
    return PostureKind.IDLE;
  }

  public static String labelFor(PostureKind kind) {
    switch (kind) {
      case JUMPING:     return "🦘 Jumping";
      case WALKING:     return "🚶 Walking";
      case FLAT:        return "📱 Flat / stationary";
      case UPRIGHT:     return "🧍 Sitting or standing";
      case IDLE:        return "⏸ Idle posture";
      default:          return "…";
    }
  }
}
