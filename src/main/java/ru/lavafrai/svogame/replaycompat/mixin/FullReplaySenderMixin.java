package ru.lavafrai.svogame.replaycompat.mixin;

import com.mojang.logging.LogUtils;
import com.replaymod.replay.FullReplaySender;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.packet.State;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.replaymod.core.versions.MCVer.getPacketTypeRegistry;

/**
 * Fixes incompatibility between ReForgedPlay and ForgeClientResetPacket.
 *
 * During replay playback, the recorded reset packet (fml:handshake, discriminator 98)
 * would reset the connection to LOGIN state, breaking subsequent PLAY packets.
 * This mixin intercepts the reset, manages the registry state transition internally,
 * and skips LOGIN-phase packets until the new GameJoin packet arrives.
 */
@Mixin(FullReplaySender.class)
public class FullReplaySenderMixin {

    @Unique
    private static final Logger replaycompat$LOGGER = LogUtils.getLogger();

    @Unique
    private static final ResourceLocation replaycompat$FML_HANDSHAKE = new ResourceLocation("fml", "handshake");

    @Shadow(remap = false)
    private PacketTypeRegistry registry;

    @Shadow(remap = false)
    protected int lastTimeStamp;

    @Unique
    private boolean replaycompat$afterReset = false;

    @Inject(method = "processPacket", at = @At("HEAD"), cancellable = true, remap = false)
    private void replaycompat$handleResetPacket(Packet<?> p, CallbackInfoReturnable<Packet<?>> cir) {
        if (replaycompat$afterReset && lastTimeStamp == 0) {
            replaycompat$afterReset = false;
        }

        if (replaycompat$afterReset) {
            if (p instanceof ClientboundGameProfilePacket) {
                registry = registry.withLoginSuccess();
                replaycompat$LOGGER.info("[ReplayCompat] Post-reset LoginSuccess processed, registry transitioned to PLAY");
            }

            if (p instanceof ClientboundLoginPacket) {
                replaycompat$afterReset = false;
                replaycompat$LOGGER.info("[ReplayCompat] Post-reset GameJoin received, resuming normal replay playback");
                return;
            }

            cir.setReturnValue(null);
            return;
        }

        if (p instanceof ClientboundCustomPayloadPacket customPayload) {
            if (replaycompat$FML_HANDSHAKE.equals(customPayload.getIdentifier())) {
                replaycompat$LOGGER.info("[ReplayCompat] Intercepted ForgeClientResetPacket during replay, entering post-reset phase");
                registry = getPacketTypeRegistry(State.LOGIN);
                replaycompat$afterReset = true;
                cir.setReturnValue(null);
            }
        }
    }
}
