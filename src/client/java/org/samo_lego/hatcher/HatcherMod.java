package org.samo_lego.hatcher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import org.samo_lego.hatcher.event.BlockAttackListener;

public class HatcherMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Add event listeners
        AttackBlockCallback.EVENT.register(new BlockAttackListener());
    }
}