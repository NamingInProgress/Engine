<render-graph name="demo">
    <passes>
        <render-pass name="main" parent="vke:deferred">
            <outputs>
                <texture name="colorOut"/>
                <texture name="depthOut" type="depth" format="DEPTH32F"/>
            </outputs>
        </render-pass>

        <render-pass name="debug" parent="debug">
            <outputs>
                <texture name="colorOut" source="main.colorOut"/>
                <texture name="depthOut" type="depth" format="DEPTH32F" source="main.depthOut"/>
            </outputs>
        </render-pass>



        <render-pass name="post" parent="vke:post">
            <inputs>
                <input name="colorIn" source="debug.colorOut"/>
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