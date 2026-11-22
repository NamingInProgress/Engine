<render-graph name="demo">
    <passes>
        <render-pass name="main" parent="vke:scene">
            <outputs>
                <texture name="colorOut"/>
                <texture name="depthOut" type="depth_stencil" format="DEPTH32F_STENCIL8"/>
            </outputs>
        </render-pass>

        <render-pass name="debug" parent="debug">
            <outputs>
                <texture name="colorOut" source="main.colorOut"/>
                <texture name="depthOut" type="depth_stencil" format="DEPTH32F_STENCIL8" source="main.depthOut"/>
            </outputs>
        </render-pass>

        <render-pass name="skybox" parent="vke:skybox">
            <outputs>
                <texture name="colorOut" source="debug.colorOut"/>
                <texture name="depthOut" type="depth_stencil" format="DEPTH32F_STENCIL8" source="debug.depthOut"/>
            </outputs>
        </render-pass>

        <render-pass name="post" parent="vke:post">
            <inputs>
                <input name="colorIn" source="skybox.colorOut"/>
            </inputs>
            <data>
                <stages>
                    <stage name="bloom"/>
                </stages>
            </data>
        </render-pass>

        <image-to-screen source="post.colorOut"/>
    </passes>
</render-graph>