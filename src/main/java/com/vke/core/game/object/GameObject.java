package com.vke.core.game.object;

import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.game.object.controller.GameObjectController;

public interface GameObject {
    default boolean isSpawned() {
        return entityId() != -1;
    }

    int entityId();

    /**
     * Spawn this GameObject into the world, creating the components along the way.
     */
    void spawn();
    void destroy();

    void onSpawned();

    GameObjectTransform getTransform();

    <G extends GameObject> G duplicate();

    /**
     * Spawns an amount of new GameObjects using this one as the preset. This means that all components will
     * be also on the new entities with the same values.
     * @param num the amount of entities to spawn
     */
    <G extends GameObject> G[] spawnBatch(int num, G... ignore);

    /**
     * Adds the specified NEW components onto this GameObject. Components that are already present will be ignored.
     * Keep in mind, that this will transition the underlying ecs entity into a new Archetype, so if you need to hold an external
     * reference to any component attached to this entity, look into {@link ComponentReference} or {@link com.vke.core.ecs.ComponentProxy ComponentProxy}.
     */
    void addComponents(int... componentId);

    /**
     * Removes the specified components from this GameObject. Components that don't exist will be ignored.
     * Keep in mind, that this will transition the underlying ecs entity into a new Archetype, so if you need to hold an external
     * reference to any component attached to this entity, look into {@link ComponentReference} or {@link com.vke.core.ecs.ComponentProxy ComponentProxy}.
     * <br><br>
     * If this GameObject is actually a {@link RestrictedGameObject} and the removed components contain any fixed ones, this method will throw an exception
     * to alert the game developer instead.
     */
    void removeComponents(int... componentId);

    /**
     * Acquire a new {@link ComponentReference} for the specified component via its id.
     */
    <T extends Component> ComponentReference<T> getComponent(int id);
    ComponentMask components();

    void control(GameObjectController controller);
}
