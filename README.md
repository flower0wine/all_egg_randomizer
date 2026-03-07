# All Egg Randomizer (万物彩蛋)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen)](https://www.minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric-0.18.4+-blue)](https://fabricmc.net/)
[![Java Version](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/License-CC0--1.0-lightgrey)](https://creativecommons.org/publicdomain/zero/1.0/)

A Fabric mod for Minecraft 1.21.11 that adds exciting random effects when throwing eggs!

## 简介

**万物彩蛋 (All Egg Randomizer)** 是一个有趣的 Fabric 模组，当玩家投掷鸡蛋时，会随机触发各种效果。这个模组为游戏增添了惊喜和乐趣——你永远不知道下一个鸡蛋会给你带来什么！

### 核心功能

投掷鸡蛋后，根据配置随机触发以下四种效果类别之一：

| 类别 | 效果描述 |
|------|----------|
| **ENTITY** | 从 Minecraft 生物注册表中随机选择一个生物生成 |
| **ITEM** | 从 Minecraft 物品注册表中随机选择一个物品掉落 |
| **EFFECT** | 随机应用一个药水效果（喷溅型或直接给予型）|
| **EVENT** | 触发世界事件（如雷击）|

## 功能特性

### 随机系统
- 基于加权随机选择算法
- 支持为每个类别设置权重
- 可配置的启用/禁用状态

### 效果系统
- 从 Minecraft 效果注册表动态获取所有可用效果
- 两种施加方式随机选择：
  - **喷溅型**：创建范围效果云，类似喷溅药水
  - **直接给予型**：查找最近的实体并直接施加效果
- 可配置效果持续时间、等级、半径等参数

### 事件系统
- **雷击事件**：支持装饰性闪电（仅视觉效果）或真实闪电（可造成伤害和火焰）
- 可配置雷击目标位置（玩家位置或鸡蛋落点）
- 易于扩展的事件注册系统

### 配置系统
- 多种配置方式：
  - **游戏内 GUI**：直观的多分类配置面板
  - **命令**：游戏命令实时控制
  - **配置文件**：手动编辑 JSON 配置文件

### 命令系统

| 命令 | 功能 |
|------|------|
| `/egg gui` | 打开配置 GUI 面板 |
| `/egg list` | 列出所有配置 |
| `/egg enable <category>` | 启用指定类别 |
| `/egg disable <category>` | 禁用指定类别 |
| `/egg weight <category> <value>` | 设置类别权重 |
| `/egg reload` | 重新加载配置 |

## 安装

### 前置要求
- Minecraft 1.21.11
- Fabric Loader 0.18.4+
- Java 21

### 安装步骤

1. 下载最新版本的模组 JAR 文件
2. 将 JAR 文件放入游戏的 `mods` 文件夹中
3. 启动游戏即可使用

### 构建

```bash
# 克隆仓库
git clone https://github.com/your-repo/all-egg-randomizer.git
cd all-egg-randomizer

# 构建模组
./gradlew build

# 构建产物位于 build/libs/ 目录
```

### 运行测试

```bash
# 运行客户端
./gradlew runClient

# 运行服务器
./gradlew runServer
```

## 配置说明

### 配置文件位置
`config/alleggrandomizer.json`

### 默认配置示例

```json
{
  "version": "1.0.0",
  "categories": {
    "ENTITY": {
      "enabled": true,
      "weight": 1.0,
      "specificSettings": {
        "spawnCount": 1,
        "despawnTime": 600
      }
    },
    "ITEM": {
      "enabled": true,
      "weight": 1.0,
      "specificSettings": {
        "stackSize": 1
      }
    },
    "EFFECT": {
      "enabled": false,
      "weight": 1.0,
      "specificSettings": {
        "duration": 300,
        "amplifier": 0,
        "splashRadius": 3.0,
        "splashDuration": 60,
        "targetType": "ANY",
        "searchRadius": 5.0
      }
    },
    "EVENT": {
      "enabled": false,
      "weight": 1.0,
      "specificSettings": {
        "targetPosition": "EGG",
        "events": ["LIGHTNING"],
        "lightning": {
          "cosmetic": false,
          "damage": true,
          "fire": true
        }
      }
    }
  },
  "globalSettings": {}
}
```

## 项目结构

```
src/
├── main/
│   ├── java/com/alleggrandomizer/
│   │   ├── AllEggRandomizer.java       # 主模组入口
│   │   ├── config/                     # 配置系统
│   │   ├── command/                    # 命令系统
│   │   ├── core/                       # 核心逻辑
│   │   │   ├── EggHitHandler.java      # 鸡蛋命中处理
│   │   │   └── generator/              # 生成器（效果/事件）
│   │   ├── network/                    # 网络通信
│   │   └── random/                     # 随机系统
│   └── resources/
│       └── alleggrandomizer.mixins.json
├── client/
│   ├── java/com/alleggrandomizer/
│   │   ├── AllEggRandomizerClient.java  # 客户端入口
│   │   └── gui/                         # GUI 系统
│   └── resources/
│       └── alleggrandomizer.client.mixins.json
```

## 技术细节

### 技术栈
- **语言**: Java 21
- **构建系统**: Gradle
- **模组加载器**: Fabric
- **映射**: Yarn
- **依赖库**: Fabric API, Gson

### 核心类说明

| 类名 | 职责 |
|------|------|
| `EggHitHandler` | 处理鸡蛋命中事件，调度随机效果 |
| `WeightedRandomSystem` | 加权随机选择算法实现 |
| `EffectGenerator` | 效果生成器 |
| `EventGenerator` | 事件生成器 |
| `ConfigManager` | 配置文件管理器 |
| `EggCommand` | 命令注册与处理 |

## 扩展开发

### 添加新的效果施加方式

```java
// 1. 在 EffectApplyMode 枚举中添加
public enum EffectApplyMode {
    SPLASH, DIRECT, NEW_MODE
}

// 2. 实现 EffectApplier 接口
public class NewModeApplier implements EffectApplier {
    @Override
    public void apply(ServerWorld world, Vec3d pos, MobEffect effect, int duration, int amplifier, ModConfig config) {
        // 实现逻辑
    }
}

// 3. 注册
EffectGenerator.registerApplier(EffectApplyMode.NEW_MODE, new NewModeApplier());
```

### 添加新的事件类型

```java
// 1. 实现 WorldEvent 接口
public class ExplosionEvent implements WorldEvent {
    @Override
    public void trigger(ServerWorld world, Vec3d pos, ModConfig config) {
        // 实现逻辑
    }
}

// 2. 注册
EventGenerator.registerEvent(new ExplosionEvent());
```

## 许可证

本项目采用 [CC0-1.0](https://creativecommons.org/publicdomain/zero/1.0/) 许可证。

## 贡献者

- **flowerwine** - 开发者
- **任性** - 开发者

## 反馈与支持

如遇到问题或有功能建议，请提交 Issue。

---

*投掷鸡蛋，探索无限可能！*
