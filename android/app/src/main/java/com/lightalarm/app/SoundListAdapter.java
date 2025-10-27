package com.lightalarm.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;

/**
 * Custom adapter for displaying sounds in a list with preview functionality
 */
public class SoundListAdapter extends BaseAdapter {
    private static final String TAG = "SoundListAdapter";
    
    private Context context;
    private List<Sound> sounds;
    private SoundPreviewManager previewManager;
    private LayoutInflater inflater;
    private int currentlyPlayingPosition = -1;
    
    public SoundListAdapter(Context context, List<Sound> sounds, SoundPreviewManager previewManager) {
        this.context = context;
        this.sounds = sounds;
        this.previewManager = previewManager;
        this.inflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        return sounds.size();
    }
    
    @Override
    public Object getItem(int position) {
        return sounds.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sound_list_item, parent, false);
            holder = new ViewHolder();
            holder.soundNameText = convertView.findViewById(R.id.soundNameText);
            holder.previewButton = convertView.findViewById(R.id.previewButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Sound sound = sounds.get(position);
        holder.soundNameText.setText(sound.getDisplayName());
        
        // Update preview button state
        if (currentlyPlayingPosition == position && previewManager.isPlaying()) {
            holder.previewButton.setImageResource(android.R.drawable.ic_media_pause); // Pause icon
            holder.previewButton.setEnabled(true);
        } else {
            holder.previewButton.setImageResource(android.R.drawable.ic_media_play); // Play icon
            holder.previewButton.setEnabled(true);
        }
        
        // Set up preview button click listener
        holder.previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePreviewClick(position, sound, holder);
            }
        });
        
        return convertView;
    }
    
    private void handlePreviewClick(int position, Sound sound, ViewHolder holder) {
        Log.d(TAG, "Preview button clicked for: " + sound.getDisplayName());
        
        if (currentlyPlayingPosition == position && previewManager.isPlaying()) {
            // Stop current preview
            previewManager.stopPreview();
            currentlyPlayingPosition = -1;
            holder.previewButton.setImageResource(android.R.drawable.ic_media_play);
            Log.d(TAG, "Stopped preview");
        } else {
            // Stop any currently playing preview
            if (currentlyPlayingPosition != -1) {
                previewManager.stopPreview();
                notifyDataSetChanged(); // Update all views
            }
            
            // Start new preview
            currentlyPlayingPosition = position;
            holder.previewButton.setImageResource(android.R.drawable.ic_media_pause);
            
            previewManager.startPreview(sound, new SoundPreviewManager.PreviewCallback() {
                @Override
                public void onPreviewStarted() {
                    Log.d(TAG, "Preview started for: " + sound.getDisplayName());
                }
                
                @Override
                public void onPreviewStopped() {
                    Log.d(TAG, "Preview stopped for: " + sound.getDisplayName());
                    currentlyPlayingPosition = -1;
                    notifyDataSetChanged(); // Update all views
                }
                
                @Override
                public void onPreviewError(String error) {
                    Log.e(TAG, "Preview error: " + error);
                    currentlyPlayingPosition = -1;
                    notifyDataSetChanged(); // Update all views
                }
            });
        }
    }
    
    /**
     * ViewHolder pattern for efficient list view recycling
     */
    private static class ViewHolder {
        TextView soundNameText;
        ImageButton previewButton;
    }
}
