package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.text.InputType;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class TextActionProviderTest {
    @Test
    public void requestContainsOnlyResolvedTargetText() {
        String document = "before selected after";
        TextActionRange range = TextActionRange.resolve(document, 0, 7, 15);
        EditorInputPolicy normal = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        TextActionProviderRequest.BuildResult built = TextActionProviderRequest.build(
                TextAction.POLISH, range, normal, false, true, "ko");

        assertNull(built.error);
        assertNotNull(built.request);
        assertEquals("selected", built.request.text);
        assertEquals(TextAction.POLISH, built.request.action);
        assertEquals("ko", built.request.targetLanguage);
    }

    @Test
    public void sensitiveRawNumberAndRemoteRequestsAreDeniedBelowUi() {
        TextActionRange range = TextActionRange.resolve("hello", 0, 0, 5);
        EditorInputPolicy password = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditorInputPolicy number = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_NUMBER);
        EditorInputPolicy uri = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        EditorInputPolicy email = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditorInputPolicy webEdit = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
        EditorInputPolicy raw = EditorInputPolicy.fromInputType(InputType.TYPE_NULL);
        EditorInputPolicy normal = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        assertEquals(TextActionProviderError.DENIED,
                build(TextAction.POLISH, range, password, false, true).error);
        assertEquals(TextActionProviderError.DENIED,
                build(TextAction.POLISH, range, number, false, true).error);
        assertEquals(TextActionProviderError.DENIED,
                build(TextAction.POLISH, range, uri, false, true).error);
        assertEquals(TextActionProviderError.DENIED,
                build(TextAction.POLISH, range, email, false, true).error);
        assertEquals(TextActionProviderError.DENIED,
                build(TextAction.POLISH, range, webEdit, false, true).error);
        assertEquals(TextActionProviderError.DENIED,
                build(TextAction.POLISH, range, raw, false, true).error);
        assertEquals(TextActionProviderError.DENIED,
                build(TextAction.POLISH, range, normal, true, true).error);
        assertEquals(TextActionProviderError.DISABLED,
                build(TextAction.POLISH, range, normal, false, false).error);
    }

    @Test
    public void oversizedProviderPayloadIsRejected() {
        String large = repeat('x', TextActionProviderRequest.MAX_TEXT_CHARS + 1);
        TextActionRange range = TextActionRange.resolve(large, 0, 0, large.length());

        assertEquals(TextActionProviderError.TOO_LARGE,
                build(TextAction.POLISH, range, normalPolicy(), false, true).error);
    }

    @Test
    public void clientDeliversSuccessfulResultAndCancelsTimeout() {
        ManualScheduler scheduler = new ManualScheduler();
        FakeProvider provider = new FakeProvider(true);
        List<TextActionProviderResult> results = new ArrayList<>();
        TextActionProviderClient client = new TextActionProviderClient(provider, scheduler, 1000);

        TextActionProviderClient.Operation operation = client.start(request(TextAction.POLISH), results::add);
        provider.complete(TextActionProviderResult.success("done"));

        assertTrue(operation.isDone());
        assertEquals(1, results.size());
        assertEquals("done", results.get(0).text);
        assertTrue(scheduler.tasks.get(0).cancelled);
    }

    @Test
    public void clientTimeoutCancelsProviderAndReturnsTimeout() {
        ManualScheduler scheduler = new ManualScheduler();
        FakeProvider provider = new FakeProvider(true);
        List<TextActionProviderResult> results = new ArrayList<>();
        TextActionProviderClient client = new TextActionProviderClient(provider, scheduler, 1000);

        client.start(request(TextAction.POLISH), results::add);
        scheduler.runPending();

        assertTrue(provider.cancelled);
        assertEquals(TextActionProviderError.TIMEOUT, results.get(0).error);
    }

    @Test
    public void clientCancellationLeavesProviderCancelled() {
        ManualScheduler scheduler = new ManualScheduler();
        FakeProvider provider = new FakeProvider(true);
        List<TextActionProviderResult> results = new ArrayList<>();
        TextActionProviderClient.Operation operation = new TextActionProviderClient(
                provider, scheduler, 1000).start(request(TextAction.POLISH), results::add);

        operation.cancel();

        assertTrue(provider.cancelled);
        assertEquals(TextActionProviderError.CANCELLED, results.get(0).error);
    }

    @Test
    public void clientRejectsMalformedAndEmptyResults() {
        ManualScheduler malformedScheduler = new ManualScheduler();
        FakeProvider malformedProvider = new FakeProvider(true);
        List<TextActionProviderResult> malformed = new ArrayList<>();
        new TextActionProviderClient(malformedProvider, malformedScheduler, 1000)
                .start(request(TextAction.POLISH), malformed::add);
        malformedProvider.complete(null);

        ManualScheduler emptyScheduler = new ManualScheduler();
        FakeProvider emptyProvider = new FakeProvider(true);
        List<TextActionProviderResult> empty = new ArrayList<>();
        new TextActionProviderClient(emptyProvider, emptyScheduler, 1000)
                .start(request(TextAction.POLISH), empty::add);
        emptyProvider.complete(TextActionProviderResult.success("   "));

        assertEquals(TextActionProviderError.MALFORMED_RESULT, malformed.get(0).error);
        assertEquals(TextActionProviderError.EMPTY_RESULT, empty.get(0).error);
    }

    @Test
    public void unavailableProviderFailsWithoutSchedulingTimeout() {
        ManualScheduler scheduler = new ManualScheduler();
        List<TextActionProviderResult> results = new ArrayList<>();

        new TextActionProviderClient(new FakeProvider(false), scheduler, 1000)
                .start(request(TextAction.POLISH), results::add);

        assertEquals(TextActionProviderError.UNAVAILABLE, results.get(0).error);
        assertTrue(scheduler.tasks.isEmpty());
    }

    @Test
    public void localTestProviderSupportsEveryProviderAction() {
        ImmediateScheduler scheduler = new ImmediateScheduler();
        LocalTestTextActionProvider provider = new LocalTestTextActionProvider(scheduler);
        for (TextAction action : new TextAction[] {
                TextAction.CORRECT, TextAction.POLISH, TextAction.SHORTER,
                TextAction.POLITE, TextAction.TRANSLATE}) {
            List<TextActionProviderResult> results = new ArrayList<>();
            provider.request(request(action), results::add);
            assertEquals(1, results.size());
            assertTrue(results.get(0).succeeded());
            assertFalse(results.get(0).text.trim().isEmpty());
        }
    }

    private static TextActionProviderRequest.BuildResult build(
            TextAction action,
            TextActionRange range,
            EditorInputPolicy policy,
            boolean remote,
            boolean enabled) {
        return TextActionProviderRequest.build(action, range, policy, remote, enabled, "ko");
    }

    private static TextActionProviderRequest request(TextAction action) {
        TextActionRange range = TextActionRange.resolve("teh sample", 0, 0, 10);
        return build(action, range, normalPolicy(), false, true).request;
    }

    private static EditorInputPolicy normalPolicy() {
        return EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);
    }

    private static String repeat(char value, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    private static final class FakeProvider implements TextActionProvider {
        final boolean available;
        Callback callback;
        boolean cancelled;

        FakeProvider(boolean available) {
            this.available = available;
        }

        @Override
        public String id() {
            return "fake";
        }

        @Override
        public boolean isAvailable() {
            return available;
        }

        @Override
        public RequestHandle request(TextActionProviderRequest request, Callback callback) {
            this.callback = callback;
            return () -> cancelled = true;
        }

        void complete(TextActionProviderResult result) {
            callback.onResult(result);
        }
    }

    private static final class ManualScheduler implements TextActionTaskScheduler {
        final List<Task> tasks = new ArrayList<>();

        @Override
        public CancelHandle schedule(Runnable runnable, long delayMs) {
            Task task = new Task(runnable);
            tasks.add(task);
            return () -> task.cancelled = true;
        }

        void runPending() {
            for (Task task : tasks) {
                if (!task.cancelled) {
                    task.runnable.run();
                }
            }
        }
    }

    private static final class ImmediateScheduler implements TextActionTaskScheduler {
        @Override
        public CancelHandle schedule(Runnable runnable, long delayMs) {
            runnable.run();
            return () -> { };
        }
    }

    private static final class Task {
        final Runnable runnable;
        boolean cancelled;

        Task(Runnable runnable) {
            this.runnable = runnable;
        }
    }
}
