package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class RepeatControllerTest {
    @Test
    public void repeatStateIsScopedToTheTouchThatStartedIt() {
        FakeScheduler scheduler = new FakeScheduler();
        List<String> emitted = new ArrayList<>();
        RepeatController controller = new RepeatController(scheduler, emitted::add);
        Object firstTouch = new Object();
        Object secondTouch = new Object();

        controller.start(firstTouch, "\u314F", 100, 50, false);

        assertFalse(controller.hasFired(firstTouch));
        assertFalse(controller.hasFired(secondTouch));

        scheduler.runScheduled();

        assertEquals(1, emitted.size());
        assertEquals("\u314F", emitted.get(0));
        assertTrue(controller.hasFired(firstTouch));
        assertFalse(controller.hasFired(secondTouch));

        controller.stop(secondTouch);
        assertTrue(controller.isRepeating());

        scheduler.runScheduled();
        assertEquals(2, emitted.size());

        controller.stop(firstTouch);
        assertFalse(controller.isRepeating());

        scheduler.runScheduled();
        assertEquals(2, emitted.size());
    }

    @Test
    public void stopDuringRepeatCallbackPreventsReschedule() {
        FakeScheduler scheduler = new FakeScheduler();
        List<String> emitted = new ArrayList<>();
        RepeatController[] holder = new RepeatController[1];
        holder[0] = new RepeatController(scheduler, value -> {
            emitted.add(value);
            holder[0].stop();
        });

        holder[0].start(new Object(), "\u314F", 100, 50, false);
        scheduler.runScheduled();

        assertEquals(1, emitted.size());
        assertFalse(holder[0].isRepeating());
        assertFalse(scheduler.hasScheduled());
    }

    @Test
    public void stopDuringImmediateRepeatPreventsInitialSchedule() {
        FakeScheduler scheduler = new FakeScheduler();
        List<String> emitted = new ArrayList<>();
        RepeatController[] holder = new RepeatController[1];
        holder[0] = new RepeatController(scheduler, value -> {
            emitted.add(value);
            holder[0].stop();
        });

        holder[0].start(new Object(), "\u314F", 100, 50, true);

        assertEquals(1, emitted.size());
        assertFalse(holder[0].isRepeating());
        assertFalse(scheduler.hasScheduled());
    }

    private static final class FakeScheduler implements RepeatController.Scheduler {
        private Runnable scheduled;

        @Override
        public void postDelayed(Runnable runnable, int delayMs) {
            scheduled = runnable;
        }

        @Override
        public void removeCallbacks(Runnable runnable) {
            if (scheduled == runnable) {
                scheduled = null;
            }
        }

        boolean hasScheduled() {
            return scheduled != null;
        }

        void runScheduled() {
            Runnable runnable = scheduled;
            scheduled = null;
            if (runnable != null) {
                runnable.run();
            }
        }
    }
}
