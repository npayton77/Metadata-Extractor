package com.example.apple_hls_metadata_extractor

import android.util.Log
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.extractor.ts.TsExtractor

/**
 * Custom extractor factory for Apple HLS streams that enhances standard extractors
 * to better handle EMSG boxes containing ID3 metadata.
 * 
 * This factory creates extractors optimized for Apple's HLS format while maintaining
 * compatibility with standard fMP4 and TS segments.
 * 
 * @author Apple HLS Metadata Extractor
 * @version 1.0
 */
class AppleEmsgExtractorFactory : ExtractorsFactory {
    
    companion object {
        private const val TAG = "AppleEmsgExtractor"
    }
    
    /**
     * Creates extractors suitable for Apple HLS streams.
     * 
     * @return Array of extractors optimized for Apple HLS format
     */
    override fun createExtractors(): Array<Extractor> {
        // Only log for debugging purposes, not production spam
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Creating extractors for Apple HLS stream")
        }
        
        return arrayOf(
            // Fragmented MP4 extractor for .m4s segments (primary for Apple HLS)
            createFragmentedMp4Extractor(),
            
            // TS extractor for .ts segments (fallback)
            createTsExtractor()
        )
    }
    
    /**
     * Creates a FragmentedMp4Extractor optimized for Apple HLS streams.
     */
    private fun createFragmentedMp4Extractor(): FragmentedMp4Extractor {
        Log.d(TAG, "Creating extractor for media segment")
        
        return FragmentedMp4Extractor.Builder()
            .setFlags(
                // Enable EMSG track output for metadata extraction
                FragmentedMp4Extractor.FLAG_ENABLE_EMSG_TRACK or
                // Workaround for sidx atom issues in some Apple streams
                FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_TFDT_BOX
            )
            .build()
    }
    
    /**
     * Creates a TsExtractor for TS segment fallback.
     */
    private fun createTsExtractor(): TsExtractor {
        return TsExtractor.Builder()
            .setFlags(
                // Enable ID3 metadata extraction from TS streams
                TsExtractor.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS
            )
            .build()
    }
} 