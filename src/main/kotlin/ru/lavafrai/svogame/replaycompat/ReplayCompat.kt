package ru.lavafrai.svogame.replaycompat

import com.mojang.logging.LogUtils
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(ReplayCompat.MODID)
class ReplayCompat {
    companion object {
        const val MODID = "replaycompat"
        private val LOGGER = LogUtils.getLogger()
    }

    init {
        MOD_BUS.addListener(this::commonSetup)
        LOGGER.info("Replay Compat mod initialized")
    }

    private fun commonSetup(@Suppress("UNUSED_PARAMETER") event: FMLCommonSetupEvent) {
        LOGGER.info("Replay Compat mod loaded successfully")
    }
}
