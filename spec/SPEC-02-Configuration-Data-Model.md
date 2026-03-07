# SPEC-02: 配置数据模型

## 1. 需求概述

设计并实现模组的配置数据模型，用于管理产物分类的启用/禁用状态、权重设置等配置信息，支持配置的持久化存储和运行时热重载。

## 2. 预期效果

### 2.1 功能预期
- 配置文件格式为 JSON，存储在服务端配置目录
- 默认为每种产物分类提供启用/禁用开关
- 每个分类具有独立的权重值，用于随机算法
- 权重默认全部相等（等概率）

### 2.2 用户体验
- 配置修改后可通过命令或UI面板实时生效
- 配置文件格式清晰，便于手动编辑
- 支持服务器重启后配置保留

## 3. 系统设计

### 3.1 数据结构设计

#### 分类配置 (CategoryConfig)
```json
{
  "enabled": true,
  "weight": 1.0,
  "specificSettings": {}
}
```

#### 完整配置结构 (ModConfig)
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
        "amplifier": 0
      }
    },
    "EVENT": {
      "enabled": false,
      "weight": 1.0,
      "specificSettings": {}
    }
  },
  "globalSettings": {
    "cooldown": 0,
    "maxSpawnPerThrow": 5
  }
}
```

### 3.2 架构设计

#### 模块职责
- **ConfigManager**: 配置管理器，负责配置的加载、保存和热重载
- **ConfigModel**: 配置数据模型，定义配置的数据结构
- **CategoryType**: 枚举类，定义所有产物分类类型

#### 设计模式
- **单例模式**: ConfigManager 全局唯一实例
- **观察者模式**: 支持配置变更监听，用于通知其他模块配置更新

### 3.3 配置持久化

- 配置文件路径: `config/alleggrandomizer.json`
- 使用 Fabric API 的配置加载机制或手动 JSON 序列化
- 版本字段用于未来配置迁移

### 3.4 默认值设计

| 配置项 | 默认值 | 说明 |
|-------|--------|------|
| ENTITY.enabled | true | 默认启用生物 |
| ITEM.enabled | true | 默认启用物品 |
| EFFECT.enabled | false | 默认禁用效果 |
| EVENT.enabled | false | 默认禁用事件 |
| 所有分类权重 | 1.0 | 等概率 |
| globalSettings.cooldown | 0 | 无冷却 |
| globalSettings.maxSpawnPerThrow | 5 | 单次最大生成数 |

## 4. 技术要点

### 4.1 配置加载时机
- 服务器启动时加载
- 提供命令触发重载

### 4.2 线程安全
- 配置读取在主线程进行
- 配置修改提供同步机制

### 4.3 配置验证
- 权重值必须为非负数
- enabled 必须为布尔值
- 异常值使用默认值

## 5. 验收标准

- [ ] 配置文件能够正确生成和加载
- [ ] 配置修改后可以正确保存
- [ ] 默认配置符合需求（生物、物品默认启用）
- [ ] 配置变更可以热重载
- [ ] 配置数据可被其他模块正确访问

## 6. 文件结构 (规划)

```
src/main/java/com/alleggrandomizer/
├── config/
│   ├── ConfigManager.java            # 配置管理器
│   ├── ConfigModel.java             # 配置数据模型
│   ├── CategoryType.java            # 分类枚举
│   └── CategoryConfig.java          # 分类配置模型
```
