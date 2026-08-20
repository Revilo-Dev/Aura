package net.revilodev.aura.abilities;

import net.revilodev.aura.abilities.logic.AbilityLevelIntegrationEvents;
import net.revilodev.aura.abilities.logic.AbilityPowerEnchantmentEvents;
import net.revilodev.aura.abilities.logic.AbilityCombatEvents;
import net.revilodev.aura.abilities.logic.AbilitySyncEvents;

// abilities events
public final class AbilitiesEvents {
    private static boolean REGISTERED = false;

    private AbilitiesEvents() {}

    public static void register() {
        if (REGISTERED) return;
        REGISTERED = true;

        AbilitySyncEvents.register();
        AbilityLevelIntegrationEvents.register();
        AbilityPowerEnchantmentEvents.register();
        AbilityCombatEvents.register();
    }
}
