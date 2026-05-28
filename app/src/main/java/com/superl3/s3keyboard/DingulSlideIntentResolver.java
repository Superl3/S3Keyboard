package com.superl3.s3keyboard;

import java.util.List;

final class DingulSlideIntentResolver {
    private static final float CANDIDATE_SLOP_MULTIPLIER = 1.5f;
    private static final float CURRENT_KEY_BONUS = 0.25f;
    private static final float CONTAINED_DOWN_BONUS = 0.65f;
    private static final float EXPANDED_DOWN_BONUS = 0.30f;
    private static final float ACTION_EXIT_BONUS = 0.15f;
    private static final float DISTANCE_PENALTY = 0.55f;
    private static final float MIN_CURRENT_SCORE = 0.95f;
    private static final float MIN_REASSIGNED_SCORE = 1.15f;
    private static final float MIN_SHADOW_CURRENT_SCORE = 0.58f;
    private static final float MIN_SHADOW_REASSIGNED_SCORE = 0.74f;

    private DingulSlideIntentResolver() {
    }

    static <T extends Target> Result<T> resolve(
            List<T> targets,
            T current,
            float downX,
            float downY,
            float adjustedDownX,
            float adjustedDownY,
            float upX,
            float upY,
            float hitSlop,
            Policy policy) {
        return resolveCandidate(
                targets,
                current,
                downX,
                downY,
                adjustedDownX,
                adjustedDownY,
                upX,
                upY,
                hitSlop,
                policy,
                false);
    }

    static <T extends Target> Result<T> resolveShadow(
            List<T> targets,
            T current,
            float downX,
            float downY,
            float adjustedDownX,
            float adjustedDownY,
            float upX,
            float upY,
            float hitSlop,
            Policy policy) {
        return resolveCandidate(
                targets,
                current,
                downX,
                downY,
                adjustedDownX,
                adjustedDownY,
                upX,
                upY,
                hitSlop,
                policy,
                true);
    }

    private static <T extends Target> Result<T> resolveCandidate(
            List<T> targets,
            T current,
            float downX,
            float downY,
            float adjustedDownX,
            float adjustedDownY,
            float upX,
            float upY,
            float hitSlop,
            Policy policy,
            boolean shadow) {
        if (targets == null || current == null || policy == null
                || !policy.isDingulTypingKey(current.key())) {
            return null;
        }

        float dx = upX - downX;
        float dy = upY - downY;
        float candidateSlop = Math.max(0f, hitSlop) * CANDIDATE_SLOP_MULTIPLIER;
        Result<T> best = null;
        for (T target : targets) {
            if (!policy.isDingulTypingKey(target.key())
                    || !isNearCandidate(target, downX, downY, adjustedDownX, adjustedDownY, candidateSlop)) {
                continue;
            }
            GestureAction action = shadow
                    ? policy.shadowActionFor(target.key(), dx, dy)
                    : policy.actionFor(target.key(), dx, dy);
            if (action == GestureAction.TAP || !policy.hasOutput(target.key(), action)) {
                continue;
            }
            float threshold = shadow
                    ? policy.shadowThresholdPx(target.key(), action)
                    : policy.thresholdPx(target.key(), action);
            float score = score(
                    target,
                    target == current,
                    action,
                    downX,
                    downY,
                    adjustedDownX,
                    adjustedDownY,
                    upX,
                    upY,
                    dx,
                    dy,
                    threshold,
                    candidateSlop);
            if (best == null || score > best.score) {
                best = new Result<>(target, action, score);
            }
        }
        if (best == null) {
            return null;
        }
        float minimumScore = minimumScore(best.target == current, shadow);
        return best.score >= minimumScore ? best : null;
    }

    private static float minimumScore(boolean current, boolean shadow) {
        if (shadow) {
            return current ? MIN_SHADOW_CURRENT_SCORE : MIN_SHADOW_REASSIGNED_SCORE;
        }
        return current ? MIN_CURRENT_SCORE : MIN_REASSIGNED_SCORE;
    }

    private static <T extends Target> boolean isNearCandidate(
            T target,
            float downX,
            float downY,
            float adjustedDownX,
            float adjustedDownY,
            float slop) {
        return target.contains(downX, downY)
                || target.contains(adjustedDownX, adjustedDownY)
                || target.expandedContains(downX, downY, slop)
                || target.expandedContains(adjustedDownX, adjustedDownY, slop);
    }

    private static <T extends Target> float score(
            T target,
            boolean current,
            GestureAction action,
            float downX,
            float downY,
            float adjustedDownX,
            float adjustedDownY,
            float upX,
            float upY,
            float dx,
            float dy,
            float threshold,
            float slop) {
        float axisDistance = action == GestureAction.LEFT || action == GestureAction.RIGHT
                ? Math.abs(dx)
                : Math.abs(dy);
        float score = axisDistance / Math.max(1f, threshold);
        if (target.contains(downX, downY) || target.contains(adjustedDownX, adjustedDownY)) {
            score += CONTAINED_DOWN_BONUS;
        } else if (target.expandedContains(downX, downY, slop)
                || target.expandedContains(adjustedDownX, adjustedDownY, slop)) {
            score += EXPANDED_DOWN_BONUS;
        }
        if (current) {
            score += CURRENT_KEY_BONUS;
        }
        if (exitsTowardAction(target, action, upX, upY)) {
            score += ACTION_EXIT_BONUS;
        }
        float normalizedDistance = (float) Math.sqrt(target.distanceSquaredTo(adjustedDownX, adjustedDownY))
                / Math.max(1f, Math.min(target.width(), target.height()));
        return score - normalizedDistance * DISTANCE_PENALTY;
    }

    private static boolean exitsTowardAction(Target target, GestureAction action, float upX, float upY) {
        switch (action) {
            case UP:
                return upY <= target.centerY();
            case DOWN:
                return upY >= target.centerY();
            case LEFT:
                return upX <= target.centerX();
            case RIGHT:
                return upX >= target.centerX();
            default:
                return false;
        }
    }

    interface Target {
        GestureKey key();

        boolean contains(float x, float y);

        boolean expandedContains(float x, float y, float slop);

        float distanceSquaredTo(float x, float y);

        float width();

        float height();

        float centerX();

        float centerY();
    }

    interface Policy {
        boolean isDingulTypingKey(GestureKey key);

        GestureAction actionFor(GestureKey key, float dx, float dy);

        default GestureAction shadowActionFor(GestureKey key, float dx, float dy) {
            return actionFor(key, dx, dy);
        }

        float thresholdPx(GestureKey key, GestureAction action);

        default float shadowThresholdPx(GestureKey key, GestureAction action) {
            return thresholdPx(key, action);
        }

        boolean hasOutput(GestureKey key, GestureAction action);
    }

    static final class Result<T extends Target> {
        final T target;
        final GestureAction action;
        final float score;

        Result(T target, GestureAction action, float score) {
            this.target = target;
            this.action = action == null ? GestureAction.TAP : action;
            this.score = score;
        }
    }
}
