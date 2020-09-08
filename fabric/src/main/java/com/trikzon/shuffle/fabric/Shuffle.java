package com.trikzon.shuffle.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class Shuffle implements ClientModInitializer
{
    private static final String MOD_ID = "shuffle";

    private static final KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".shuffle",
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R,
            "key.category." + MOD_ID
    ));
    private static boolean shuffleMode = false;
    private static boolean keyWasDown = false;
    private static int slotToSwitchTo = -1;

    private void onClientTick(MinecraftClient client)
    {
        if (client.player == null) return;
        ClientPlayerEntity player = client.player;

        if (keyBinding.isPressed() && !keyWasDown)
        {
            keyWasDown = true;

            shuffleMode = !shuffleMode;
            if (shuffleMode)
            {
                player.addChatMessage(new TranslatableText("message.shuffle.enable"), true);
            }
            else
            {
                player.addChatMessage(new TranslatableText("message.shuffle.disable"), true);
            }
        }
        else if (!keyBinding.isPressed() && keyWasDown)
        {
            keyWasDown = false;
        }

        if (slotToSwitchTo >= 0 && slotToSwitchTo <= 8)
        {
            player.inventory.selectedSlot = slotToSwitchTo;
            slotToSwitchTo = -1;
        }
    }

    private ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult result)
    {
        if (!world.isClient || !shuffleMode || player.isSpectator())
        {
            return ActionResult.PASS;
        }

        Item itemInHand = player.getStackInHand(hand).getItem();
        if (Block.getBlockFromItem(itemInHand) != Blocks.AIR && itemInHand != Items.AIR)
        {
            ArrayList<Integer> slotsWithBlocks = new ArrayList<>();
            for (int i = 0; i <= 8; i++)
            {
                Item item = player.inventory.main.get(i).getItem();
                if (Block.getBlockFromItem(item) != Blocks.AIR && item != Items.AIR)
                {
                    slotsWithBlocks.add(i);
                }
            }
            if (slotsWithBlocks.size() > 0)
            {
                int randomSlot = world.random.nextInt(slotsWithBlocks.size());
                slotToSwitchTo = slotsWithBlocks.get(randomSlot);
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void onInitializeClient()
    {
        ClientTickCallback.EVENT.register(this::onClientTick);
        UseBlockCallback.EVENT.register(this::onBlockUse);
    }
}