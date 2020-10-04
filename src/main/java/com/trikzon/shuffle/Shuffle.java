package com.trikzon.shuffle;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Shuffle.MOD_ID)
public class Shuffle {
    public static final String MOD_ID = "shuffle";

    public Shuffle() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientMod::new);
    }
}
