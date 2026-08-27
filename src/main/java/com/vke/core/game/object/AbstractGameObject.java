package com.vke.core.game.object;

import com.vke.core.Context;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.services2.Services;
import com.vke.utils.Utils;

import java.util.HashSet;

public abstract class AbstractGameObject extends TransformedGameObject {
    protected final Context ctx;
    protected final EcsManager ecs;

    private final HashSet<ComponentReference<?>> activeRefs;

    protected int entityId;
    private ComponentMask mask;

    public AbstractGameObject(Context ctx) {
        this.ctx = ctx;
        this.ecs = ctx.service(Services.ECS);
        this.entityId = -1;

        this.activeRefs = new HashSet<>();
        this.mask = createMask();
    }

    protected abstract ComponentMask createMask();

    @Override
    public int entityId() {
        return entityId;
    }

    @Override
    public void spawn() {
        if (this.entityId != -1) throw new IllegalStateException("Cannot spawn 2 entities from the same game object.");
        this.entityId = ecs.spawnEntities(1, mask, (at, left, right, eidx, eid) -> {
            int idx = left + eidx;
            for (int component : mask.getComponents()) {
                Component c = at.getComponentById(component);
                c.initialize(idx);
            }
        })[0];

        //initialize the component
        transformComponent();

        onSpawned();
    }

    protected abstract void onSpawned();

    protected abstract GameObject createFromSpawnedEntity(int entity);

    @Override
    public GameObject[] spawnBatch(int num) {
        GameObject[] array = new GameObject[num];
        int[] comps = mask.getComponents();
        int numC = comps.length;
        
        /*
        ESSAY
        So the component mask has sorted ids inside of it, that means that the archetypes internal component storage
        MUST have the same natural ordering of components as we do. that means, that we can cache the reference components
        and indices beforehand here in these small helper arrays. the archetypes are immutable, that means their mask wont change.
        thereforere the below optimization hack is actually safe and working.
        ESSAY END
         */
        
        Component[] references = new Component[numC];
        int[] indices = new int[numC];
        for (int i = 0; i < numC; i++) {
            var ref = getComponent(comps[i]);
            references[i] = ref.getComponent();
            indices[i] = ref.getIndex();
        }

        ecs.spawnEntities(num, mask, (at, left, right, entityIndex, entityId) -> {
            Component[] components = at.getComponents();
            for (int i = 0, componentsLength = components.length; i < componentsLength; i++) {
                Component c = components[i];
                c.copyFrom(references[i], left + entityIndex, indices[i]);
            }
            GameObject obj = createFromSpawnedEntity(entityId);
            if (obj instanceof TransformedGameObject t) {
                //init transform as well
                t.transformComponent();
            }
            array[entityIndex] = obj;
        });
        return array;
    }

    @Override
    public void destroy() {
        if (entityId == -1) throw new IllegalStateException("Cannot spawn 2 entities from the same game object.");
        ecs.destroyEntity(entityId);
        for (var r : activeRefs) {
            r.drop();
        }
        activeRefs.clear();
        this.entityId = -1;
    }

    @Override
    public void addComponents(int... componentIds) {
        this.mask = this.mask.addComponents(componentIds);
        ecs.transitionEntity(this.entityId, this.mask, (at, idx) -> {
            for (int componentId : componentIds) {
                Component cmp = at.getComponentById(componentId);
                cmp.initialize(idx);
            }
        });
    }

    @Override
    public void removeComponents(int... componentIds) {
        this.mask = this.mask.removeComponents(componentIds);
        if (this instanceof RestrictedGameObject rgo) {
            int culprit;
            if ((culprit = Utils.intsContainAnyIntThenReturnInt(componentIds, rgo.getFixedComponents())) != -1) {
                String compName = ecs.getComponentName(culprit);
                throw new UnsupportedOperationException(String.format("Cannot remove component %s from %s!", compName, getClass().getSimpleName()));
            }
        }

        ecs.transitionEntity(entityId, mask, null);
    }

    @Override
    public <T extends Component> ComponentReference<T> getComponent(int id) {
        ComponentReference<T> r = ecs.obtainComponentReference(entityId, id);
        this.activeRefs.add(r);
        return r;
    }

    @Override
    public ComponentMask components() {
        return this.mask;
    }
}
