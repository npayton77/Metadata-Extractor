package com.example.apple_hls_metadata_extractor

import android.util.Log
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import java.io.IOException

/**
 * Custom HLS data source that intercepts fMP4 segments to extract ID3 metadata from EMSG boxes.
 * 
 * This data source wraps the default HTTP data source and scans media segments for Apple's
 * proprietary EMSG boxes containing ID3 metadata that ExoPlayer doesn't natively support.
 * 
 * @param metadataCallback Callback to receive extracted metadata
 * @author Apple HLS Metadata Extractor
 * @version 1.0
 */
class DeepHlsDataSource private constructor(
    private val metadataCallback: MetadataCallback
) : DataSource {
    
    companion object {
        private const val TAG = "DeepHlsDataSource"
        private var requestCount = 0
        
        /**
         * Creates a factory for DeepHlsDataSource instances.
         * 
         * @param metadataCallback Callback to receive extracted metadata
         * @return DataSource.Factory for creating DeepHlsDataSource instances
         */
        fun createFactory(metadataCallback: MetadataCallback): DataSource.Factory {
            Log.e(TAG, "🏭 CREATING DEEP HLS DATA SOURCE FACTORY")
            Log.e(TAG, "🏭 Metadata callback: $metadataCallback")
            return DataSource.Factory {
                Log.e(TAG, "🏭 FACTORY CREATING DATA SOURCE INSTANCE")
                DeepHlsDataSource(metadataCallback)
            }
        }
    }
    
    private val httpDataSource = DefaultHttpDataSource.Factory()
        .setUserAgent("AppleHlsMetadataExtractor/1.0")
        .createDataSource()
    
    private var totalBytesRead = 0L
    private var isMediaSegment = false
    private var segmentBuffer = ByteArray(0)
    
    override fun addTransferListener(transferListener: TransferListener) {
        httpDataSource.addTransferListener(transferListener)
    }
    
    override fun open(dataSpec: DataSpec): Long {
        requestCount++
        val uriString = dataSpec.uri.toString()
        isMediaSegment = uriString.contains(".m4s") || uriString.contains(".ts") || 
                        uriString.contains("segment") || uriString.contains("chunk")
        
        Log.e(TAG, "🌐 DEEP DATA SOURCE OPEN #$requestCount")
        Log.e(TAG, "🌐 URI: $uriString")
        Log.e(TAG, "🌐 Is Media Segment: $isMediaSegment")
        
        if (isMediaSegment) {
            Log.e(TAG, "🚨 OPENING MEDIA SEGMENT - WILL SCAN FOR EMSG!")
            totalBytesRead = 0L
            segmentBuffer = ByteArray(0)
        }
        
        return httpDataSource.open(dataSpec)
    }
    
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val bytesRead = httpDataSource.read(buffer, offset, length)
        
        if (bytesRead > 0 && isMediaSegment) {
            // Accumulate segment data for EMSG scanning
            val newData = buffer.sliceArray(offset until offset + bytesRead)
            segmentBuffer += newData
            totalBytesRead += bytesRead
            
            // Scan for EMSG boxes without verbose logging
            try {
                scanForEmsgBoxes(segmentBuffer)
            } catch (e: Exception) {
                // Don't let EMSG parsing errors break the stream
            }
        }
        
        return bytesRead
    }
    
    /**
     * Scans the segment buffer for EMSG boxes containing ID3 metadata.
     */
    private fun scanForEmsgBoxes(data: ByteArray) {
        var offset = 0
        
        while (offset < data.size - 8) {
            // Look for EMSG box signature
            if (data[offset + 4] == 'e'.code.toByte() &&
                data[offset + 5] == 'm'.code.toByte() &&
                data[offset + 6] == 's'.code.toByte() &&
                data[offset + 7] == 'g'.code.toByte()) {
                
                Log.d(TAG, "EMSG box found, parsing metadata")
                try {
                    parseEmsgBox(data, offset)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing EMSG box: ${e.message}")
                }
            }
            offset++
        }
    }
    
    /**
     * Parses an EMSG box to extract ID3 metadata.
     */
    private fun parseEmsgBox(data: ByteArray, emsgOffset: Int) {
        if (emsgOffset + 16 >= data.size) return
        
        // Read EMSG box size (first 4 bytes)
        val boxSize = ((data[emsgOffset].toInt() and 0xFF) shl 24) or
                     ((data[emsgOffset + 1].toInt() and 0xFF) shl 16) or
                     ((data[emsgOffset + 2].toInt() and 0xFF) shl 8) or
                     (data[emsgOffset + 3].toInt() and 0xFF)
        
        if (boxSize <= 0 || emsgOffset + boxSize > data.size) return
        
        // Skip to message data (varies by EMSG version, typically around offset 20-24)
        var messageOffset = emsgOffset + 20
        
        // Look for ID3 signature in message data
        while (messageOffset < emsgOffset + boxSize - 10) {
            if (data[messageOffset] == 'I'.code.toByte() &&
                data[messageOffset + 1] == 'D'.code.toByte() &&
                data[messageOffset + 2] == '3'.code.toByte()) {
                
                // Found ID3 data, extract it
                val id3Data = data.sliceArray(messageOffset until emsgOffset + boxSize)
                parseId3Data(id3Data)
                return
            }
            messageOffset++
        }
    }
    
    /**
     * Parses ID3v2 data to extract individual metadata frames.
     */
    private fun parseId3Data(id3Data: ByteArray) {
        try {
            if (id3Data.size < 10) return
            
            // Skip ID3 header (10 bytes)
            var offset = 10
            
            var extractedTitle: String? = null
            var extractedArtist: String? = null
            var extractedAlbum: String? = null
            var extractedYear: String? = null
            var extractedArtworkUrl: String? = null
            
            while (offset < id3Data.size - 10) {
                val frameId = String(id3Data.sliceArray(offset until offset + 4), Charsets.ISO_8859_1)
                if (!frameId.all { it.isLetter() || it.isDigit() }) break
                
                val frameSize = ((id3Data[offset + 4].toInt() and 0xFF) shl 24) or
                               ((id3Data[offset + 5].toInt() and 0xFF) shl 16) or
                               ((id3Data[offset + 6].toInt() and 0xFF) shl 8) or
                               (id3Data[offset + 7].toInt() and 0xFF)
                
                if (frameSize <= 0 || offset + 10 + frameSize > id3Data.size) break
                
                val frameDataBytes = id3Data.sliceArray(offset + 10 until offset + 10 + frameSize)
                
                // Clean the frame data thoroughly to remove encoding artifacts
                var frameData = String(frameDataBytes, Charsets.UTF_8)
                    .trim('\u0000', ' ', '\t', '\n', '\r')  // Remove null bytes and whitespace
                    .replace(Regex("[\\x00-\\x1F\\x7F-\\x9F]"), "")  // Remove control characters
                    .replace("_", "")  // Remove underscores that might be encoding artifacts
                    .trim()
                
                // If UTF-8 didn't work well, try ISO-8859-1
                if (frameData.isEmpty() || frameData.contains("�")) {
                    frameData = String(frameDataBytes, Charsets.ISO_8859_1)
                        .trim('\u0000', ' ', '\t', '\n', '\r')
                        .replace(Regex("[\\x00-\\x1F\\x7F-\\x9F]"), "")
                        .replace("_", "")
                        .trim()
                }
                
                when (frameId) {
                    "TIT2" -> extractedTitle = frameData.takeIf { it.isNotEmpty() }
                    "TPE1" -> extractedArtist = frameData.takeIf { it.isNotEmpty() }
                    "TALB" -> extractedAlbum = frameData.takeIf { it.isNotEmpty() }
                    "TDRC" -> {
                        // Extract year from TDRC (can be full date like "2023-01-01" or just "2023")
                        val yearMatch = Regex("(\\d{4})").find(frameData)
                        extractedYear = yearMatch?.groupValues?.get(1)
                    }
                    "WXXX" -> {
                        // WXXX frame often contains corrupted data, extract clean URL
                        val rawString = String(frameDataBytes, Charsets.ISO_8859_1)
                        
                        // Find https:// or http:// pattern
                        val httpIndex = rawString.indexOf("http")
                        if (httpIndex >= 0) {
                            val urlPart = rawString.substring(httpIndex)
                            // Clean up the URL by removing null bytes and control characters
                            val cleanUrl = urlPart.replace(Regex("[\\x00-\\x1F\\x7F-\\x9F]"), "").trim()
                            
                            if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
                                extractedArtworkUrl = cleanUrl
                            }
                        }
                    }
                }
                
                offset += 10 + frameSize
            }
            
            // If we extracted any metadata, trigger the callback
            if (extractedTitle != null || extractedArtist != null || extractedAlbum != null) {
                metadataCallback.onMetadata(
                    extractedTitle,
                    extractedArtist,
                    extractedAlbum,
                    extractedYear,
                    extractedArtworkUrl,
                    "Radio Station", // Default radio station
                    "Streaming Service" // Default station owner
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing raw ID3 data: ${e.message}")
        }
    }
    
    override fun getUri() = httpDataSource.uri
    
    override fun getResponseHeaders() = httpDataSource.responseHeaders
    
    override fun close() {
        if (isMediaSegment && totalBytesRead > 0) {
            Log.d(TAG, "Media segment processed: $totalBytesRead bytes")
        }
        httpDataSource.close()
    }
} 