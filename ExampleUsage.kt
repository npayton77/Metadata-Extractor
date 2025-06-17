package com.example.apple_hls_metadata_extractor

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import android.content.Context
import android.util.Log

/**
 * Example usage of the Apple HLS ID3 Metadata Extractor.
 * 
 * This example shows how to integrate the extractor with ExoPlayer to receive
 * real-time metadata from Apple HLS streams.
 * 
 * @author Apple HLS Metadata Extractor
 * @version 1.0
 */
class ExampleUsage(private val context: Context) {
    
    companion object {
        private const val TAG = "ExampleUsage"
        
        // Example Apple HLS stream URL (replace with your stream)
        private const val APPLE_HLS_STREAM_URL = "https://your-apple-hls-stream.m3u8"
    }
    
    private lateinit var player: ExoPlayer
    
    /**
     * Sets up ExoPlayer with Apple HLS metadata extraction.
     */
    fun setupPlayer() {
        // Create the metadata callback
        val metadataCallback = object : MetadataCallback {
            override fun onMetadata(
                title: String?,
                artist: String?,
                album: String?,
                year: String?,
                artworkUrl: String?,
                radioStation: String?,
                stationOwner: String?
            ) {
                Log.d(TAG, "🎵 Metadata received:")
                Log.d(TAG, "   Title: $title")
                Log.d(TAG, "   Artist: $artist")
                Log.d(TAG, "   Album: $album")
                Log.d(TAG, "   Year: $year")
                Log.d(TAG, "   Artwork URL: $artworkUrl")
                Log.d(TAG, "   Radio Station: $radioStation")
                Log.d(TAG, "   Station Owner: $stationOwner")
                
                // Update your UI here
                updateUI(title, artist, album, year, artworkUrl)
            }
        }
        
        // Create ExoPlayer
        player = ExoPlayer.Builder(context).build()
        
        // Create the deep HLS data source factory with metadata extraction
        val dataSourceFactory = DeepHlsDataSource.createFactory(metadataCallback)
        
        // Create HLS media source with custom extractor factory
        val hlsMediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
            .setExtractorFactory(AppleEmsgExtractorFactory())
        
        // Build the media source
        val mediaSource = hlsMediaSourceFactory.createMediaSource(
            MediaItem.fromUri(APPLE_HLS_STREAM_URL)
        )
        
        // Set up the player
        player.setMediaSource(mediaSource)
        player.prepare()
        
        Log.d(TAG, "Player setup complete with Apple HLS metadata extraction")
    }
    
    /**
     * Updates the UI with extracted metadata.
     * Replace this with your actual UI update logic.
     */
    private fun updateUI(
        title: String?,
        artist: String?,
        album: String?,
        year: String?,
        artworkUrl: String?
    ) {
        // Example UI updates - replace with your implementation
        
        // Update track title
        title?.let { 
            // titleTextView.text = it
            Log.d(TAG, "UI: Setting title to '$it'")
        }
        
        // Update artist name
        artist?.let { 
            // artistTextView.text = it
            Log.d(TAG, "UI: Setting artist to '$it'")
        }
        
        // Update album info
        album?.let { albumName ->
            val albumText = if (year != null) "$albumName ($year)" else albumName
            // albumTextView.text = albumText
            Log.d(TAG, "UI: Setting album to '$albumText'")
        }
        
        // Load artwork image
        artworkUrl?.let { url ->
            // Use your preferred image loading library (Glide, Picasso, Coil, etc.)
            // Glide.with(context).load(url).into(artworkImageView)
            Log.d(TAG, "UI: Loading artwork from '$url'")
        }
    }
    
    /**
     * Starts playback.
     */
    fun play() {
        player.play()
        Log.d(TAG, "Playback started")
    }
    
    /**
     * Pauses playback.
     */
    fun pause() {
        player.pause()
        Log.d(TAG, "Playback paused")
    }
    
    /**
     * Stops playback and releases resources.
     */
    fun stop() {
        player.stop()
        Log.d(TAG, "Playback stopped")
    }
    
    /**
     * Releases the player resources.
     * Call this when the player is no longer needed.
     */
    fun release() {
        player.release()
        Log.d(TAG, "Player released")
    }
}

/**
 * Example Activity integration
 */
/*
class MainActivity : AppCompatActivity() {
    
    private lateinit var exampleUsage: ExampleUsage
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize the Apple HLS metadata extractor
        exampleUsage = ExampleUsage(this)
        exampleUsage.setupPlayer()
    }
    
    override fun onStart() {
        super.onStart()
        exampleUsage.play()
    }
    
    override fun onStop() {
        super.onStop()
        exampleUsage.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        exampleUsage.release()
    }
}
*/ 