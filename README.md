# music-g51-claude-code

本地音乐播放器 - 纯离线Android音乐播放应用

## 功能特性

- 本地音乐扫描 (自动扫描设备上的音频文件)
- 音乐播放 (播放/暂停/上一首/下一首/进度拖动)
- 歌词显示 (自动匹配同目录LRC歌词文件, 支持封面/歌词双模式切换)
- 收藏管理 (标记/取消收藏歌曲)
- 搜索功能 (按歌曲名/歌手/专辑搜索)
- 通知栏控制 (锁屏/通知栏播放控制)
- 深色沉浸式播放界面 (专辑封面模糊背景)

## 技术栈

- Kotlin + Jetpack Compose
- MVVM 架构
- Room 数据库
- Media3 ExoPlayer (播放引擎)
- Coil (图片加载)
- DataStore (偏好设置)
- GitHub Actions CI/CD

## 版本历史

### v0.1.1 - 修复编译错误与代码优化
- 修复PlayerViewModel编译错误 (Smart cast和类型不匹配)
- 补充POST_NOTIFICATIONS权限声明
- 修复MusicPlaybackService前台通知渠道创建
- 优化播放器进度条拖动体验 (拖动时暂停自动更新)
- 代码重构: 移除冗余import, 消除重复逻辑

### v0.1.0 - 初始版本
- 基础播放功能 (扫描/播放/搜索/歌词/收藏/通知栏控制)
