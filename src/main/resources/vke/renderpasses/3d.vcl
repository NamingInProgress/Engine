<render-graph name="StandardDeferred">
    <!-- Pass 1: Draw the 3D World -->
    <render-pass name="my3DWorld">
        <class name="com.vke.api.rendering.abstraction.rendergraph.RenderGraphDefinition"/>
        <!-- Declare what this pass outputs so others can use it -->
        <outputs>
            <texture name="colorOut" />
            <texture name="depthOut" type="depth" />
        </outputs>
        <uniforms>
            <uniform path="buffer.matrix" field="someMatrix"/>
        </uniforms>
    </render-pass>

    <!-- Pass 2: Post Processing -->
    <render-pass name="post" parent="post">
        <!-- Explicitly request the output from my3DWorld -->
        <inputs>
            <texture name="mainColor" source="my3DWorld.colorOut" uniform-field-name="u_mainTex" />
        </inputs>
        <outputs>
            <texture name="postOut" />
        </outputs>
        <stages>
            <ssao/>
            <blur/>
        </stages>
    </render-pass>

    <!-- Pass 3: UI -->
    <render-pass name="ui">
        <class name="com.vke.api.rendering.abstraction.rendergraph.RenderGraphDefinition"/>
        <inputs>
            <texture name="background" source="post.postOut" />
        </inputs>
        <!-- Outputs directly to the swapchain/screen -->
        <outputs>
            <render-target/>
        </outputs>
    </render-pass>
</render-graph>