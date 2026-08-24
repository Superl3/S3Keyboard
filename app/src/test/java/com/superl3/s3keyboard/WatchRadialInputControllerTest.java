package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class WatchRadialInputControllerTest {
    @Test
    public void startsOnConsonantPageAndRequiresKeyThenAction() {
        WatchRadialInputController controller = new WatchRadialInputController();

        assertEquals(WatchRadialPage.CONSONANTS, controller.page());
        assertEquals(WatchRadialInputController.Stage.SELECT_KEY, controller.stage());
        assertTrue(controller.selectKey(0));
        assertEquals(WatchRadialInputController.Stage.SELECT_ACTION, controller.stage());
        assertEquals("ㄱ", controller.commit(GestureAction.TAP));
        assertEquals(WatchRadialInputController.Stage.SELECT_KEY, controller.stage());
    }

    @Test
    public void cyclesAcrossConsonantVowelAndCommandPages() {
        WatchRadialInputController controller = new WatchRadialInputController();

        controller.cyclePage();
        assertEquals(WatchRadialPage.VOWELS, controller.page());
        controller.cyclePage();
        assertEquals(WatchRadialPage.COMMANDS, controller.page());
        controller.cyclePage();
        assertEquals(WatchRadialPage.CONSONANTS, controller.page());
    }

    @Test
    public void vowelPageUsesActualDingulDirectionalMappings() {
        WatchRadialInputController controller = new WatchRadialInputController();
        controller.showPage(WatchRadialPage.VOWELS);

        assertTrue(controller.selectKey(1));
        assertEquals("ㅓ", controller.commit(GestureAction.LEFT));
        assertTrue(controller.selectKey(1));
        assertEquals("ㅏ", controller.commit(GestureAction.RIGHT));
        assertTrue(controller.selectKey(3));
        assertEquals("ㅛ", controller.commit(GestureAction.UP));
        assertTrue(controller.selectKey(3));
        assertEquals("ㅠ", controller.commit(GestureAction.DOWN));
    }

    @Test
    public void vowelPagePreservesTheOriginalFourKeyVerticalOrder() {
        WatchRadialInputController controller = new WatchRadialInputController();
        controller.showPage(WatchRadialPage.VOWELS);

        assertEquals(7, controller.keys().size());
        assertEquals("ㅢ", controller.keys().get(0).label);
        assertEquals("ㅣ.", controller.keys().get(1).label);
        assertEquals("ㅡㅐ", controller.keys().get(2).label);
        assertEquals(". .", controller.keys().get(3).label);
        assertEquals("?", controller.keys().get(4).label);
        assertEquals(".", controller.keys().get(5).label);
        assertEquals("/", controller.keys().get(6).label);
    }

    @Test
    public void persistentUtilitiesKeepSpaceAndDeleteOutsideTheCommandRing() {
        assertEquals(2, KeyboardLayoutFactory.watchPersistentUtilityKeys().size());
        assertEquals(
                KeyboardCommands.CMD_SPACE,
                KeyboardLayoutFactory.watchPersistentUtilityKeys().get(0).tap);
        assertEquals(
                KeyboardCommands.CMD_DELETE,
                KeyboardLayoutFactory.watchPersistentUtilityKeys().get(1).tap);

        for (GestureKey key : KeyboardLayoutFactory.watchRadialKeys(WatchRadialPage.COMMANDS)) {
            assertFalse(KeyboardCommands.CMD_SPACE.equals(key.tap));
            assertFalse(KeyboardCommands.CMD_DELETE.equals(key.tap));
        }
        for (GestureKey key : KeyboardLayoutFactory.watchRadialKeys(WatchRadialPage.VOWELS)) {
            assertFalse(KeyboardCommands.CMD_DELETE.equals(key.tap));
        }
    }

    @Test
    public void commandPageRoutesThroughExistingKeyboardCommands() {
        WatchRadialInputController controller = new WatchRadialInputController();
        controller.showPage(WatchRadialPage.COMMANDS);

        assertTrue(controller.selectKey(0));
        assertEquals(KeyboardCommands.CMD_ENTER, controller.commit(GestureAction.TAP));
        assertTrue(controller.selectKey(2));
        assertEquals(KeyboardCommands.CMD_RESERVED_PHRASES, controller.commit(GestureAction.TAP));
    }

    @Test
    public void invalidSelectionDoesNotChangeState() {
        WatchRadialInputController controller = new WatchRadialInputController();

        assertFalse(controller.selectKey(-1));
        assertFalse(controller.selectKey(8));
        assertNull(controller.commit(GestureAction.TAP));
        assertEquals(WatchRadialInputController.Stage.SELECT_KEY, controller.stage());
    }

    @Test
    public void directionResolverUsesTapDeadZoneAndDominantAxis() {
        assertEquals(GestureAction.TAP,
                WatchRadialInputController.actionForDelta(3, 4, 10));
        assertEquals(GestureAction.LEFT,
                WatchRadialInputController.actionForDelta(-20, 4, 10));
        assertEquals(GestureAction.RIGHT,
                WatchRadialInputController.actionForDelta(20, 4, 10));
        assertEquals(GestureAction.UP,
                WatchRadialInputController.actionForDelta(4, -20, 10));
        assertEquals(GestureAction.DOWN,
                WatchRadialInputController.actionForDelta(4, 20, 10));
    }
}
