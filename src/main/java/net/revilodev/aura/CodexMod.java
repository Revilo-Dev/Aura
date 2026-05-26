package net.revilodev.aura;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilitiesEvents;
import net.revilodev.aura.abilities.AbilitiesNetwork;
import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.attributes.CodexAttributes;
import net.revilodev.aura.effect.CodexMobEffects;
import net.revilodev.aura.effect.CodexPotions;
import net.revilodev.aura.entity.ModEntities;
import net.revilodev.aura.item.ModItems;
import net.revilodev.aura.skills.SkillsAttachments;
import net.revilodev.aura.skills.SkillConfig;
import net.revilodev.aura.skills.SkillsEvents;
import net.revilodev.aura.skills.SkillsNetwork;
import net.revilodev.aura.stats.CodexStats;

@Mod(CodexMod.MOD_ID)
public final class CodexMod {
    public static final String MOD_ID = "aura";

    public CodexMod(IEventBus modBus, ModContainer container) {
        ModItems.register(modBus);                 // <-- REQUIRED
        CodexAttributes.register(modBus);
        CodexMobEffects.register(modBus);
        CodexPotions.register(modBus);
        ModEntities.register(modBus);
        CodexStats.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, SkillConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, AbilityConfig.SPEC, MOD_ID + "-abilities-server.toml");

        SkillsAttachments.REGISTER.register(modBus);
        AbilitiesAttachments.REGISTER.register(modBus);
        modBus.addListener(SkillsNetwork::onRegisterPayloadHandlers);
        modBus.addListener(AbilitiesNetwork::onRegisterPayloadHandlers);
        net.revilodev.aura.skills.command.SkillsCommands.register();
        net.revilodev.aura.abilities.command.AbilitiesCommands.register();

        SkillsEvents.register();
        AbilitiesEvents.register();
    }
}
