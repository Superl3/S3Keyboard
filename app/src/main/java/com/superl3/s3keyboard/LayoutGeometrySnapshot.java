package com.superl3.s3keyboard;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable copy of the geometry produced by the real keyboard layout calculator. */
final class LayoutGeometrySnapshot {
    static final class Slot {
        final String keyId;
        final RectF visualRect;
        final RectF hitRect;
        final float gestureOriginX;
        final float gestureOriginY;

        Slot(String keyId, RectF visualRect, RectF hitRect, float gestureOriginX, float gestureOriginY) {
            this.keyId = keyId == null ? "" : keyId;
            this.visualRect = new RectF(visualRect);
            this.hitRect = new RectF(hitRect);
            this.gestureOriginX = gestureOriginX;
            this.gestureOriginY = gestureOriginY;
        }
    }

    final List<Slot> slots;
    final RectF visualBounds;
    final RectF hitBounds;

    private LayoutGeometrySnapshot(List<Slot> slots, RectF visualBounds, RectF hitBounds) {
        this.slots = Collections.unmodifiableList(slots);
        this.visualBounds = visualBounds;
        this.hitBounds = hitBounds;
    }

    static LayoutGeometrySnapshot empty() {
        return new LayoutGeometrySnapshot(new ArrayList<>(), new RectF(), new RectF());
    }

    static LayoutGeometrySnapshot from(List<HangulKeyboardView.KeySlot> source) {
        return from(source, 0f, 0f);
    }

    static LayoutGeometrySnapshot from(
            List<HangulKeyboardView.KeySlot> source,
            float offsetX,
            float offsetY) {
        if (source == null || source.isEmpty()) {
            return empty();
        }
        List<Slot> copied = new ArrayList<>(source.size());
        RectF visualBounds = null;
        RectF hitBounds = null;
        for (HangulKeyboardView.KeySlot sourceSlot : source) {
            // KeySlot exposes its live rectangles. Copy before applying the
            // preview-to-overlay offset so the editor never moves the real
            // keyboard geometry while drawing the diagnostic silhouette.
            RectF visual = new RectF(sourceSlot.visualBounds());
            RectF hit = new RectF(sourceSlot.hitBounds());
            visual.offset(offsetX, offsetY);
            hit.offset(offsetX, offsetY);
            Slot slot = new Slot(
                    sourceSlot.debugId(),
                    visual,
                    hit,
                    sourceSlot.gestureOriginX + offsetX,
                    sourceSlot.gestureOriginY + offsetY);
            copied.add(slot);
            if (visualBounds == null) {
                visualBounds = new RectF(visual);
                hitBounds = new RectF(hit);
            } else {
                visualBounds.union(visual);
                hitBounds.union(hit);
            }
        }
        return new LayoutGeometrySnapshot(copied, visualBounds, hitBounds);
    }
}
