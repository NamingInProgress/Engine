<render-graph>
    <render-pass name="main">
        <class name="com.vke.test.ttf.TTFRenderPass"/>
        <outputs>
            <texture name="depthStencil" type="DEPTH_STENCIL" format="DEPTH32F_STENCIL8"/>
            <render-target name="colorOut"/>
        </outputs>
    </render-pass>
</render-graph>