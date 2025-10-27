package com.lightalarm.app;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages sound categorization and provides access to organized sound collections
 */
public class SoundManager {
    private static final String TAG = "SoundManager";
    private static SoundManager instance;
    private Map<String, List<Sound>> categorizedSounds;
    private Context context;
    
    private SoundManager(Context context) {
        this.context = context;
        this.categorizedSounds = new HashMap<>();
        initializeSounds();
    }
    
    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }
    
    /**
     * Initialize sounds by scanning raw resources and categorizing them
     */
    private void initializeSounds() {
        Log.d(TAG, "Initializing sound categories...");
        
        // Initialize categories
        categorizedSounds.put("Ambience", new ArrayList<>());
        categorizedSounds.put("Classic Alarm", new ArrayList<>());
        categorizedSounds.put("Natural Sound", new ArrayList<>());
        
        // Get all raw resource names and categorize them
        try {
            // Use reflection to get raw resource names
            Class<?> rawClass = Class.forName(context.getPackageName() + ".R$raw");
            java.lang.reflect.Field[] fields = rawClass.getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                try {
                    String resourceName = field.getName();
                    int resourceId = field.getInt(null);
                    
                    // Categorize based on filename prefix
                    String category = categorizeSound(resourceName);
                    if (category != null) {
                        String displayName = generateDisplayName(resourceName);
                        Sound sound = new Sound(displayName, resourceName, resourceId, category);
                        categorizedSounds.get(category).add(sound);
                        Log.d(TAG, "Added sound: " + displayName + " to category: " + category);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not process resource: " + field.getName(), e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize sounds from resources", e);
            // Add some default sounds as fallback
            addDefaultSounds();
        }
        
        Log.d(TAG, "Sound initialization complete. Categories: " + categorizedSounds.keySet());
    }
    
    /**
     * Categorize sound based on filename prefix
     */
    private String categorizeSound(String resourceName) {
        if (resourceName.startsWith("ambiencesound_")) {
            return "Ambience";
        } else if (resourceName.startsWith("classicalarm_")) {
            return "Classic Alarm";
        } else if (resourceName.startsWith("naturalsound_")) {
            return "Natural Sound";
        }
        return null; // Unknown category
    }
    
    /**
     * Generate a user-friendly display name from resource name
     */
    private String generateDisplayName(String resourceName) {
        // Remove prefix and convert underscores to spaces, then title case
        String name = resourceName;
        
        if (name.startsWith("ambiencesound_")) {
            name = name.substring("ambiencesound_".length());
        } else if (name.startsWith("classicalarm_")) {
            name = name.substring("classicalarm_".length());
        } else if (name.startsWith("naturalsound_")) {
            name = name.substring("naturalsound_".length());
        }
        
        // Replace underscores with spaces and title case
        name = name.replace("_", " ");
        return toTitleCase(name);
    }
    
    /**
     * Convert string to title case
     */
    private String toTitleCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
    
    /**
     * Add default sounds as fallback when resource scanning fails
     */
    private void addDefaultSounds() {
        Log.d(TAG, "Adding default sounds as fallback");
        
        // Add some default sounds for each category
        categorizedSounds.get("Ambience").add(new Sound("Airport", "ambiencesound_airport", 0, "Ambience"));
        categorizedSounds.get("Ambience").add(new Sound("Cafe", "ambiencesound_cafe", 0, "Ambience"));
        categorizedSounds.get("Ambience").add(new Sound("Forest", "ambiencesound_forest", 0, "Ambience"));
        categorizedSounds.get("Ambience").add(new Sound("Ocean", "ambiencesound_ocean", 0, "Ambience"));
        
        categorizedSounds.get("Classic Alarm").add(new Sound("Classic Bell", "ClassicAlarm_bell", 0, "Classic Alarm"));
        categorizedSounds.get("Classic Alarm").add(new Sound("Digital Beep", "ClassicAlarm_digital", 0, "Classic Alarm"));
        categorizedSounds.get("Classic Alarm").add(new Sound("Gentle Chime", "ClassicAlarm_chime", 0, "Classic Alarm"));
        
        categorizedSounds.get("Natural Sound").add(new Sound("Birds Chirping", "NaturalSound_birds", 0, "Natural Sound"));
        categorizedSounds.get("Natural Sound").add(new Sound("Rain", "NaturalSound_rain", 0, "Natural Sound"));
        categorizedSounds.get("Natural Sound").add(new Sound("Wind", "NaturalSound_wind", 0, "Natural Sound"));
    }
    
    /**
     * Get all available categories
     */
    public List<String> getCategories() {
        return new ArrayList<>(categorizedSounds.keySet());
    }
    
    /**
     * Get sounds for a specific category
     */
    public List<Sound> getSoundsForCategory(String category) {
        List<Sound> sounds = categorizedSounds.get(category);
        return sounds != null ? new ArrayList<>(sounds) : new ArrayList<>();
    }
    
    /**
     * Get all sounds as a flat list
     */
    public List<Sound> getAllSounds() {
        List<Sound> allSounds = new ArrayList<>();
        for (List<Sound> categorySounds : categorizedSounds.values()) {
            allSounds.addAll(categorySounds);
        }
        return allSounds;
    }
    
    /**
     * Find a sound by its resource name
     */
    public Sound findSoundByResourceName(String resourceName) {
        for (List<Sound> categorySounds : categorizedSounds.values()) {
            for (Sound sound : categorySounds) {
                if (sound.getResourceName().equals(resourceName)) {
                    return sound;
                }
            }
        }
        return null;
    }
}





