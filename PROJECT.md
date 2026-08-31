# 天禽 · 飞盘奇门排盘 App（Android）项目档案

> 本文件是维护者的**总纲**：新会话接手本项目前必读。包含项目结构、算法口径、UI 体系、发布流程、已知坑。
> 配套项目（Cardputer 版）见 `~/Arduino/tianqin_cardputer/PROJECT.md`。

## 1. 项目定位

- **应用名**：天禽（包名 `com.potuo.feipanqimen2`）
- **一句话**：飞盘奇门遁甲排盘 App（鸣法体系 · 值使飞宫法 · 星门顺飞），含黄历、案例库、内置教材、格局检测、分享图
- **当前版本**：v3.1.1（versionCode 19），历史 v2.0 → v3.1.1
- **项目位置**：`~/.agent_work/feipan-qimen-app2/`
- **算法口径**：《奇门基础资料 2023版教》（WPS doc，55 页）——权威教材，排盘规则唯一依据

## 2. 仓库与更新源

| 项 | 值 |
|---|---|
| GitHub | `github.com/potuo/feipan-qimen`（主仓，remote `origin`） |
| Gitee | `gitee.com/potuo/feipan-qimen`（国内镜像 + 更新源，remote `gitee`） |
| 更新架构 | **Gitee 单源**：version.json / changelog.json / notice.json 走 Gitee raw（`gitee.com/potuo/feipan-qimen/raw/master/`），APK 走 Gitee Release 直链；兜底 GitHub raw → jsDelivr |
| 分支 | master（代码）+ Gitee dist 分支（历史放 APK，已弃用） |
| 发布脚本 | `~/.agent_work/publish_gitee.sh`（建 Gitee Release → 传 APK → 更新 version.json → 双仓 push） |

## 3. 构建环境（⚠️ 必读坑）

```bash
cd ~/.agent_work/feipan-qimen-app2
# ⚠️ GRADLE_HOME 会劫持 gradlew（Arch 系统 gradle 缺模块）——必须清掉：
env -u GRADLE_HOME ./gradlew assembleDebug            # 构建
env -u GRADLE_HOME ./gradlew testDebugUnitTest --rerun-tasks   # 单测（34 个）
```
- SDK `~/Android/Sdk`（android-34），JDK 26，Compose BOM 2024.12.01
- 产物：`app/build/outputs/apk/debug/app-debug.apk`（约 18MB，debug 包）
- 单测位置：`app/src/test/java/com/potuo/feipanqimen2/qimen/`（34 个：排盘自洽性扫描 + 参考盘逐宫断言 + enhance 补全）
- 验证 APK 版本：`~/Android/Sdk/build-tools/34.0.0/aapt dump badging app-debug.apk | grep versionName`

## 4. 代码结构

```
app/src/main/java/com/potuo/feipanqimen2/
├── MainActivity.kt        # 入口：主题注入(FeipanQimenTheme) + 抽屉导航 + 启动动画 + 自动更新检查
├── UpdateChecker.kt       # 更新检查：Gitee→GitHub API→jsDelivr→raw 多源链，防旧缓存跳过逻辑
├── QimenShareImage.kt     # 分享图：canvas 绘制 1080px 盘面（6 行/格），可带案例信息(标签/备注/反馈)
├── PatternBook.kt         # 格局↔教材联动：格局名→vol3.txt 关键词窗口提取原文
├── qimen/                 # ★排盘算法（勿乱改，见 §5）
│   ├── QimenCalculator.kt     # 排盘七步主流程
│   ├── QimenConstants.kt      # 所有常量表（局数/神序/星门序/击刑/入墓/驿马/五行）
│   ├── QimenPalaceEnhancer.kt # 纯函数：六亲/旺衰/角标/地盘神 + enhance() 老案例补全
│   ├── QimenPatternDetector.kt# 格局检测：击刑/入墓/门迫/受制/交和/伏吟反吟/守门八格/空亡
│   ├── QimenModels.kt         # QimenResult / PalaceInfo 数据结构
│   ├── QimenCalculator 相关    # 黄历服务 TrueSolarTime（真太阳时）
├── ui/
│   ├── InputScreen.kt     # 排盘输入：日期选择 + Material3 TimePicker 时间选择（默认当前时间→映射时辰）
│   ├── ResultScreen.kt    # 盘面结果：QimenBoard + 格局卡（点按弹教材原文）+ 标签/类别 + 保存/分享
│   ├── CaseListScreen.kt  # 案例库：搜索 + 类别筛选 + 反馈筛选(全部/已反馈/未反馈 带数量) + 空状态区分
│   ├── CaseDetailScreen.kt# 案例详情：盘面 + 标签/备注/反馈输入 + 分享图片
│   ├── LearnScreen.kt     # 学习栏目：md 渲染器(标题/引用/列表/图片/代码块) + 目录/搜索/位置记忆
│   ├── AboutScreen.kt     # 关于：GitHub 头像 + 双平台链接(logo) + 检查更新 + 更新日志 + 公告 + 日志
│   ├── SettingsScreen.kt  # 设置：主题选择(古典金/紫微/青玉) + 明暗 + 经度 + 数据导入导出
│   ├── theme/             # Theme.kt(colorScheme) + QimenPalette.kt(盘面色板三主题×双态) + Dimens/Shape/Type
│   └── components/        # QimenComponents.kt(QimenBoard/PalaceCell 六行布局/MiniBoard) + CommonComponents
├── data/                  # Room：CaseEntity(含 feedback) / CaseDao(组合筛选) / AppDatabase(v3 迁移) / CaseRepository
└── log/LogManager.kt      # 日志系统（按天滚动 + 崩溃捕获）

assets/qimen_book/         # ★内置教材
├── vol1.txt 数术基础 / vol2.txt 奇门排盘 / vol3.txt 占断法则
│   （md 风格：`#`卷 `##`章 `###`节 `>`口诀引用 `**`重点 `-`列表 `![图注](img/xx.jpg)` ```等宽九宫）
└── img/                   # 16 张教材原图（从 doc 提取压缩，语义命名）
```

## 5. 排盘算法口径（★ 修改前必读）

**教材**：《奇门基础资料 2023版教》唯一口径。排盘七步全在 `qimen/QimenCalculator.kt`：

1. **四柱**：年柱立春分界 / 月柱节令（lunar 库提供）/ 日柱 / 时柱五鼠遁（子时 23:00 分界）
2. **定局**：节气+符头定元——符头（甲己日）地支：子午卯酉上元 / 寅申巳亥中元 / 辰戌丑未下元；局数表 `JIE_QI_JU_SHU`（阳遁 12 节气 × 上中下元）
3. **地盘奇仪**：戊己庚辛壬癸丁丙乙，戊起局数宫，阳顺阴逆（洛书序 1→9）
4. **天盘奇仪**：值符干（旬首遁仪）加地盘时干宫，整体平移
5. **九神**：值符螣蛇太阴六合勾陈太常朱雀九地九天（阳顺阴逆），值符从时干宫起
6. **九星八门**（飞盘核心）：星序**含天禽**、门序**含中门**，值符星/值使门加时干宫后**皆顺飞 9 宫**；值使门落宫 = 旬首遁仪地盘宫按六十甲子序数阳顺阴逆数到当前时柱（飞宫法）
   - ⚠️ **天禽/中门参与飞布不固定居中**（2026-08-31 修正，勿改回"居中"）
7. **暗干支**：时干支加值使落宫，本旬十干从时干支起正序循环（**遇甲不排，回绕过旬首**），阳顺阴逆，中宫参与

**增强字段**（`QimenPalaceEnhancer`，纯函数）：
- 六亲：以时干为「我」生我父母/克我官鬼/我克妻财/我生子孙/比和兄弟
- 旺衰：天盘干 vs 月令五行（**24 节气全表**，勿只查 12 节——处暑是中气曾导致全盘算错）
- 角标：马（时支驿马）/ 迫（门克宫，有刑墓不标）/ 刑（击刑本宫）/ 墓（入墓本宫）
- 地盘神：值符神从**旬首遁仪地盘宫**起布（与天盘神同序，起点不同）
- `enhance()`：老案例 panJson 缺注解字段时自动补算（v3.1.0 加入，勿删）

**格局检测**（`QimenPatternDetector`）：六仪击刑（仅值符论）/ 入墓 / 门迫受制交和（中门不论）/ 伏吟反吟 / 守门八格（值使宫天盘干查 `SHOU_MEN` 表）/ 空亡（恒最后）

## 6. UI 体系

- **盘面 PalaceCell 六行布局**（App 内 = 分享图，v3.1.1 对齐三列定位）：
  1. 宫名(左,次级灰) 天盘神(中) 角标(右上,棕)
  2. 暗干(棕,左列) 星(黑/红,中列) 天盘干(黑/红,右列)
  3. 星六亲(左) 天盘干六亲(右)
  4. 暗支(棕,左) 门(黑/红,中) 地盘干(黑,右)
  5. 门六亲(左) 地盘干六亲(右)
  6. 地盘神(左,值符红) 旺衰(右,棕)
  - 红色=值符星/值使门/日时干/地盘值符；值符值使宫朱砂描边；**中宫与其他宫同底**
- **主题**：古典金(宣纸/墨底)、紫微(月白/夜空)、青玉(绢素/松烟)——`QimenPalette` 六套盘面色板 + `Theme.kt` 六套 colorScheme，设置页三选一 + 明暗
- **学习栏目**：自研轻量 md 渲染器（非第三方库），LazyColumn + 图片全局缓存 + SharedPreferences 位置记忆
- **惯例**：子页面不带头部（MainActivity 统一标题栏）；元素居中；QimenDimens 间距 token

## 7. 数据层

- Room `cases` 表 v3：`panDate/panHour/siZhu/jieQi/yuan/dunType/juNumber/panJson/category/tags/note/huangLi/feedback`
- **feedback**（v2.6.13 加）：非空即"已反馈"——列表徽标 + 筛选（searchCasesFiltered 组合查询：搜索×类别×反馈）
- 迁移链：v1→v2 加 category，v2→v3 加 feedback（AppDatabase.MIGRATION_*）
- panJson 序列化完整 QimenResult；老案例反序列化自动 enhance 补全

## 8. 发布流程（发版）

```bash
cd ~/.agent_work/feipan-qimen-app2
# 1. bump：app/build.gradle.kts versionCode+1 / versionName → 新版本
# 2. changelog.json 顶部加条目（version/date/items）
# 3. version.json 更新 version + notes（apk_url 发布脚本会改写）
# 4. 构建 + 单测：
env -u GRADLE_HOME ./gradlew assembleDebug testDebugUnitTest --rerun-tasks
# 5. 发布（Gitee Release + 双仓 push + version.json apk_url 指向 Gitee 直链）：
bash ~/.agent_work/publish_gitee.sh
# 6. GitHub Release（打 tag + 传 APK）：
gh release create vX.Y.Z app/build/outputs/apk/debug/app-debug.apk --repo potuo/feipan-qimen --title "天禽 vX.Y.Z" --notes "..."
# 7. 验证更新源：curl -sL -A "feipan-qimen" https://gitee.com/potuo/feipan-qimen/raw/master/version.json
```
⚠️ Gitee raw 有 1~2 分钟缓存延迟；发布后别秒测。App 端已做防旧缓存（checkLatest 跳过 ≤ 本地版本的源）。

## 9. 已知坑清单

1. **GRADLE_HOME 劫持**：必须 `env -u GRADLE_HOME ./gradlew`（Arch 系统 gradle 缺 gradle-public-api-legacy）
2. **更新检测多源**：Gitee raw(主) → GitHub API → jsDelivr(12h 缓存!) → GitHub raw；checkLatest(localVersion) 防旧缓存
3. **Cursor CLI**：`/opt/cursor-agent/cursor-agent -p --yolo --model auto`（免费计划仅 auto 模型）；须 git 仓库内；改完**必须独立验证**（不信自报）；强领域（算法）自己改，通用 UI 可派 Cursor；写 PRD 钉死规格（红线：qimen/ 算法文件只准删 import）
4. **分享图 vs App 盘面**：必须视觉一致（六行布局 + 三列定位 + 字号层级），改一处同步另一处
5. **老案例**：v2.6.8 前存的 panJson 缺注解字段 → 显示 4 行 → deserializePan 已自动 enhance
6. **教材 doc**：原始教材 `~/Documents/xwechat_files/wxid_y0knek6spfd422_c454/msg/file/2026-08/奇门基础资料 2023版教.doc`；改教材需重新从 doc 提取（libreoffice 转 docx → unzip media）
7. **用户偏好**：装机后用户自己测试（勿占 adb）；GitHub 仓库隐私（禁传含本机路径文件）；更新日志/公告走联网 json

## 10. 历史关键决策（防倒退）

| 版本 | 决策 |
|---|---|
| v2.6.9 | 星门飞布修正：天禽/中门**参与飞布不居中**（此前误读教材示例盘） |
| v2.6.9 | 旺衰月令补全 24 节气（含中气，处暑 bug） |
| v2.6.10 | Gitee 单源切换（COS 全删）；系统公告功能 |
| v2.6.11 | 青玉主题；教材重排 md + 16 原图 |
| v2.6.12 | 学习目录/搜索/位置记忆；格局点按弹教材原文；案例反馈字段 |
| v2.6.13 | 反馈筛选；关于页头像双平台；Cursor 大精简(-200 行) |
| v3.0.0 | 时间选择器(默认当前时间)；案例分享改图片 |
| v3.1.0 | 老案例 enhance 补全；分享图带标签/备注/反馈；筛选优化 |
| v3.1.1 | App 盘面三列定位对齐分享图 |
