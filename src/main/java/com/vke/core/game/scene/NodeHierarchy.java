package com.vke.core.game.scene;

import com.carrotsearch.hppc.IntArrayList;
import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.core.Context;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.game.object.GameObject;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.WorldTransformC;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.List;

public class NodeHierarchy {
    public static final int INIITIAL_CAP = 1024;

    private CompiledTransformNode[] compiledNodes = new CompiledTransformNode[INIITIAL_CAP];
    private int nodeLength;
    private int nodeCapacity = INIITIAL_CAP;

    private boolean hierarchyDirty = true;

    private final Context context;
    private final EcsManager ecs;

    private final SceneGraph sceneGraph;

    private GameObject[] entityToGameObject;

    public NodeHierarchy(Context context, EcsManager ecs) {
        this.context = context;
//        EcsManager actual = ecs.assumeImplementation();
//        if (!(actual instanceof EcsManagerImpl)) {
//            context.getLogger().warn("3rd Party ECS detected! Make sure that your entity-ids start at 0 and are contiguous!");
//        }

        this.ecs = ecs;

        this.sceneGraph = new SceneGraph(INIITIAL_CAP);
        this.entityToGameObject = new GameObject[INIITIAL_CAP];
    }

    private void growNodes(int min) {
        if (nodeCapacity < min) {
            double cap = nodeCapacity;
            while (cap < min) {
                cap *= CpuBuffer.GROWTH_FAC;
            }
            nodeCapacity = (int) Math.ceil(cap);
            compiledNodes = Arrays.copyOf(compiledNodes, nodeCapacity);
        }
    }

    private CompiledTransformNode getOrCreateNode(int index) {
        growNodes(index + 1);
        if (compiledNodes[index] == null) {
            compiledNodes[index] = new CompiledTransformNode();
        }
        return compiledNodes[index];
    }

    public void markDirty() {
        this.hierarchyDirty = true;
    }

    public void updateTransforms() {
        if (hierarchyDirty) {
            buildWholeHierarchy();
            hierarchyDirty = false;
        }
        evaluateTransforms();
    }

    public void buildWholeHierarchy() {
        nodeLength = 0;

        // Pass 1: Iterate roots in SceneGraph directly (Skip ECS query overhead for roots)
        int rootEntity = sceneGraph.iterChildren(-1);
        while (rootEntity != -1) {

            nodeLength = dfs(rootEntity, -1, nodeLength);
            rootEntity = sceneGraph.nextChild(rootEntity);
        }
    }

    private int dfs(int entity, int parentNodeIdx, int currentWriteIdx) {
        @SuppressWarnings("appearently you can write anything in here and intellij will shut up lmao")
        int nodeIdx = currentWriteIdx;

        EcsManager.EntityLocation location = ecs.locateEntity(entity);
        Archetype at = location.archetype();
        TransformC tc = at.getComponentById(TransformC.ID);
        WorldTransformC wtc = at.getComponentById(WorldTransformC.ID);

        growNodes(nodeIdx + 1);
        CompiledTransformNode node = buildCompiledNode(nodeIdx, parentNodeIdx, location.index(), tc, wtc);

        int nextWriteIdx = nodeIdx + 1;

        int child = sceneGraph.iterChildren(entity);
        while (child != -1) {
            nextWriteIdx = dfs(child, nodeIdx, nextWriteIdx);
            child = sceneGraph.nextChild(child);
        }

        node.subSize = (nextWriteIdx - 1) - nodeIdx;
        return nextWriteIdx;
    }

    private CompiledTransformNode buildCompiledNode(int nodeIdx, int parentNodeIdx, int localIndex, TransformC tc, WorldTransformC wtc) {
        CompiledTransformNode cn = getOrCreateNode(nodeIdx);
        cn.dirty = tc.dirty;
        cn.dirty[localIndex] = true;
        cn.x = tc.x;
        cn.y = tc.y;
        cn.z = tc.z;

        cn.ox = tc.ox;
        cn.oy = tc.oy;
        cn.oz = tc.oz;

        cn.sx = tc.sx;
        cn.sy = tc.sy;
        cn.sz = tc.sz;

        cn.rx = tc.rx;
        cn.ry = tc.ry;
        cn.rz = tc.rz;
        cn.rw = tc.rw;
        cn.localIndex = localIndex;

        cn.worldMatrix = wtc.worldMatrix;
        cn.worldMatIndex = localIndex * 16;

        cn.parentNode = parentNodeIdx;
        return cn;
    }

    private void evaluateTransforms() {
        Matrix4f localM = new Matrix4f();
        Matrix4f parentM = new Matrix4f();
        Matrix4f resultM = new Matrix4f();

        int pendingDirty = 0;
        for (int i = 0; i < nodeLength; i++) {
            CompiledTransformNode node = compiledNodes[i];
            if (node != null) {
                if (node.dirty[node.localIndex] || pendingDirty > 0) {
                    pendingDirty = Math.max(pendingDirty - 1, 0);
                    localM.translationRotateScale(
                            node.x[node.localIndex], node.y[node.localIndex], node.z[node.localIndex],
                            node.rx[node.localIndex], node.ry[node.localIndex], node.rz[node.localIndex], node.rw[node.localIndex],
                            node.sx[node.localIndex], node.sy[node.localIndex], node.sz[node.localIndex]
                    );

                    if (node.parentNode == -1) {
                        localM.get(node.worldMatrix, node.worldMatIndex);
                    } else {
                        CompiledTransformNode parent = compiledNodes[node.parentNode];
                        parentM.set(parent.worldMatrix, parent.worldMatIndex);
                        parentM.mul(localM, resultM);
                        resultM.get(node.worldMatrix, node.worldMatIndex);
                    }

                    pendingDirty = Math.max(pendingDirty, node.subSize);

                    node.dirty[node.localIndex] = false;
                }
            }
        }
    }

    public void addChild(int parentId, int childId) {
        sceneGraph.attachToParent(childId, parentId);
        markDirty();
    }

    public void getChildren(int parentId, List<GameObject> dest) {
        int child = sceneGraph.iterChildren(parentId);
        while (child != -1) {
            GameObject obj = entityToGameObject[child];
            dest.add(obj);
            child = sceneGraph.nextChild(child);
        }
    }

    public GameObject upcast(int entity) {
        return entityToGameObject[entity];
    }

    public GameObject getParent(int entity) {
        int parentId = sceneGraph.parentOf(entity);
        return entityToGameObject[parentId];
    }

    public void spawned(int entityId, GameObject obj) {
        if (entityToGameObject.length <= entityId) {
            entityToGameObject = Arrays.copyOf(entityToGameObject, entityId);
        }
        entityToGameObject[entityId] = obj;
        sceneGraph.attachToParent(entityId, -1);
        markDirty();
    }

    public void deleted(int entityId) {
        entityToGameObject[entityId] = null;

        IntArrayList deletedIds = new IntArrayList();
        sceneGraph.deleteNode(entityId, deletedIds);
        markDirty();

        for (int i = 0; i < deletedIds.elementsCount; i++) {
            int delEnt = deletedIds.buffer[i];
            entityToGameObject[delEnt] = null;
        }

        ecs.destroyEntities(deletedIds.buffer, 0, deletedIds.elementsCount);
    }
}
