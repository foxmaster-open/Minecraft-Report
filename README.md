# Report Plugin - 举报插件

## English

### 📋 Description
A simple and efficient report system for Minecraft servers. Players can report anyone (including OPs) using a GUI interface. OPs can view all reports in a GUI and manage them.

### ✨ Features
- **GUI Interface** - Intuitive graphical reporting system
- **No Permission Required** - All players can use the report function
- **Report Anyone** - Players can report any player, including administrators
- **Report List** - OPs can view all reports in a GUI
- **Cooldown System** - Prevents spam reporting
- **OP Notifications** - Online OPs receive instant notifications
- **Data Persistence** - Reports are automatically saved

### 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/report` | Open report GUI | report.use |
| `/reportlist` | View report list (GUI) | report.list |

### 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `report.use` | Use report function | `true` |
| `report.list` | View report list | `op` |
| `report.*` | All permissions | `op` |

### 🎮 How to Use

#### For Players:
1. Type `/report` to open the player selection GUI
2. Click on the player you want to report
3. Select a reason from the list
4. Report is submitted and OPs are notified

#### For OPs:
1. Type `/reportlist` to open the report list GUI
2. Red = Unhandled, Green = Handled
3. Click on any report to delete it

### ⚙️ Configuration

File location: `plugins/Report/config.yml`

```yaml
# Report reasons list
report-reasons:
  - "&cCheating/Hacking"
  - "&6Harassment"
  - "&eSpam/Advertising"
  - "&2Griefing"
  - "&bTheft"
  - "&dVerbal Abuse"
  - "&5Bug Exploiting"
  - "&3Other"

# Cooldown settings (seconds)
cooldown:
  enabled: true
  time: 60

# Notify OPs when a report is submitted
notify-ops: true

# Save reports to file
save-to-file: true
```

### 📦 Installation

1. Download `report-plugin-1.0.0.jar`
2. Place it in your server's `plugins` folder
3. Restart the server or use `plugman load Report`
4. Configure in `plugins/Report/config.yml`

### 💾 Data Storage

- Reports are saved in `plugins/Report/reports.yml`
- Contains reporter, target, reason, timestamp, and status
- Automatically loaded on server start

---

## 中文

### 📋 插件介绍
一个简单易用的 Minecraft 服务器举报系统。玩家可以通过 GUI 界面举报任何人（包括OP）。OP可以通过 GUI 查看所有举报记录并进行管理。

### ✨ 功能特点
- **GUI界面** - 直观的图形化举报界面
- **无需权限** - 所有玩家都可以使用举报功能
- **举报任何人** - 玩家可以举报任何玩家，包括管理员
- **举报列表** - OP 可通过 GUI 查看所有举报
- **冷却系统** - 防止恶意刷屏举报
- **OP通知** - 在线OP会收到即时通知
- **数据持久化** - 举报记录自动保存

### 📋 命令列表

| 命令 | 描述 | 权限 |
|------|------|------|
| `/report` | 打开举报GUI | report.use |
| `/reportlist` | 查看举报列表（GUI） | report.list |

### 🔐 权限节点

| 权限 | 描述 | 默认值 |
|------|------|--------|
| `report.use` | 使用举报功能 | `true` |
| `report.list` | 查看举报列表 | `op` |
| `report.*` | 所有权限 | `op` |

### 🎮 使用方法

#### 玩家：
1. 输入 `/report` 打开玩家选择 GUI
2. 点击想要举报的玩家头像
3. 选择举报原因
4. 举报提交成功，OP会收到通知

#### OP管理员：
1. 输入 `/reportlist` 打开举报列表 GUI
2. 红色 = 未处理，绿色 = 已处理
3. 点击任意举报可删除

### ⚙️ 配置文件

配置文件位置：`plugins/Report/config.yml`

```yaml
# 举报原因列表
report-reasons:
  - "&c作弊/外挂"
  - "&6恶意骚扰"
  - "&e刷屏/广告"
  - "&2恶意破坏"
  - "&b偷窃/抢夺"
  - "&d语言辱骂"
  - "&5利用BUG"
  - "&3其他违规"

# 冷却时间设置（秒）
cooldown:
  enabled: true
  time: 60

# 举报时是否通知OP
notify-ops: true

# 是否保存举报记录到文件
save-to-file: true
```

### 📦 安装方法

1. 下载 `report-plugin-1.0.0.jar`
2. 放入服务器的 `plugins` 文件夹
3. 重启服务器或使用 `plugman load Report`
4. 在 `plugins/Report/config.yml` 中配置

### 💾 数据存储

- 举报记录保存在 `plugins/Report/reports.yml`
- 包含举报人、被举报人、原因、时间、状态
- 服务器启动时自动加载
