<render-graph>
    <render-pass name="main" parent="deferred">
        <outputs>
            <texture name="colorOut"/>
            <texture name="depthOut" type="DEPTH" format="DEPTH32F"/>
        </outputs>
    </render-pass>

    <render-pass name="debug" parent="debug">
        <outputs>
            <texture name="colorOut" source="main.colorOut"/>
            <texture name="depthOut" type="DEPTH" format="DEPTH32F" source="main.depthOut"/>
        </outputs>
    </render-pass>

<!--    <image-to-screen source="debug.colorOut"/>-->
<!---->
    <render-pass name="post" parent="post">
        <inputs>
            <input name="colorIn" source="debug.colorOut"/>
        </inputs>
        <outputs>
            <texture name="colorOut"/>
        </outputs>
        <stages>
            <stage name="bloom"/>
        </stages>
    </render-pass>
<!---->
    <image-to-screen source="post.colorOut"/>
</render-graph>