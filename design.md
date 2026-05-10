# Android App Design Specification: Local Music Player (Pure Local)

## 1. Project Overview
**App Name:** 本地音乐 (Local Music)
**Target Platform:** Android (API 26+)
**Language:** Simplified Chinese (zh-CN)
**Core Function:** A purely offline music player. No internet permissions. Scans local storage for audio files, manages playlists, displays lyrics, and provides a high-quality playback experience.
**Tech Stack Recommendation:**
- Language: Kotlin
- UI Framework: Jetpack Compose (preferred for dynamic UI) or XML + ViewBinding.
- Architecture: MVVM (Model-View-ViewModel).
- Database: Room (for storing playlists, favorites, and scan history).
- Audio Engine: ExoPlayer (Media3) or MediaPlayer.
- Background: WorkManager (for scanning), Foreground Service (for playback).

## 2. Global UI/UX Design System
**Theme:** Modern, Immersive, Glassmorphism.
**Color Palette:**
- **Primary Background:** Dynamic gradient based on album art (or a default deep purple/red gradient as seen in reference images).
- **Text:** White (primary), Light Gray (secondary/lyrics inactive).
- **Accents:** Green (for VIP/Status indicators in "Me" tab), Red/Pink (for hearts/favorites).
  **Typography:**
- Titles: Bold, Sans-serif.
- Lyrics: Medium weight, variable size (Current line large/bold, others smaller/dimmer).

## 3. Screen Specifications

### Screen A: Home Screen (首页)
*Corresponds to Reference Image 4 (modified for local usage)*

**Layout Structure:**
1.  **Top Bar:**
    -   **Search Bar:** Rounded rectangle, light gray background. Placeholder text: "搜索本地音乐...". Icon: Magnifying glass.
    -   **Right Icons:** Scan Icon (Refresh), Settings Icon.
2.  **Tab Layout:**
    -   Tabs: "推荐" (Recommended/Local Picks), "歌单" (Playlists), "歌手" (Artists), "专辑" (Albums).
    -   Active Tab: Green underline or bold text.
3.  **Content Area (Scrollable):**
    -   **Section 1: "最近播放" (Recently Played):**
        -   Horizontal RecyclerView.
        -   Items: Square Album Art + Song Title below.
    -   **Section 2: "本地歌单" (Local Playlists):**
        -   Grid or List view.
        -   Items: "所有歌曲" (All Songs), "我的收藏" (Favorites), "新建歌单" (Create New).
    -   **Section 3: "歌曲列表" (Song List):**
        -   Vertical List.
        -   Item Layout:
            -   Left: Small Square Album Art.
            -   Middle: Song Title (Top), Artist Name (Bottom, smaller gray text).
            -   Right: "More" icon (three dots) or Duration.
4.  **Bottom Navigation Bar:**
    -   Two Tabs: "首页" (Home - Active), "我的" (Me).
    -   Style: White background (or translucent), icons with text labels.
5.  **Mini Player (Floating at bottom above Nav):**
    -   Height: ~60dp.
    -   Left: Small Album Art.
    -   Middle: Song Title, Artist.
    -   Right: Play/Pause Icon, Playlist Icon.

### Screen B: Player Screen (播放详情页)
*Corresponds to Reference Images 1 & 2. This is a full-screen activity.*

**Background:**
-   Full screen image.
-   Logic: Take the current song's album art, blur it heavily (Gaussian blur), and use it as the background. Add a dark overlay gradient (Top: dark, Bottom: dark) to ensure text readability.

**Top Bar (Transparent overlay):**
-   Left: Down Arrow (Minimize/Back).
-   Center: Song Title (Small), Artist (Smaller).
-   Right: Heart Icon (Favorite), Share/More Icon.

**Main Content Area (Switchable Modes):**
*User can swipe or click a button to switch between Mode 1 and Mode 2.*

*   **Mode 1: Cover Mode (封面模式 - Ref Image 2)**
    -   **Center:** Large Square Album Art with rounded corners. Shadow effect.
    -   **Below Cover:**
        -   Song Title (Large, White, Bold).
        -   Artist Name (Gray).
    -   **Lyrics Preview:** Two lines of lyrics centered below artist name (faded white).
    -   **Progress Bar:**
        -   Thin line.
        -   Left text: Current Time (00:00).
        -   Right text: Total Duration (03:15).
    -   **Controls (Bottom Center):**
        -   Row of 3 buttons: Previous (Left), Play/Pause (Center, Large Circle), Next (Right).

*   **Mode 2: Lyrics Mode (歌词模式 - Ref Image 1)**
    -   **Layout:** Full screen scrollable text.
    -   **Top:** Song Title and Artist (Small, centered).
    -   **Center (Lyrics):**
        -   Vertical scrolling list.
        -   **Current Line:** Large font, White, Bold, Centered vertically.
        -   **Surrounding Lines:** Smaller font, Light Gray, semi-transparent.
        -   **Animation:** Smooth scroll to keep current line in center.
    -   **Bottom Controls:**
        -   Left: "弹" (Bullet screen toggle - disabled visually or hidden if not implemented, keep for UI consistency), "词" (Lyrics toggle - Active state).
        -   Right: Large Circular Play/Pause Button.
        -   *Note:* In this mode, standard Prev/Next buttons are hidden or moved to a swipe gesture.

### Screen C: Me / Library Screen (我的)
*Corresponds to Reference Image 3*

**Layout Structure:**
1.  **Header Card:**
    -   Background: Light Green / Mint Gradient.
    -   Left: User Avatar (Circle).
    -   Middle: User Name ("本地用户"), VIP Badge (Green pill shape).
    -   Right: "88" (Points placeholder), "领现金" (Button - purely visual or disabled).
    -   **Action Row:** "会员中心" (Member Center - Visual only), "装扮" (Theme), "日签" (Daily Quote), "关注" (Follow - Visual only).
2.  **Stats Grid (4 Columns):**
    -   Item: Icon (Heart, Download, Mic, Bag) + Number + Label.
    -   Labels: "收藏" (Favorites), "本地" (Local Files), "有声" (Audiobooks - 0), "已购" (Purchased - 0).
3.  **Section: "最近播放" (Recently Played):**
    -   Horizontal Scroll.
    -   Cards: Album Art + Title.
4.  **Section: "自建歌单" (Created Playlists):**
    -   List View.
    -   Item: Playlist Cover + Playlist Name + Song Count.
5.  **Bottom Navigation:** Same as Home Screen.
6.  **Mini Player:** Same as Home Screen.

## 4. Functional Logic & Implementation Details

### 4.1. Local File Scanning
-   **Permission:** `READ_EXTERNAL_STORAGE` (or `READ_MEDIA_AUDIO` for Android 13+).
-   **Service:** A background service or coroutine to scan `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI`.
-   **Filter:** Filter by duration (>30s) to avoid ringtones/notifications.
-   **Data Model:** `Song` (id, title, artist, album, duration, path, albumArtUri).

### 4.2. Music Playback Service
-   **Component:** `MediaSessionService` (Foreground Service).
-   **Features:**
    -   Play, Pause, Next, Previous.
    -   SeekTo.
    -   Audio Focus handling (pause when phone rings or other app plays audio).
    -   Lock Screen Controls (Notification with media style).
    -   Broadcast Receiver for Wired Headset / Bluetooth events (Play/Pause).

### 4.3. Lyrics Handling
-   **Logic:**
    -   Look for `.lrc` file with the same name as the audio file in the same directory.
    -   If not found, search common directories.
    -   Parse LRC format `[mm:ss.xx]Lyric text`.
    -   Sync lyrics with `MediaPlayer.getCurrentPosition()`.

### 4.4. Data Persistence
-   **Room Database:**
    -   Table: `Playlist` (id, name, coverUri).
    -   Table: `PlaylistSongMap` (playlistId, songId, order).
    -   Table: `Favorite` (songId).
-   **SharedPreferences:** Store "Last Played Song ID", "Player Mode (Cover/Lyrics)", "Volume".

### 4.5. Specific UI Behaviors (Crucial for Agent)
-   **Gradient Background:** In Player Screen, use `Palette` library (or simple color extraction) to get dominant colors from Album Art and apply as background gradient.
-   **Blur Effect:** Use `RenderScript` or a Compose `Modifier.blur()` on the background image.
-   **Mini Player:** Must be persistent across all screens. When clicked, it expands to the Full Player Screen.

## 5. Assets & Icons (Text Descriptions for Generation)
-   **Play Icon:** Triangle pointing right.
-   **Pause Icon:** Two vertical bars.
-   **Next/Prev:** Triangles with vertical bar (skip) or double triangles.
-   **Heart:** Outline (unselected), Filled Red (selected).
-   **List/Menu:** Three horizontal lines or Three dots.
-   **Search:** Magnifying glass.
-   **Scan/Refresh:** Circular arrow.

## 6. Code Structure Guide
-   `ui/`: Composables for Home, Player, Me.
-   `data/`: Room DAOs, Repositories.
-   `service/`: `MusicService.kt`.
-   `utils/`: `MediaScanner.kt`, `LyricParser.kt`.
-   `viewmodel/`: `PlayerViewModel.kt` (handles playback state), `LibraryViewModel.kt`.