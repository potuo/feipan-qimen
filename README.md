# 天禽 · 飞盘奇门排盘

> 据《奇门基础资料 2023版教》鸣法体系的**飞盘奇门**排盘 Android App
> 天禽居中 · 符使入中 · 星门顺飞 · 值使飞宫法

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

飞盘奇门排盘工具，遵循《奇门基础资料 2023版教》口径：九星八门按洛书宫序顺飞、天禽居中宫、符使可入中，严格实现「值使门飞宫法」与「符头地支定元」。

## ✨ 功能

- 🌀 **飞盘排盘**：定局 → 布盘 → 星门顺飞 → 暗干支，完整七步
- 🔍 **格局检测**：击刑 / 入墓 / 门迫 / 伏吟反吟 / 守门 / 空亡，自动标注吉凶
- 📅 **黄历详情**：宜忌 / 冲煞 / 彭祖 / 吉神凶煞 / 二十八宿
- 🗂️ **案例库**：事项分类（求财/事业/婚姻…），筛选 + 统计 + 导入导出
- ☀️ **真太阳时**：经度校正，跨时辰边界自动提示
- 📤 **盘面分享**：一键生成分享图
- 📖 **飞盘总纲**：内置教材《奇门基础资料 2023版教》三卷
- 🆕 **检查更新**：无服务器方案，自动检测新版本
- 🎨 **双主题**：古典金 / 紫微 × 浅色 / 暗色

## 📸 截图

| 排盘输入 | 盘面结果 | 侧滑栏 |
|---|---|---|
| ![输入页](screenshots/02_input.png) | ![盘面](screenshots/03_result.png) | ![侧滑栏](screenshots/04_drawer.png) |

| 黄历详情 | 案例库 | 设置 |
|---|---|---|
| ![黄历](screenshots/05_huangli.png) | ![案例](screenshots/06_cases.png) | ![设置](screenshots/07_settings.png) |

## 📦 下载安装

最新 APK 见 [Releases](https://github.com/potuo/feipan-qimen/releases)。

```bash
./gradlew test          # 算法单测（含校验案例）
./gradlew assembleDebug # 构建 APK
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 📜 版本历史

- **v2.6.x**：更名「天禽」+ 八卦天禽图标；格局检测；真太阳时；盘面分享；案例分类；飞盘总纲；更新日志联网化
- **v2.5**：黄历详情页；黑白主题切换；主题收敛古典金
- **v2.4**：设置页折叠分组
- **v2.3**：框架重构为侧滑栏导航；中式古典盘面；启动动画
- **v2.0**：值使飞宫法 + 符头定元（教材口径重构）；黄历/案例库

## 📄 License

[MIT](LICENSE)
