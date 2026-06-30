package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class FutoSwipeModelFilesTest {
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void missingRootIsNotReady() {
        FutoSwipeModelFiles files = FutoSwipeModelFiles.fromRoot(new File(temp.getRoot(), "missing"));

        assertFalse(files.ready());
        assertNull(files.encoderPath());
    }

    @Test
    public void decoderReturnsEmptyWhenModelsAreNotConfigured() {
        FutoSwipeDecoder decoder = new FutoSwipeDecoder(
                FutoSwipeModelFiles.fromRoot(new File(temp.getRoot(), "missing")));

        List<SwipeCandidate> candidates = decoder.decode(trace(), 4);

        assertTrue(candidates.isEmpty());
    }

    @Test
    public void decoderReturnsEmptyWhenNativeBackendCannotInitialize() throws IOException {
        File root = temp.newFolder("fake-models");
        writeModel(root, "honorable_sturgeon", "model_fp32.pte");
        FutoSwipeDecoder decoder = new FutoSwipeDecoder(FutoSwipeModelFiles.fromRoot(root));

        List<SwipeCandidate> candidates = decoder.decode(trace(), 4);

        assertTrue(candidates.isEmpty());
    }

    @Test
    public void readsReadmeStyleModelDirectories() throws IOException {
        File root = temp.newFolder("models");
        writeModel(root, "encoder_model", "model_fp32.pte");
        writeModel(root, "english_decoder_model", "model_fp32.pte");
        writeModel(root, "english_contextlm", "model_fp32.pte");
        touch(new File(new File(root, "english_contextlm"), "vocab.txt"));

        FutoSwipeModelFiles files = FutoSwipeModelFiles.fromRoot(root);

        assertTrue(files.ready());
        assertNotNull(files.encoderPath());
        assertNotNull(files.decoderPath());
        assertNotNull(files.lmModelPath());
        assertNotNull(files.lmVocabPath());
    }

    @Test
    public void readsHuggingFaceStyleModelDirectories() throws IOException {
        File root = temp.newFolder("models");
        writeModel(root, "honorable_sturgeon", "model_fp32.pte");
        writeModel(root, "magic_macaw", "model_fp32.pte");
        writeModel(root, "hungry_jellyfish", "context_lm.pte");
        touch(new File(new File(root, "hungry_jellyfish"), "vocab.txt"));

        FutoSwipeModelFiles files = FutoSwipeModelFiles.fromRoot(root);

        assertTrue(files.ready());
        assertTrue(files.encoderPath().contains("honorable_sturgeon"));
        assertTrue(files.decoderPath().contains("magic_macaw"));
        assertTrue(files.lmModelPath().contains("hungry_jellyfish"));
    }

    private static void writeModel(File root, String directoryName, String modelFileName) throws IOException {
        File directory = new File(root, directoryName);
        assertTrue(directory.mkdirs());
        touch(new File(directory, modelFileName));
        touch(new File(directory, "metadata.json"));
    }

    private static void touch(File file) throws IOException {
        assertTrue(file.createNewFile());
    }

    private static SwipeTrace trace() {
        SwipeTrace.Builder builder = new SwipeTrace.Builder(0f, 0f, 100f, 100f);
        builder.add(0f, 0f, 0L, key("a"));
        builder.add(40f, 20f, 16L, key("b"));
        builder.add(70f, 50f, 32L, key("c"));
        builder.add(100f, 100f, 48L, key("d"));
        return builder.build();
    }

    private static GestureKey key(String label) {
        return new GestureKey(label, label, label.toUpperCase(), null, null, null, null);
    }
}
