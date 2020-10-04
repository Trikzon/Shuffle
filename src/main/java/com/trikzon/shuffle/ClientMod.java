package com.trikzon.shuffle;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static com.trikzon.shuffle.Shuffle.MOD_ID;

public class ClientMod {
    private static final KeyBinding keyBinding = new KeyBinding(
            "key." + MOD_ID + ".shuffle",
            GLFW.GLFW_KEY_R,
            "key.category." + MOD_ID
    );
    private static boolean shuffleMode = false;
    private static boolean keyWasDown = false;
    private static int slotToSwitchTo = -1;

    public ClientMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(this::onBlockUse);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    private void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (!event.side.isClient()) return;
        if (event.player == null) return;
        PlayerEntity player = event.player;

        if (keyBinding.isPressed() && !keyWasDown) {
            keyWasDown = true;
            shuffleMode = !shuffleMode;
            if (shuffleMode) {
                player.sendStatusMessage(new TranslationTextComponent("message.shuffle.enable"), true);
                player.playSound(SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF, 0.5f, 1.0f);
            } else {
                player.sendStatusMessage(new TranslationTextComponent("message.shuffle.disable"), true);
                player.playSound(SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 1.0f);
            }
        } else if (!keyBinding.isPressed() && keyWasDown) {
            keyWasDown = false;
        }

        if (slotToSwitchTo >= 0 && slotToSwitchTo <= 8) {
            player.inventory.currentItem = slotToSwitchTo;
            slotToSwitchTo = -1;
        }
    }

    private void onBlockUse(final PlayerInteractEvent.RightClickBlock event) {
        if (!event.getWorld().isRemote || !shuffleMode ||
                !(event.getEntity() instanceof PlayerEntity) || event.getPlayer().isSpectator()) {
            return;
        }

        PlayerEntity player = event.getPlayer();
        Item itemInHand = player.getHeldItem(event.getHand()).getItem();
        if (Block.getBlockFromItem(itemInHand) != Blocks.AIR && itemInHand != Items.AIR) {
            ArrayList<Integer> slotsWithBlocks = new ArrayList<>();
            for (int i = 0; i <= 8; i++) {
                Item item = player.inventory.mainInventory.get(i).getItem();
                if (Block.getBlockFromItem(item) != Blocks.AIR && item != Items.AIR) {
                    slotsWithBlocks.add(i);
                }
            }
            if (slotsWithBlocks.size() > 0)
            {
                int randomSlot = event.getWorld().getRandom().nextInt(slotsWithBlocks.size());
                slotToSwitchTo = slotsWithBlocks.get(randomSlot);
            }
        }
    }
}
