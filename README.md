# Report举报插件

一个简单易用的举报系统，普通玩家可以举报任何人（包括OP），管理员可以查看所有举报记录。

## ✨ 功能特点

- **普通玩家举报** - 任何人都可以使用 `/report` 命令举报违规玩家
- **举报OP** - 普通玩家也可以举报OP，维护游戏公平
- **冷却机制** - 防止恶意刷举报（默认60秒）
- **管理员查看** - `/reportlist` 查看所有举报记录
- **自动清理** - 超过指定天数的举报自动删除（默认7天）
- **管理员通知** - 新举报自动通知所有在线管理员
- **数据存储** - YAML文件存储，轻量可靠

## 📋 命令列表

| 命令 | 描述 | 权限 |
|------|------|------|
| `/report <玩家> <理由>` | 举报违规玩家 | report.use |
| `/reportlist [页码]` | 查看举报列表 | report.admin |

## 🔐 权限节点

| 权限 | 描述 | 默认 |
|------|------|------|
| report.use | 使用举报功能 | true |
| report.admin | 查看举报列表 | op |
| report.* | 所有权限 | op |

## ⚙️ 配置说明

配置文件位于 `plugins/Report/config.yml`：

```yaml
# 举报冷却时间（秒）
cooldown: 60

# 最大举报记录保留天数
max-days: 7

# 所有消息都可以自定义
messages:
  prefix: "&8[&c举报&8] &r"
  report-success: "&a举报已提交！管理员将会处理。"
  # ... 更多消息配置
🎮 使用示例
玩家举报
minecraft
/report Steve 使用飞行外挂
效果：举报提交，在线管理员收到通知

管理员查看举报
minecraft
/reportlist
效果：显示所有举报记录

minecraft
/reportlist 2
效果：查看第2页

📦 安装方法
将插件jar文件放入 plugins 文件夹

重启服务器或使用 plugman load Report

配置文件在 plugins/Report/config.yml

💾 数据存储
举报记录保存在 plugins/Report/reports.yml

自动清理超过指定天数的记录

包含举报者、被举报者、理由、时间

📌 注意事项
不能举报自己

有冷却时间防止刷屏

管理员会实时收到新举报通知

举报记录自动过期删除
