<render-pass name="scene2d">
    <class name="com.vke.impl.rendering.twoD.pass.Scene2DRenderPass"/>
    <inputs>

    </inputs>
    <outputs>
        <texture name="colorOut"/>
        <texture name="depthOut" type="depth_stencil" format="DEPTH32F_STENCIL8"/>
    </outputs>
</render-pass>