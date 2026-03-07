# SPEC-06: 效果系统实现说明

## 实现状态：✅ 已完成

## 实现变更说明

### 与原规范的差异

1. **效果施加方式改为随机选择**
   - 原规范：通过配置 `applyMode` 参数指定 SPLASH 或 DIRECT
   - 实际实现：每次触发时随机选择 SPLASH 或 DIRECT 模式
   - 理由：符合用户需求"效果需要有两种方式赋予"，增加随机性和趣味性

2. **配置简化**
   - 移除了 `applyMode` 配置项
   - 保留了两种模式各自需要的参数（splashRadius、searchRadius 等）

### 实现的功能

#### 效果系统核心
- ✅ 从 Minecraft 效果注册表随机选择效果
- ✅ 随机选择施加方式（SPLASH 或 DIRECT）
- ✅ 喷溅型效果：创建 AreaEffectCloud，类似喷溅药水
- ✅ 直接给予型效果：查找最近的实体/玩家并直接施加
- ✅ 可配置参数：持续时间、等级、半径、目标类型等

#### 架构设计
- ✅ 策略模式：不同施加方式作为独立策略
- ✅ 注册表模式：统一管理效果施加器
- ✅ 易于扩展：添加新的施加方式只需实现接口并注册

### 文件结构

```
src/main/java/com/alleggrandomizer/
├── core/
│   └── generator/
│       ├── EffectGenerator.java            # 效果生成器（随机选择效果和施加方式）
│       └── effect/
│           ├── EffectApplier.java          # 效果施加器接口
│           ├── SplashEffectApplier.java    # 喷溅型实现
│           └── DirectEffectApplier.java    # 直接给予型实现
├── config/
│   └── EffectApplyMode.java                # 效果施加方式枚举
```

### 配置示例

```json
{
  "effect": {
    "enabled": true,
    "weight": 1.0,
    "specificSettings": {
      "duration": 300,              // 效果持续时间（tick）
      "amplifier": 0,               // 效果等级（0=等级I）
      "splashRadius": 3.0,          // 喷溅半径（SPLASH模式）
      "splashDuration": 60,         // 喷溅云持续时间（SPLASH模式）
      "targetType": "ANY",          // 目标类型（DIRECT模式）：PLAYER, ENTITY, ANY
      "searchRadius": 5.0           // 搜索半径（DIRECT模式）
    }
  }
}
```

### 使用方式

当鸡蛋击中目标且选中 EFFECT 分类时：
1. 从所有可用效果中随机选择一个效果
2. 随机选择施加方式（50% SPLASH，50% DIRECT）
3. 根据选择的方式应用效果：
   - SPLASH：在碰撞位置创建效果云
   - DIRECT：查找最近的实体并直接施加效果

### 扩展性

添加新的效果施加方式：
```java
// 1. 在 EffectApplyMode 枚举中添加新模式
public enum EffectApplyMode {
    SPLASH, DIRECT, NEW_MODE
}

// 2. 实现 EffectApplier 接口
public class NewModeApplier implements EffectApplier {
    // 实现接口方法
}

// 3. 在 EffectGenerator 中注册
registerApplier(EffectApplyMode.NEW_MODE, new NewModeApplier());
```

### 技术细节

#### 喷溅型效果
- 使用 `AreaEffectCloudEntity` 创建效果云
- 效果云会自动显示对应效果的颜色粒子
- 范围内的实体会持续获得效果

#### 直接给予型效果
- 在指定半径内搜索最近的 LivingEntity
- 支持目标类型过滤（仅玩家、仅实体、任意）
- 如果找不到目标，会记录日志但不会失败

### 验收标准

- ✅ 能够从效果注册表中随机选择效果
- ✅ 能够随机选择施加方式（SPLASH/DIRECT）
- ✅ 喷溅型效果正确生成 AreaEffectCloud 并能被周围实体获取
- ✅ 直接给予型效果正确施加到目标实体/玩家
- ✅ 配置参数（duration, amplifier）能够正确读取和应用
- ✅ 效果系统可与现有 ENTITY、ITEM、EVENT 分类共存
- ✅ 添加新的效果施加方式无需修改现有代码（开闭原则）
- ✅ 代码编译通过，无错误

## 实现日期
2026-03-07

## 实现者备注
- 效果施加方式改为随机选择，更符合"随机"模组的主题
- 两种模式的配置参数都保留在配置文件中，确保灵活性
- 架构设计遵循 SOLID 原则，易于维护和扩展
