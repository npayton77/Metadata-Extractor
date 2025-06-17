package com.example.apple_hls_metadata_extractor

/**
 * Callback interface for receiving extracted ID3 metadata from Apple HLS streams.
 * 
 * This interface is called when metadata is successfully extracted from EMSG boxes
 * within fMP4 segments of Apple's HLS streams.
 * 
 * @author Apple HLS Metadata Extractor
 * @version 1.0
 */
interface MetadataCallback {
    
    /**
     * Called when ID3 metadata is extracted from an Apple HLS stream.
     * 
     * @param title Track title from TIT2 frame (nullable)
     * @param artist Artist name from TPE1 frame (nullable)
     * @param album Album title from TALB frame (nullable)
     * @param year Recording year from TDRC frame (nullable)
     * @param artworkUrl Artwork URL from WXXX frame, cleaned of corruption (nullable)
     * @param radioStation Radio station name (nullable)
     * @param stationOwner Station owner/description (nullable)
     */
    fun onMetadata(
        title: String?,
        artist: String?,
        album: String?,
        year: String?,
        artworkUrl: String?,
        radioStation: String?,
        stationOwner: String?
    )
} 