package com.example.zwell;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class MusicActivity extends AppCompatActivity {
    AIService ai;
    private volatile boolean isPlaying = false;
    private AudioTrack track;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_music);
        ai = new AIService(this);

        EditText et = findViewById(R.id.etMood);
        Button btn = findViewById(R.id.btnCompose);
        TextView tv = findViewById(R.id.tvPlan);

        btn.setOnClickListener(v -> {
            if (isPlaying) { stopAudio(); btn.setText("Compose & Play"); return; }
            String mood = et.getText().toString().trim();
            tv.setText("Composing plan…");
            ai.generateMusicPlan(mood, new AIService.Callback() {
                @Override public void onResult(String text) {
                    runOnUiThread(() -> tv.setText(text));
                    try {
                        JSONObject plan = new JSONObject(text);
                        startAudio(plan);
                        runOnUiThread(() -> btn.setText("Stop"));
                    } catch (Exception e) {
                        // fallback: do nothing, printed plan shown for debugging
                    }
                }

                @Override public void onError(Exception e) {
                    runOnUiThread(() -> tv.setText("Error: " + e.getMessage()));
                }
            });
        });
    }

    private void startAudio(JSONObject plan) {
        try {
            int bpm = plan.optInt("bpm", 60);
            JSONArray chords = plan.optJSONArray("chords");
            int noteMs = plan.optInt("note_ms", 500);

            int sampleRate = 22050;
            int bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

            track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
            track.play();
            isPlaying = true;

            new Thread(() -> {
                try {
                    while (isPlaying) {
                        if (chords == null || chords.length() == 0) {
                            writeTone(track, 440.0, noteMs, sampleRate);
                        } else {
                            for (int i = 0; i < chords.length() && isPlaying; i++) {
                                String c = chords.optString(i, "A");
                                double freq = chordToFreq(c);
                                writeTone(track, freq, noteMs, sampleRate);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }).start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private double chordToFreq(String chord) {
        String root = chord.length() > 0 ? chord.substring(0,1).toUpperCase() : "A";
        switch (root) {
            case "C": return 261.63;
            case "D": return 293.66;
            case "E": return 329.63;
            case "F": return 349.23;
            case "G": return 392.00;
            case "A": return 440.00;
            case "B": return 493.88;
            default: return 440.00;
        }
    }

    private void writeTone(AudioTrack t, double freq, int ms, int sr) {
        int samples = (int)((ms/1000.0)*sr);
        short[] buf = new short[samples];
        for (int i = 0; i < samples; i++) {
            double env = Math.min(1.0, i/(sr*0.02)) * Math.min(1.0, (samples-i)/(sr*0.04));
            double v = Math.sin(2*Math.PI*freq*i/sr) * 0.25 * env;
            buf[i] = (short)(v * Short.MAX_VALUE);
        }
        t.write(buf, 0, samples);
    }

    private void stopAudio() {
        isPlaying = false;
        if (track != null) {
            try { track.stop(); track.release(); } catch (Exception ignored) {}
            track = null;
        }
    }

    @Override protected void onDestroy() { stopAudio(); super.onDestroy(); }
}