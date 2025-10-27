package com.lightalarm.app;

/**
 * Data class representing a sound file with its metadata
 */
public class Sound {
    private String displayName;
    private String resourceName;
    private int resourceId;
    private String category;
    
    public Sound(String displayName, String resourceName, int resourceId, String category) {
        this.displayName = displayName;
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.category = category;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public int getResourceId() {
        return resourceId;
    }
    
    public String getCategory() {
        return category;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}











