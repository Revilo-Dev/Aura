package net.revilodev.aura.abilities.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityId;

public abstract class AbilitySwitchEvent extends Event {
    private final ServerPlayer player;
    private final AbilityElement element;
    private final AbilityId fromAbility;
    private final AbilityId toAbility;

    protected AbilitySwitchEvent(ServerPlayer player, AbilityElement element, AbilityId fromAbility, AbilityId toAbility) {
        // passes ability switch details
        this.player = player;
        this.element = element;
        this.fromAbility = fromAbility;
        this.toAbility = toAbility;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public AbilityElement getElement() {
        return element;
    }

    public AbilityId getFromAbility() {
        return fromAbility;
    }

    public AbilityId getToAbility() {
        return toAbility;
    }

    public static final class Pre extends AbilitySwitchEvent implements ICancellableEvent {
        public Pre(ServerPlayer player, AbilityElement element, AbilityId fromAbility, AbilityId toAbility) {
            super(player, element, fromAbility, toAbility);
        }
    }

    public static final class Post extends AbilitySwitchEvent {
        public Post(ServerPlayer player, AbilityElement element, AbilityId fromAbility, AbilityId toAbility) {
            super(player, element, fromAbility, toAbility);
        }
    }
}
