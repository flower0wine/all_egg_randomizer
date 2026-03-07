# SPEC-01: 鸡蛋投掷核心系统

## 1. 需求概述

实现模组的核心功能：监听玩家投掷鸡蛋的事件，根据配置随机生成产物（生物、物品、效果、事件等）。

## 2. 预期效果

### 2.1 功能预期
- 玩家使用鸡蛋（Egg）进行投掷时，鸡蛋落地或击中实体后触发随机产物生成
- 产物类型由配置系统控制，默认启用生物（Entity）和物品（Item）
- 支持产物生成的扩展性，后续可添加新的产物类型

### 2.2 用户体验
- 鸡蛋投掷行为保持原版特性（投掷轨迹、击中反馈）
- 产物生成时应有适当的视觉/音效反馈
- 产物生成位置为鸡蛋着地点或击中实体位置

## 3. 系统设计

### 3.1 架构设计

#### 模块职责
- **EggThrowEventHandler**: 监听鸡蛋投掷事件，负责事件捕获
- **OutputGenerator**: 产物生成器接口，定义产物生成的抽象行为
- **OutputRegistry**: 产物类型注册表，管理所有可用的产物生成器

#### 设计模式
- **策略模式**: 不同的产物类型（生物、物品、效果、事件）作为不同的策略实现
- **注册表模式**: OutputRegistry 统一管理所有产物生成策略，支持运行时注册

### 3.2 事件流

```
玩家投掷鸡蛋
    ↓
EggThrowEvent (ProjectileHitEvent / EntityHitEvent)
    ↓
EggThrowEventHandler 捕获事件
    ↓
查询配置系统获取当前启用的产物分类
    ↓
调用随机算法系统确定产物类型
    ↓
从对应分类中选择具体产物
    ↓
OutputGenerator 生成产物
    ↓
消耗鸡蛋（若击中实体）
```

### 3.3 产物分类

| 分类标识 | 说明 | 示例 |
|---------|------|------|
| ENTITY | 生物 | 牛、猪、羊、僵尸、骷髅等 |
| ITEM | 物品 | 钻石、金锭、苹果、附魔书等 |
| EFFECT | 药水效果 | 速度、跳跃提升、夜视等 |
| EVENT | 世界事件 | 闪电、 Thunder、爆炸等 |

### 3.4 可扩展性设计

- 新产物分类通过实现 `IOutputGenerator` 接口并注册到 `OutputRegistry` 即可添加
- 每个产物分类拥有独立的选择器和生成器
- 产物选择器负责从对应分类中选取具体产物

## 4. 技术要点

### 4.1 Mixin 注入点
- 目标类: `net.minecraft.class_1657` (Entity class in yarn mappings)
- 或使用 Fabric API 的 `ProjectileHitEvent`

### 4.2 依赖模块
- 配置系统 (SPEC-02): 获取启用状态和权重配置
- 随机算法系统 (SPEC-04): 生成随机产物类型

### 4.3 边界情况处理
- 鸡蛋未击中任何实体/方块时的处理
- 产物生成位置被阻挡时的处理
- 产物生成可能导致的性能问题（防止大量实体/物品生成）

## 5. 验收标准

- [ ] 鸡蛋投掷后能够正确触发产物生成
- [ ] 产物生成位置正确（鸡蛋着地点）
- [ ] 未击中任何目标时也能生成产物
- [ ] 可以正确禁用/启用各产物分类
- [ ] 新产物类型可正确注册和使用

## 6. 文件结构 (规划)

```
src/main/java/com/alleggrandomizer/
├── core/
│   ├── EggThrowEventHandler.java     # 事件处理核心
│   ├── OutputRegistry.java           # 产物注册表
│   └── generator/
│       ├── IOutputGenerator.java     # 产物生成器接口
│       ├── EntityGenerator.java      # 生物生成器
│       ├── ItemGenerator.java        # 物品生成器
│       ├── EffectGenerator.java      # 效果生成器
│       └── EventGenerator.java       # 事件生成器
```
