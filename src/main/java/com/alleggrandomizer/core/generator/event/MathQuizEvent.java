package com.alleggrandomizer.core.generator.event;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.*;

/**
 * Math Quiz event implementation.
 * Asks the player a random math question, gives 5 seconds to answer.
 * Correct answer: random reward (emerald, diamond, netherite_ingot 2-6)
 * Wrong/Timeout: punishment (throw to sky, levitation effect)
 */
public class MathQuizEvent implements WorldEvent {

    private static final String EVENT_ID = "MATH_QUIZ";
    private static final int QUIZ_TIMEOUT_TICKS = 100; // 5 seconds (20 ticks per second)
    private static final Random RANDOM = Random.create();
    
    // Active quizzes map (player UUID -> quiz data)
    private static final Map<UUID, MathQuizData> ACTIVE_QUIZZES = new HashMap<>();

    @Override
    public String getEventId() {
        return EVENT_ID;
    }

    @Override
    public boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config) {
        // This event requires egg to get player, so we use executeWithEgg instead
        AllEggRandomizer.LOGGER.warn("MathQuizEvent requires egg parameter, use executeWithEgg");
        return false;
    }

    @Override
    public boolean executeWithEgg(ServerWorld world, Vec3d position, Map<String, Object> config, EggEntity egg) {
        if (world == null || egg == null) {
            AllEggRandomizer.LOGGER.warn("Cannot execute MathQuiz event: world or egg is null");
            return false;
        }

        // Get the player who threw the egg
        Entity owner = egg.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) {
            AllEggRandomizer.LOGGER.warn("MathQuiz event requires a player to throw the egg");
            return false;
        }

        try {
            // Generate math question
            int num1 = RANDOM.nextInt(100) + 1; // 1-100
            int num2 = RANDOM.nextInt(100) + 1; // 1-100
            boolean isAddition = RANDOM.nextBoolean();
            
            int correctAnswer;
            String operator;
            if (isAddition) {
                correctAnswer = num1 + num2;
                operator = "+";
            } else {
                // Ensure positive result for subtraction
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                correctAnswer = num1 - num2;
                operator = "-";
            }

            // Store quiz data
            MathQuizData quizData = new MathQuizData(player.getUuid(), correctAnswer, world);
            ACTIVE_QUIZZES.put(player.getUuid(), quizData);

            // Send question to player
            String question = String.format("%d %s %d = ?", num1, operator, num2);
            player.sendMessage(
                Text.literal("[数学问答] ")
                    .formatted(Formatting.GOLD)
                    .append(Text.literal("请在5秒内回答: " + question).formatted(Formatting.WHITE)),
                false
            );
            
            // Play sound - use the correct API (volume, pitch)
            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            // Schedule timeout using a simple scheduled executor
            // This is a non-blocking approach that executes on server thread
            final UUID playerUuid = player.getUuid();
            final ServerWorld finalWorld = world;
            final MinecraftServer server = finalWorld.getServer();
            
            // Use a simple approach: schedule a delayed task using Java's ExecutorService
            // This is better than Thread.sleep because it doesn't block server threads
            server.execute(() -> {
                // Schedule the timeout check after delay
                java.util.concurrent.ScheduledExecutorService scheduler = 
                    java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        t.setName("alleggrandomizer-quiz-timeout");
                        return t;
                    });
                
                scheduler.schedule(() -> {
                    server.execute(() -> checkTimeout(playerUuid));
                    scheduler.shutdown();
                }, QUIZ_TIMEOUT_TICKS * 50L, java.util.concurrent.TimeUnit.MILLISECONDS);
            });

            AllEggRandomizer.LOGGER.info("MathQuiz started for player {}: {}", player.getName().getString(), question);
            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing MathQuiz event", e);
            return false;
        }
    }

    /**
     * Handle player chat message for quiz answer.
     * Called by mixin or event handler.
     */
    public static boolean handlePlayerChat(ServerPlayerEntity player, String message) {
        UUID playerUuid = player.getUuid();
        
        // Check if player has active quiz
        if (!ACTIVE_QUIZZES.containsKey(playerUuid)) {
            return false; // Not handled by us
        }
        
        MathQuizData quizData = ACTIVE_QUIZZES.remove(playerUuid);
        
        try {
            // Parse answer
            int playerAnswer;
            try {
                playerAnswer = Integer.parseInt(message.trim());
            } catch (NumberFormatException e) {
                // Invalid answer format
                player.sendMessage(
                    Text.literal("[数学问答] ").formatted(Formatting.RED)
                        .append(Text.literal("答案格式无效！正确答案是: " + quizData.correctAnswer).formatted(Formatting.WHITE)),
                    false
                );
                applyPunishment(player);
                return true;
            }
            
            // Check answer
            if (playerAnswer == quizData.correctAnswer) {
                // Correct answer - give reward
                applyReward(player);
                player.sendMessage(
                    Text.literal("[数学问答] ").formatted(Formatting.GREEN)
                        .append(Text.literal("回答正确！奖励已发放。").formatted(Formatting.WHITE)),
                    false
                );
                AllEggRandomizer.LOGGER.info("Player {} answered correctly: {}", 
                    player.getName().getString(), quizData.correctAnswer);
            } else {
                // Wrong answer
                player.sendMessage(
                    Text.literal("[数学问答] ").formatted(Formatting.RED)
                        .append(Text.literal("回答错误！正确答案是: " + quizData.correctAnswer).formatted(Formatting.WHITE)),
                    false
                );
                applyPunishment(player);
                AllEggRandomizer.LOGGER.info("Player {} answered wrong: {} (correct: {})", 
                    player.getName().getString(), playerAnswer, quizData.correctAnswer);
            }
        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error handling math quiz answer", e);
        }
        
        return true;
    }

    /**
     * Check if quiz timed out.
     */
    private static void checkTimeout(UUID playerUuid) {
        if (!ACTIVE_QUIZZES.containsKey(playerUuid)) {
            return;
        }
        
        MathQuizData quizData = ACTIVE_QUIZZES.remove(playerUuid);
        
        // Get player from UUID
        ServerPlayerEntity player = quizData.world.getServer().getPlayerManager().getPlayer(playerUuid);
        if (player == null) {
            return;
        }
        
        // Timeout - apply punishment
        player.sendMessage(
            Text.literal("[数学问答] ").formatted(Formatting.RED)
                .append(Text.literal("回答超时！").formatted(Formatting.WHITE)),
            false
        );
        
        applyPunishment(player);
        AllEggRandomizer.LOGGER.info("Player {} timed out on math quiz", player.getName().getString());
    }

    /**
     * Apply reward for correct answer.
     */
    private static void applyReward(ServerPlayerEntity player) {
        // Random reward: emerald, diamond, or netherite_ingot (2-6 pieces)
        int rewardAmount = RANDOM.nextInt(5) + 2; // 2-6
        ItemStack reward;
        
        int rewardType = RANDOM.nextInt(3);
        switch (rewardType) {
            case 0:
                reward = new ItemStack(Items.EMERALD, rewardAmount);
                break;
            case 1:
                reward = new ItemStack(Items.DIAMOND, rewardAmount);
                break;
            default:
                reward = new ItemStack(Items.NETHERITE_INGOT, rewardAmount);
                break;
        }
        
        // Give item to player
        boolean gaveItem = player.getInventory().insertStack(reward);
        if (!gaveItem) {
            // If inventory full, drop at player position
            player.dropItem(reward, true);
        }
        
        // Play reward sound - use correct API
        player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        AllEggRandomizer.LOGGER.info("Gave reward to {}: {} x{}", 
            player.getName().getString(), reward.getItem(), rewardAmount);
    }

    /**
     * Apply punishment for wrong answer or timeout.
     * Punishment: throw to sky (y=255), levitation effect
     */
    private static void applyPunishment(ServerPlayerEntity player) {
        // Get current position
        double posX = player.getX();
        double posY = player.getY();
        double posZ = player.getZ();
        
        // Throw player to sky (y = 255)
        player.setPosition(posX, 255, posZ);
        
        // Apply slow falling effect so they float down safely
        // SLOW_FALLING prevents fall damage and makes player descend slowly
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SLOW_FALLING,
            100, // 5 seconds
            0, // level 0 (normal slow falling)
            false,
            false
        ));
        
        // Play sound
        player.playSound(SoundEvents.ENTITY_WITCH_CELEBRATE, 1.0f, 1.0f);
        
        AllEggRandomizer.LOGGER.info("Applied punishment to player {}", player.getName().getString());
    }

    @Override
    public String getDescription() {
        return "Asks player a math question, rewards correct answer, punishes wrong/timeout";
    }

    /**
     * Data class to store active quiz information.
     */
    private static class MathQuizData {
        final UUID playerUuid;
        final int correctAnswer;
        final ServerWorld world;

        MathQuizData(UUID playerUuid, int correctAnswer, ServerWorld world) {
            this.playerUuid = playerUuid;
            this.correctAnswer = correctAnswer;
            this.world = world;
        }
    }
}
