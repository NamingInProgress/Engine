<render-graph>
    <render-pass name="main">
        <class name="com.vke.demo.DemoRenderPass"/>
        <outputs>
            <texture name="colorOut"/>
            <texture name="depthOut" type="depth" format="DEPTH32F"/>
        </outputs>
    </render-pass>

    <render-pass name="post" parent="post">
        <inputs>
            <texture name="input" source="main.colorOut"/>
        </inputs>
        <outputs>
            <render-target name="output"/>
        </outputs>
        <stages>
            <stage name="vke:blur"/>
            <stage name="vke:invert_colors"/>
            <stage name="vke:idk_something" />
        </stages>
    </render-pass>
</render-graph>