package com.superl3.s3keyboard;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FutoSwipeDecoder implements SwipeDecoder {
    private static final String DECODER_CLASS_NAME = "org.futo.ml.inference.SwipeDecoder";
    private static final int DEFAULT_THREADS = 1;
    private static final int DEFAULT_BEAM_WIDTH = 100;
    private static final boolean DEFAULT_USE_EXPANSION = true;
    private static final String DEFAULT_FREQ_KEY = "f";
    private static final int MIN_POINTS = 4;

    private final FutoSwipeModelFiles modelFiles;
    private Object decoder;
    private Method recognizeMethod;
    private Method closeMethod;
    private Method getWordMethod;
    private Method getScoreMethod;
    private boolean disabled;

    FutoSwipeDecoder(Context context) {
        this(FutoSwipeModelFiles.fromAppFiles(context));
    }

    FutoSwipeDecoder(FutoSwipeModelFiles modelFiles) {
        this.modelFiles = modelFiles;
    }

    @Override
    public List<SwipeCandidate> decode(SwipeTrace trace, int maxCandidates) {
        if (trace == null
                || trace.pointCount() < MIN_POINTS
                || trace.distinctKeyCount() < 2
                || maxCandidates <= 0
                || !ensureDecoder(maxCandidates)) {
            return Collections.emptyList();
        }
        try {
            Object result = recognizeMethod.invoke(
                    decoder,
                    trace.normalizedXArray(),
                    trace.normalizedYArray(),
                    trace.relativeTimeMsArray(),
                    maxCandidates,
                    DEFAULT_BEAM_WIDTH,
                    null);
            return mapResults(result, maxCandidates);
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError e) {
            closeAndDisable();
            return Collections.emptyList();
        }
    }

    private boolean ensureDecoder(int topK) {
        if (disabled || decoder != null) {
            return decoder != null;
        }
        if (modelFiles == null || !modelFiles.ready()) {
            disabled = true;
            return false;
        }
        try {
            Class<?> decoderClass = Class.forName(DECODER_CLASS_NAME);
            Constructor<?> constructor = decoderClass.getConstructor(
                    String.class,
                    String.class,
                    int.class,
                    int.class,
                    int.class,
                    boolean.class,
                    String.class,
                    String.class,
                    String.class);
            decoder = constructor.newInstance(
                    modelFiles.encoderPath(),
                    modelFiles.decoderPath(),
                    DEFAULT_THREADS,
                    DEFAULT_BEAM_WIDTH,
                    Math.max(1, topK),
                    DEFAULT_USE_EXPANSION,
                    DEFAULT_FREQ_KEY,
                    modelFiles.lmModelPath(),
                    modelFiles.lmVocabPath());
            recognizeMethod = decoderClass.getMethod(
                    "recognize",
                    float[].class,
                    float[].class,
                    float[].class,
                    int.class,
                    int.class,
                    float[].class);
            closeMethod = decoderClass.getMethod("close");
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            closeAndDisable();
            return false;
        }
    }

    private List<SwipeCandidate> mapResults(Object result, int maxCandidates) {
        if (!(result instanceof List<?>)) {
            return Collections.emptyList();
        }
        List<?> rawResults = (List<?>) result;
        List<SwipeCandidate> candidates = new ArrayList<>();
        for (Object rawResult : rawResults) {
            SwipeCandidate candidate = mapResult(rawResult);
            if (candidate == null || candidate.word.isEmpty()) {
                continue;
            }
            candidates.add(candidate);
            if (candidates.size() >= maxCandidates) {
                break;
            }
        }
        return candidates;
    }

    private SwipeCandidate mapResult(Object rawResult) {
        if (rawResult == null) {
            return null;
        }
        try {
            if (getWordMethod == null || getScoreMethod == null) {
                Class<?> resultClass = rawResult.getClass();
                getWordMethod = resultClass.getMethod("getWord");
                getScoreMethod = resultClass.getMethod("getScore");
            }
            Object word = getWordMethod.invoke(rawResult);
            Object score = getScoreMethod.invoke(rawResult);
            if (!(word instanceof String) || !(score instanceof Number)) {
                return null;
            }
            return new SwipeCandidate((String) word, ((Number) score).floatValue());
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return null;
        }
    }

    private void closeAndDisable() {
        disabled = true;
        if (decoder == null || closeMethod == null) {
            decoder = null;
            return;
        }
        try {
            closeMethod.invoke(decoder);
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            // Best-effort cleanup only.
        } finally {
            decoder = null;
            recognizeMethod = null;
            closeMethod = null;
            getWordMethod = null;
            getScoreMethod = null;
        }
    }
}
