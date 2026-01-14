package codes.wunder.hytaleregions.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class CommandUtil {

    private CommandUtil() {
    }

    public static PlayerRef requirePlayerRef(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr == null) throw new IllegalStateException("Missing PlayerRef component.");
        return pr;
    }

    public static TransformComponent requireTransform(Ref<EntityStore> ref, Store<EntityStore> store) {
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) throw new IllegalStateException("Missing TransformComponent.");
        return t;
    }

    public static int chunkOf(double coord) {
        // Correct for negatives
        return ((int) Math.floor(coord)) >> 4;
    }

    public static World requireWorld(Store<EntityStore> store) {
        return store.getExternalData().getWorld();
    }
}
