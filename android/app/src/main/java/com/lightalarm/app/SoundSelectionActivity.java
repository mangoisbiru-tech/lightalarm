package com.lightalarm.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

/**
 * Activity for selecting alarm sounds with category filtering and preview functionality
 */
public class SoundSelectionActivity extends Activity {
    private static final String TAG = "SoundSelectionActivity";
    
    private SoundManager soundManager;
    private SoundPreviewManager previewManager;
    private List<Sound> currentSounds;
    private Sound selectedSound;
    private String selectedCategory;
    
    // UI Components
    private Spinner categorySpinner;
    private ListView soundListView;
    private Button selectButton;
    private Button cancelButton;
    private TextView selectedSoundText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_selection);
        
        Log.d(TAG, "SoundSelectionActivity created");
        
        // Initialize managers
        soundManager = SoundManager.getInstance(this);
        previewManager = SoundPreviewManager.getInstance(this);
        
        // Initialize UI
        initializeUI();
        setupCategorySpinner();
        setupSoundList();
        setupButtons();
        
        // Load initial category
        if (!soundManager.getCategories().isEmpty()) {
            selectedCategory = soundManager.getCategories().get(0);
            loadSoundsForCategory(selectedCategory);
        }
    }
    
    private void initializeUI() {
        categorySpinner = findViewById(R.id.categorySpinner);
        soundListView = findViewById(R.id.soundListView);
        selectButton = findViewById(R.id.selectButton);
        cancelButton = findViewById(R.id.cancelButton);
        selectedSoundText = findViewById(R.id.selectedSoundText);
        
        // Initially disable select button
        selectButton.setEnabled(false);
    }
    
    private void setupCategorySpinner() {
        List<String> categories = soundManager.getCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                loadSoundsForCategory(selectedCategory);
                selectedSound = null;
                updateSelectedSoundDisplay();
                selectButton.setEnabled(false);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void setupSoundList() {
        soundListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSound = currentSounds.get(position);
                updateSelectedSoundDisplay();
                selectButton.setEnabled(true);
                Log.d(TAG, "Selected sound: " + selectedSound.getDisplayName());
            }
        });
    }
    
    private void setupButtons() {
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedSound != null) {
                    // Return selected sound to calling activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selected_sound_name", selectedSound.getDisplayName());
                    resultIntent.putExtra("selected_sound_resource", selectedSound.getResourceName());
                    resultIntent.putExtra("selected_sound_id", selectedSound.getResourceId());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
    
    private void loadSoundsForCategory(String category) {
        Log.d(TAG, "Loading sounds for category: " + category);
        currentSounds = soundManager.getSoundsForCategory(category);
        
        // Create adapter for sound list
        SoundListAdapter adapter = new SoundListAdapter(this, currentSounds, previewManager);
        soundListView.setAdapter(adapter);
        
        Log.d(TAG, "Loaded " + currentSounds.size() + " sounds for " + category);
    }
    
    private void updateSelectedSoundDisplay() {
        if (selectedSound != null) {
            selectedSoundText.setText("Selected: " + selectedSound.getDisplayName());
        } else {
            selectedSoundText.setText("No sound selected");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop any playing preview
        previewManager.stopPreview();
        Log.d(TAG, "SoundSelectionActivity destroyed");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop preview when activity is paused
        previewManager.stopPreview();
    }
}








