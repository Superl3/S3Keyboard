package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;

import org.junit.Test;

public final class RemoteKeySessionTest {
    @Test
    public void tappedModifierAppliesToNextKeyThenClears() {
        RemoteKeySession session = new RemoteKeySession();

        session.tapModifier(KeyEvent.META_CTRL_ON);

        assertTrue((session.pendingMetaState() & KeyEvent.META_CTRL_ON) != 0);
        assertEquals(0, session.lockedMetaState());

        int metaState = session.consumeForKey(0);

        assertTrue((metaState & KeyEvent.META_CTRL_ON) != 0);
        assertEquals(0, session.pendingMetaState());
        assertEquals(0, session.lockedMetaState());
    }

    @Test
    public void repeatedTapTogglesPendingModifierOff() {
        RemoteKeySession session = new RemoteKeySession();

        session.tapModifier(KeyEvent.META_ALT_ON);
        session.tapModifier(KeyEvent.META_ALT_ON);

        assertEquals(0, session.pendingMetaState());
        assertEquals(0, session.consumeForKey(0));
    }

    @Test
    public void lockedModifierSurvivesKeyConsumption() {
        RemoteKeySession session = new RemoteKeySession();

        session.toggleLockedModifier(KeyEvent.META_ALT_ON);

        int first = session.consumeForKey(0);
        int second = session.consumeForKey(0);

        assertTrue((first & KeyEvent.META_ALT_ON) != 0);
        assertTrue((second & KeyEvent.META_ALT_ON) != 0);
        assertTrue((session.lockedMetaState() & KeyEvent.META_ALT_ON) != 0);
    }

    @Test
    public void tappingLockedModifierTurnsItOff() {
        RemoteKeySession session = new RemoteKeySession();

        session.toggleLockedModifier(KeyEvent.META_META_ON);
        session.tapModifier(KeyEvent.META_META_ON);

        assertEquals(0, session.pendingMetaState());
        assertEquals(0, session.lockedMetaState());
        assertEquals(0, session.consumeForKey(0));
    }

    @Test
    public void pendingLockedAndExplicitModifiersCompose() {
        RemoteKeySession session = new RemoteKeySession();

        session.toggleLockedModifier(KeyEvent.META_CTRL_ON);
        session.tapModifier(KeyEvent.META_ALT_ON);

        int metaState = session.consumeForKey(KeyEvent.META_SHIFT_ON);

        assertTrue((metaState & KeyEvent.META_CTRL_ON) != 0);
        assertTrue((metaState & KeyEvent.META_ALT_ON) != 0);
        assertTrue((metaState & KeyEvent.META_SHIFT_ON) != 0);
        assertEquals(0, session.pendingMetaState());
        assertTrue((session.lockedMetaState() & KeyEvent.META_CTRL_ON) != 0);
    }

    @Test
    public void resetClearsPendingAndLockedModifiers() {
        RemoteKeySession session = new RemoteKeySession();

        session.tapModifier(KeyEvent.META_CTRL_ON);
        session.toggleLockedModifier(KeyEvent.META_ALT_ON);
        session.reset();

        assertEquals(0, session.pendingMetaState());
        assertEquals(0, session.lockedMetaState());
        assertEquals(0, session.consumeForKey(0));
    }
}
