<render-pass name="scene">
    <class name="com.vke.core.rendering.graph2.renderpass.SceneRenderPass"/>
    <inputs>

    </inputs>
    <outputs>
        <texture name="gbuf_normal" type="color" format="RGBA16F"/>
        <texture name="gbuf_material_idx" type="color" format="R32I"/>
        <texture name="gbuf_mesh_uvs" type="color" format="RG16F"/>
    </outputs>
</render-pass>