package com.superl3.s3keyboard;

import android.content.Context;

import java.io.File;

final class FutoSwipeModelFiles {
    private static final String MODEL_FILE = "model_fp32.pte";
    private static final String CONTEXT_LM_FILE = "context_lm.pte";
    private static final String METADATA_FILE = "metadata.json";
    private static final String VOCAB_FILE = "vocab.txt";

    final File root;
    final File encoderModel;
    final File decoderModel;
    final File lmModel;
    final File lmVocab;

    private FutoSwipeModelFiles(
            File root,
            File encoderModel,
            File decoderModel,
            File lmModel,
            File lmVocab) {
        this.root = root;
        this.encoderModel = encoderModel;
        this.decoderModel = decoderModel;
        this.lmModel = lmModel;
        this.lmVocab = lmVocab;
    }

    static FutoSwipeModelFiles fromAppFiles(Context context) {
        if (context == null) {
            return fromRoot(null);
        }
        return fromRoot(new File(context.getFilesDir(), "futo-swipe"));
    }

    static FutoSwipeModelFiles fromRoot(File root) {
        if (root == null) {
            return new FutoSwipeModelFiles(null, null, null, null, null);
        }
        File encoder = findModel(root, MODEL_FILE,
                "encoder_model",
                "honorable_sturgeon",
                ".");
        File decoder = findModel(root, MODEL_FILE,
                "english_decoder_model",
                "magic_macaw");
        File lm = findModel(root, CONTEXT_LM_FILE,
                "english_contextlm",
                "hungry_jellyfish");
        if (lm == null) {
            lm = findModel(root, MODEL_FILE,
                    "english_contextlm",
                    "hungry_jellyfish");
        }
        File lmVocab = lm == null ? null : sibling(lm, VOCAB_FILE);
        if (lmVocab != null && !lmVocab.isFile()) {
            lm = null;
            lmVocab = null;
        }
        return new FutoSwipeModelFiles(root, encoder, decoder, lm, lmVocab);
    }

    boolean ready() {
        return encoderModel != null && encoderModel.isFile();
    }

    String encoderPath() {
        return pathOrNull(encoderModel);
    }

    String decoderPath() {
        return pathOrNull(decoderModel);
    }

    String lmModelPath() {
        return pathOrNull(lmModel);
    }

    String lmVocabPath() {
        return pathOrNull(lmVocab);
    }

    private static File findModel(File root, String modelFileName, String... directories) {
        for (String directory : directories) {
            File candidateDirectory = ".".equals(directory) ? root : new File(root, directory);
            File model = new File(candidateDirectory, modelFileName);
            File metadata = new File(candidateDirectory, METADATA_FILE);
            if (model.isFile() && metadata.isFile()) {
                return model;
            }
        }
        return null;
    }

    private static File sibling(File file, String siblingName) {
        File parent = file == null ? null : file.getParentFile();
        return parent == null ? null : new File(parent, siblingName);
    }

    private static String pathOrNull(File file) {
        return file == null || !file.isFile() ? null : file.getAbsolutePath();
    }
}
