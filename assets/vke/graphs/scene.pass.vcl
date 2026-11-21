<render-pass name="scene">
    <class name="com.vke.impl.rendering.pass.SceneRenderPass"/>
    <inputs>

    </inputs>
    <outputs>
        <!-- Deferred -->
        <texture name="gbuf_normal" type="color" format="RGBA16F"/>
        <texture name="gbuf_material_idx" type="color" format="R32I"/>
        <texture name="gbuf_mesh_uvs" type="color" format="RG16F"/>

        <!-- Text -->
<!--        <texture name="stencilOut" type="stencil" format="STENCIL8"/>-->
    </outputs>
</render-pass>