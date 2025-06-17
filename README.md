# Apple HLS ID3 Metadata Extractor for Android

A powerful Android library for extracting ID3 metadata from Apple's proprietary HLS streams with EMSG boxes. This library solves the challenge of parsing metadata from Apple's streaming format that ExoPlayer doesn't natively support.

## 🎯 **Problem Solved**

Apple's HLS streams embed ID3 metadata in EMSG (Event Message) boxes within fMP4 segments. Standard Android media players like ExoPlayer can't parse this metadata format, leaving developers unable to access track information, album artwork, and other metadata from Apple-sourced streams.

## ⚡ **Key Features**

- **Real-time ID3 extraction** from Apple HLS EMSG boxes
- **Corrupted data handling** - Cleans malformed WXXX frames for artwork URLs
- **Multiple encoding support** - UTF-8 and ISO-8859-1 fallback
- **ExoPlayer integration** - Drop-in replacement for standard data sources
- **Thread-safe operation** - Concurrent metadata processing
- **Comprehensive logging** - Debug-friendly with detailed extraction logs

## 🔧 **Supported Metadata**

| ID3 Frame | Description | Example |
|-----------|-------------|---------|
| `TIT2` | Track Title | "Bohemian Rhapsody" |
| `TPE1` | Artist Name | "Queen" |
| `TALB` | Album Title | "A Night at the Opera" |
| `TDRC` | Recording Year | "1975" |
| `WXXX` | Artwork URL | "https://example.com/artwork.jpg" |
| `APIC` | Embedded Images | Binary image data |

## 🚀 **Quick Start**

### 1. Add to your project

```kotlin
// Copy the converter files to your project
// - MetadataCallback.kt
// - DeepHlsDataSource.kt  
// - AppleEmsgExtractorFactory.kt
```

### 2. Implement the callback

```kotlin
private val metadataCallback = object : MetadataCallback {
    override fun onMetadata(
        title: String?,
        artist: String?,
        album: String?,
        year: String?,
        artworkUrl: String?,
        radioStation: String?,
        stationOwner: String?
    ) {
        // Handle extracted metadata
        println("Now Playing: $title by $artist")
        println("Album: $album ($year)")
        println("Artwork: $artworkUrl")
    }
}
```

### 3. Create ExoPlayer with custom data source

```kotlin
// Create the deep HLS data source factory
val dataSourceFactory = DeepHlsDataSource.createFactory(metadataCallback)

// Create HLS media source with custom extractor
val hlsMediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
    .setExtractorFactory(AppleEmsgExtractorFactory())

// Build your media source
val mediaSource = hlsMediaSourceFactory.createMediaSource(
    MediaItem.fromUri("https://your-apple-hls-stream.m3u8")
)

// Use with ExoPlayer
player.setMediaSource(mediaSource)
player.prepare()
```

## 🛠️ **Technical Details**

### EMSG Box Parsing
The library scans fMP4 segments for EMSG boxes containing ID3 data:

```
fMP4 Segment → EMSG Box → ID3v2 Data → Individual Frames → Metadata
```

### Data Cleaning Pipeline
1. **Raw ID3 extraction** from EMSG message data
2. **Frame parsing** with size validation
3. **Encoding detection** (UTF-8 → ISO-8859-1 fallback)
4. **Control character removal** (`\x00-\x1F\x7F-\x9F`)
5. **WXXX frame URL extraction** with corruption handling

### Corrupted WXXX Frame Handling
Apple's streams often contain corrupted WXXX frames like:
```
❌ Raw: "artworkURL_640x��https://example.com/image.jpg"
✅ Cleaned: "https://example.com/image.jpg"
```

The library uses regex pattern matching and data cleaning to extract valid URLs.

## 📋 **Requirements**

- **Android API 24+** (Android 7.0)
- **ExoPlayer 2.18+** or **Media3 1.0+**
- **Kotlin 1.8+**

## 🔍 **Dependencies**

```gradle
implementation "androidx.media3:media3-exoplayer:1.4.1"
implementation "androidx.media3:media3-exoplayer-hls:1.4.1"
implementation "androidx.media3:media3-extractor:1.4.1"
```

## 🐛 **Debugging**

Enable detailed logging to see the extraction process:

```kotlin
// Look for these log tags:
// - "DeepHlsDataSource" - Network and EMSG detection
// - "AudioService" - ID3 frame parsing
// - "AppleEmsgExtractor" - Extractor factory operations
```

Example debug output:
```
🌐 DEEP DATA SOURCE OPEN #1
🌐 URI: https://stream.example.com/segment001.m4s
🚨 OPENING MEDIA SEGMENT - WILL SCAN FOR EMSG!
🎨 Starting ID3 frame parsing, data size: 1024
🏷️ Found frame: TIT2, size: 15, cleaned data: 'Bohemian Rhapsody'
🏷️ Found frame: TPE1, size: 5, cleaned data: 'Queen'
🖼️ WXXX ARTWORK URL FOUND: 'https://example.com/artwork.jpg'
```

## 🎵 **Use Cases**

- **Internet Radio Apps** - Display track info from Apple-sourced streams
- **Music Streaming** - Extract metadata from HLS audio streams  
- **Podcast Players** - Get episode information from Apple streams
- **Audio Analytics** - Track listening data with proper metadata
- **Broadcasting Software** - Professional radio automation systems

## 🤝 **Contributing**

This library was developed to solve real-world streaming metadata challenges. Contributions welcome!

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Submit a pull request

## 📄 **License**

MIT License - Feel free to use in commercial and open-source projects.

## 🙏 **Acknowledgments**

- Built for **Savvy Beast Radio** - "The Internet's Home For Hard/Heavy Rock N' Roll"
- Solves Apple HLS metadata extraction that ExoPlayer doesn't support natively
- Handles real-world corrupted data scenarios from production streams

---

**Made with ❤️ for the Android audio streaming community** 
