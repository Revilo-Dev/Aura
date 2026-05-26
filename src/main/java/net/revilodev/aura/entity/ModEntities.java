package net.revilodev.aura.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.entity.projectile.BurstCubeProjectile;

public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, CodexMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BurstCubeProjectile>> BURST_CUBE = REGISTER.register(
            "burst_cube",
            () -> EntityType.Builder.<BurstCubeProjectile>of(BurstCubeProjectile::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("burst_cube")
    );

    public static void register(IEventBus modBus) {
        REGISTER.register(modBus);
    }
}
