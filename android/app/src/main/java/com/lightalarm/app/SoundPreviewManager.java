package com.lightalarm.app;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.IOException;

/**
 * Manages sound preview playback with 10-second duration limit
 */
public class SoundPreviewManager {
    private static final String TAG = "SoundPreviewManager";
    private static final int PREVIEW_DURATION_MS = 10000; // 10 seconds
    
    private static SoundPreviewManager instance;
    private Context context;
    private MediaPlayer currentPlayer;
    private Handler previewHandler;
    private Runnable stopPreviewRunnable;
    private boolean isPlaying = false;
    
    private SoundPreviewManager(Context context) {
        this.context = context;
        this.previewHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized SoundPreviewManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundPreviewManager(context);
        }
        return instance;
    }
    
    /**
     * Start playing a sound preview for 10 seconds
     */
    public void startPreview(Sound sound, PreviewCallback callback) {
        Log.d(TAG, "Starting preview for: " + sound.getDisplayName());
        
        // Stop any currently playing preview
        stopPreview();
        
        try {
            // Create new MediaPlayer
            currentPlayer = new MediaPlayer();
            currentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            currentPlayer.setVolume(0.7f, 0.7f); // 70% volume for preview
            
            // Set data source from raw resource
            if (sound.getResourceId() > 0) {
                currentPlayer.setDataSource(context.getResources().openRawResourceFd(sound.getResourceId()));
            } else {
                Log.w(TAG, "Invalid resource ID for sound: " + sound.getDisplayName());
                if (callback != null) {
                    callback.onPreviewError("Invalid sound resource");
                }
                return;
            }
            
            // Set up completion listener
            currentPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "Preview completed naturally");
                    stopPreview();
                    if (callback != null) {
                        callback.onPreviewStopped();
                    }
                }
            });
            
            // Set up error listener
            currentPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "Preview error: " + what + ", " + extra);
                    stopPreview();
                    if (callback != null) {
                        callback.onPreviewError("Playback error");
                    }
                    return true;
                }
            });
            
            // Prepare and start
            currentPlayer.prepare();
            currentPlayer.start();
            isPlaying = true;
            
            if (callback != null) {
                callback.onPreviewStarted();
            }
            
            // Schedule automatic stop after 10 seconds
            stopPreviewRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Auto-stopping preview after 10 seconds");
                    stopPreview();
                    if (callback != null) {
                        callback.onPreviewStopped();
                    }
                }
            };
            previewHandler.postDelayed(stopPreviewRunnable, PREVIEW_DURATION_MS);
            
            Log.d(TAG, "Preview started successfully");
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to start preview", e);
            stopPreview();
            if (callback != null) {
                callback.onPreviewError("Failed to load sound file");
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error starting preview", e);
            stopPreview();
            if (callback != null) {
                callback.onPreviewError("Unexpected error");
            }
        }
    }
    
    /**
     * Stop the current preview
     */
    public void stopPreview() {
        Log.d(TAG, "Stopping preview");
        
        if (stopPreviewRunnable != null) {
            previewHandler.removeCallbacks(stopPreviewRunnable);
            stopPreviewRunnable = null;
        }
        
        if (currentPlayer != null) {
            try {
                if (currentPlayer.isPlaying()) {
                    currentPlayer.stop();
                }
                currentPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping preview", e);
            }
            currentPlayer = null;
        }
        
        isPlaying = false;
    }
    
    /**
     * Check if a preview is currently playing
     */
    public boolean isPlaying() {
        return isPlaying && currentPlayer != null && currentPlayer.isPlaying();
    }
    
    /**
     * Get the currently playing sound (if any)
     */
    public Sound getCurrentSound() {
        // This would require storing the current sound reference
        // For now, return null as we don't need this functionality
        return null;
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        stopPreview();
        if (previewHandler != null) {
            previewHandler.removeCallbacksAndMessages(null);
        }
    }
    
    /**
     * Callback interface for preview events
     */
    public interface PreviewCallback {
        void onPreviewStarted();
        void onPreviewStopped();
        void onPreviewError(String error);
    }
}








