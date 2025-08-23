package com.example.zwell;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * AIService - wrapper around Gemini Java SDK (Futures-based usage).
 * <p>
 * Requirements:
 *  - Gradle dependency: com.google.ai.client:generativeai:0.3.0 (or the 0.x Java SDK you used)
 *  - Guava for ListenableFuture support: com.google.guava:guava:31.1-android (or compatible)
 *  - BuildConfig.GEMINI_API_KEY must be provided via build.gradle buildConfigField
 */
public class AIService {
    private static final String TAG = "AIService";

    private final GenerativeModelFutures futuresModel;
    private final Handler mainHandler;

    public interface Callback {
        void onResult(String text);
        void onError(Exception e);
    }

    public AIService(Context ctx) {
        // Read API key from BuildConfig (or pass it in a constructor overload)
        String apiKey = BuildConfig.GEMINI_API_KEY;

        // Create low-level GenerativeModel and a futures wrapper
        GenerativeModel model = new GenerativeModel("gemini-1.5-flash", apiKey);
        futuresModel = GenerativeModelFutures.from(model);

        // Handler so callbacks run on the main/UI thread
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Core helper: build Content, call Gemini via futuresModel, post result to main thread
    private void requestAI(String prompt, Callback cb) {
        try {
            Content content = new Content.Builder()
                    .addText(prompt)
                    .build();

            ListenableFuture<GenerateContentResponse> future = futuresModel.generateContent(content);

            Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String text = "No response.";
                    try {
                        if (result != null && result.getText() != null && !result.getText().isEmpty()) {
                            text = result.getText();
                        }
                    } catch (Exception ignore) { /* fallthrough to default message */ }

                    final String finalText = text;
                    mainHandler.post(() -> cb.onResult(finalText));
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    Log.e(TAG, "AI call failed", t);
                    mainHandler.post(() -> cb.onError(new Exception(t)));
                }
            }, MoreExecutors.directExecutor());

        } catch (Exception e) {
            Log.e(TAG, "requestAI error", e);
            mainHandler.post(() -> cb.onError(e));
        }
    }

    // ---------------- Feature wrappers ----------------

    // Generic text generation (chatbot)
    public void generateText(String prompt, Callback cb) {
        requestAI(prompt, cb);
    }

    // What-If Simulator (structured prompt)
    public void generateWhatIf(String worry, Callback cb) {
        String p = "You are a supportive CBT-aware coach. Given this worry, produce 3 realistic yet optimistic outcomes, "
                + "a short action plan (3 steps), and one-line reframe. Keep language brief and teen-friendly.\nWorry: "
                + worry;
        requestAI(p, cb);
    }

    // Peer-support story
    public void generateStory(String situation, Callback cb) {
        String p = "Write a short relatable fiction (300-600 words) about a young person facing: " + situation
                + ". Show struggle, small steps taken, and a hopeful ending with 3 short takeaways.";
        requestAI(p, cb);
    }

    // Music mood plan (returns a JSON-like plan that the app can parse)
    public void generateMusicPlan(String mood, Callback cb) {
        String p = "Return JSON only (no explanation). Create a short ambient music plan for mood: '" + mood
                + "'. Provide fields: bpm (int), scale (string), chords (array of strings), note_ms (int).";
        requestAI(p, cb);
    }

    // Emotion -> Strict SVG (returns SVG markup ONLY)
    public void generateEmotionArt(String emotion, Callback cb) {
        String p = "Return STRICT SVG XML only (no explanation). viewBox=\"0 0 800 600\". Create a graffiti/comic-style abstract piece"
                + " that visually represents: " + emotion + ".";
        requestAI(p, cb);
    }

    // VR world descriptor (return JSON describing the scene)
    public void generateVRWorld(String mood, Callback cb) {
        String p = "Return JSON only describing a simple VR scene for mood '" + mood + "'. Fields: top (hex color), bottom (hex),"
                + " fog (0.0-1.0), particles (int), affirmation (string).";
        requestAI(p, cb);
    }

    // Mindful prompt / breathing script
    public void generateMindfulPrompt(String feeling, Callback cb) {
        String p = "Create a short 1-minute guided breathing script for someone feeling " + feeling
                + ". Use simple step-by-step instructions (inhale/exhale counts).";
        requestAI(p, cb);
    }
}
