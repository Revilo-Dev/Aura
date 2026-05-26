package net.revilodev.aura.skills;

public final class SkillsEvents {
    private SkillsEvents() {}

    private static boolean REGISTERED = false;

    public static void register() {
        if (REGISTERED) return;
        REGISTERED = true;

        net.revilodev.aura.skills.logic.SkillSyncEvents.register();
        net.revilodev.aura.skills.logic.LevelUpSkillIntegrationEvents.register();
        net.revilodev.aura.skills.logic.SkillEvents.register();
    }
}
