


这份文档已将您提供的资料转化为结构化的**开发者文档**，去除了口语化表述，提取了核心原理、技术方案及代码规范，适合直接作为知识库、Wiki 或技术团队的参考文档。

---

# Fabric 1.21.x 实体模型倒立渲染开发指南

## 概述
在 Minecraft Fabric 1.21.x 环境下，实现 Living Entity（生物实体）模型倒立的核心原理是对实体渲染时的 **MatrixStack（矩阵栈）** 进行操作。具体表现为**沿 X 轴旋转 180°**，并结合**平移矩阵（Translation）** 调整旋转支点，以防止模型脱离原本的物理坐标。

开发者可根据需求，从原版机制、模型预处理、自定义渲染器或 Mixin 注入四种方案中进行选择。

---

## 方案一：原版命名标签机制（无代码 / 测试方案）
**适用场景**：快速调试、无需编写代码的原版特性利用、强制模组（如 UpSideDownAlways）。

Minecraft 原版 `LivingEntityRenderer` 已内置倒立渲染逻辑。
- **实现方式**：在游戏中给实体使用命名牌，命名为 `Dinnerbone` 或 `Grumm`（忽略大小写）。
- **技术细节**：渲染器会在每帧检测实体自定义名称（CustomName），若匹配则自动应用矩阵反转。该方案兼容所有原版及大多数自定义实体。

---

## 方案二：模型层预定义（Blockbench 建模期）
**适用场景**：完全自定义的实体模型，且该实体只需保持倒立状态。

在导出 Java 实体模型前，直接在建模软件中固化倒立状态，可免去运行时的矩阵运算开销。
1. 在 Blockbench 中打开模型。
2. 选中模型的**根节点（Root folder/bone）**。
3. 将 **Rotation X（X轴旋转角）** 设置为 `180`。
4. 导出为 Java 代码并在模组中正常注册使用。

---

## 方案三：自定义渲染器矩阵变换（针对特定新增生物）
**适用场景**：为开发者自行添加的新生物编写渲染逻辑。

通过重写 `MobRenderer` 的 `render` 方法，在模型渲染前对局部坐标系进行平移与旋转变换。

### 核心代码实现
```java
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class CustomEntityRenderer extends MobRenderer<CustomEntity, CustomEntityRenderState, CustomEntityModel> {
    
    // ... 构造器及其他必须方法 ...

    @Override
    public void render(CustomEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push(); // 保存当前矩阵状态 (注：部分 mapping 下可能为 pushPose())

        // --- 倒立变换核心逻辑 ---
        // 1. 定义模型高度偏移量（建议：玩家类为 1.8F，根据具体模型微调，或从 state 动态获取）
        float heightOffset = 1.8F; 
        
        // 2. 向上平移至模型顶部（将旋转支点移至头顶）
        matrices.translate(0.0F, heightOffset + 0.1F, 0.0F);
        
        // 3. 沿 X 轴翻转 180 度
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        
        // 4. 向下平移恢复位置
        matrices.translate(0.0F, -(heightOffset + 0.1F), 0.0F);

        // 调用父类渲染逻辑（处理 Yaw 偏航角、模型动画等）
        super.render(state, matrices, vertexConsumers, light);

        matrices.pop(); // 恢复矩阵状态
    }
}
```
**注意**：`CustomEntityRenderState` 需要继承自 `LivingEntityRenderState`。若实体高度动态变化，应在 Entity Tick 中更新 state 的 height 字段。

---

## 方案四：Mixin 全局注入（针对现有/全局生物）
**适用场景**：开发类似 UpSideDownAlways 的全局模组，或需要强制修改第三方模组生物的模型状态。

通过 Mixin 拦截 `LivingEntityRenderer`，在调用 `EntityModel#render` 之前注入矩阵变换。

### Mixin 注入代码
```java
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    
    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At(
            value = "INVOKE", 
            target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", 
            shift = At.Shift.BEFORE
        )
    )
    private void injectUpsideDown(LivingEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // 无需 push/pop，直接在已有矩阵上下文叠加变换
        float height = 1.8F; // 进阶：可通过 state.boundingBox 等属性动态获取高度
        
        matrices.translate(0.0F, height + 0.1F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        matrices.translate(0.0F, -(height + 0.1F), 0.0F);
    }
}
```
*提示：目标签名的 `@At` 拦截点在跨版本时可能存在微变，建议使用 Fabric Loom / 字节码查看器核对确切 mapping。*

---

## ⚠️ 避坑指南与开发建议

1. **坐标系与支点陷阱**
   实体模型的原点（Pivot）通常在脚底 $(0,0,0)$。如果不经过 `translate` 平移直接旋转 $180^\circ$，模型会以脚底为圆心向下翻转，导致**模型陷入地下**。必须遵循 `上移 -> 旋转 -> 下移` 的标准流水线。
2. **API 兼容性 (1.21.x)**
   - `MatrixStack` 的相关 API 在 1.21 已趋于稳定。
   - 映射表（Mappings）提示：在 Fabric Yarn mapping 中通常为 `push()` / `pop()`，而在 Mojmap (NeoForge/Paper) 中可能为 `pushPose()` / `popPose()`，请根据你的构建环境自动补全。
3. **副作用评估**
   使用矩阵反转渲染时，**阴影 (Shadows)** 和 **基础骨骼动画 (Animations)** 会由父类或原版系统自动正确处理，不受破坏影响。
4. **模型 JSON 误区**
   Living Entity 的模型是由 Java 代码生成的（通过 `EntityModelLayer`），**不要**尝试去修改 resource pack 下的 `.json` block/item 模型文件来实现实体的倒立。