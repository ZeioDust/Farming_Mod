package com.cultivar.client;

import com.cultivar.Cultivar;
import com.cultivar.client.guide.GuideClient;
import com.cultivar.guide.GuideHooks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Cultivar.MODID, dist = Dist.CLIENT)
public class CultivarClient {
    public CultivarClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        GuideHooks.install(GuideClient::open);
    }
}
