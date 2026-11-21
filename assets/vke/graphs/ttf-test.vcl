<render-graph name="ttf-test">
    <passes>
        <render-pass name="main">
            <class name="com.vke.test.ttf.TTFRenderPass"/>
            <outputs>
                <screen name="colorOut"/>
<!--                <texture name="depthStencil" type="DEPTH_STENCIL" format="DEPTH32F_STENCIL8"/>-->
                <texture name="depthStencil" type="depth_stencil" format="DEPTH32F_STENCIL8"/>
            </outputs>
        </render-pass>
    </passes>
</render-graph>