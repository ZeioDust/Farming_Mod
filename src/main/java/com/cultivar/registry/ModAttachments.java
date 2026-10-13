package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.guide.GuideState;
import com.cultivar.world.CrossLog;
import com.cultivar.world.WindVector;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Cultivar.MODID);

    public static final Supplier<AttachmentType<WindVector>> WIND = ATTACHMENTS.register("wind",
            () -> AttachmentType.builder(() -> WindVector.CALM).serialize(WindVector.MAP_CODEC).build());

    public static final Supplier<AttachmentType<CrossLog>> CROSS_LOG = ATTACHMENTS.register("cross_log",
            () -> AttachmentType.builder(CrossLog::empty).serialize(CrossLog.MAP_CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<GuideState>> GUIDE = ATTACHMENTS.register("guide",
            () -> AttachmentType.builder(GuideState::empty).serialize(GuideState.MAP_CODEC).copyOnDeath().build());

    private ModAttachments() {
    }
}
