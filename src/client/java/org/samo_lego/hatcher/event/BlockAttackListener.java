package org.samo_lego.hatcher.event;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class BlockAttackListener implements AttackBlockCallback {

    private static long lastInvokeTime = 0L;

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (world.isClientSide() && !player.isCreative()) {
            // For some reason, event gets fired twice
            if (System.currentTimeMillis() - lastInvokeTime <= 200L) {
                return InteractionResult.FAIL;
            }

            lastInvokeTime = System.currentTimeMillis();

            var state = world.getBlockState(pos);

            if (state.getBlock() == Blocks.DRAGON_EGG) {
                // Check for piston
                var inventory = player.getInventory();
                int pistonIx = -1;
                int pwrIx = -1;

                for (int i = 0; i < 9; ++i) {
                    var stack = inventory.getItem(i);

                    if (stack.getItem() == Items.PISTON || stack.getItem() == Items.STICKY_PISTON) {
                        pistonIx = i;
                    } else if (stack.getItem() == Items.LEVER) {
                        pwrIx = i;
                    }
                }

                if (pistonIx == -1 || pwrIx == -1) {
                    player.sendSystemMessage(Component.literal("[Hatcher] You need piston and a lever!"));
                    return InteractionResult.PASS;
                }

                int lastSlot = player.getInventory().selected;

                // We have everything, send the packets
                var lplayer = (LocalPlayer) player;
                lplayer.connection.send(new ServerboundSetCarriedItemPacket(pistonIx));

                // Placing piston
                var pistonPos = pos.relative(player.getDirection(), 1);
                var hitResult = new BlockHitResult(pistonPos.getCenter(), player.getDirection(), pistonPos, true);
                Minecraft.getInstance().gameMode.useItemOn(lplayer, hand, hitResult);


                // Place power src on piston
                lplayer.connection.send(new ServerboundSetCarriedItemPacket(pwrIx));
                hitResult = new BlockHitResult(pistonPos.getCenter(), player.getDirection(), pistonPos, true);
                // Place power source
                Minecraft.getInstance().gameMode.useItemOn(lplayer, hand, hitResult);

                // Interact with power source
                var powerSrcPos = pistonPos.relative(player.getDirection(), 1);
                hitResult = new BlockHitResult(powerSrcPos.getCenter(), player.getDirection(), powerSrcPos, true);
                Minecraft.getInstance().gameMode.useItemOn(lplayer, hand, hitResult);


                // Reselect old item
                lplayer.connection.send(new ServerboundSetCarriedItemPacket(lastSlot));

                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }
}
