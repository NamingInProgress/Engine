<render-graph>
    <render-pass name="main" parent="deferred">
        <outputs>
            <render-target name="colorOut"/>
            <texture name="depthOut" type="DEPTH" format="DEPTH32F"/>
        </outputs>
    </render-pass>

<!--    <render-pass name="post" parent="post">-->
<!--        <inputs>-->
<!--            <texture name="input" source="main.colorOut"/>-->
<!--        </inputs>-->
<!--        <outputs>-->
<!--            <render-target name="output"/>-->
<!--        </outputs>-->
<!--        <stages>-->
<!--            <stage name="vke:blur"/>-->
<!--            <stage name="vke:invert_colors"/>-->
<!--            <stage name="vke:idk_something" />-->
<!--        </stages>-->
<!--    </render-pass>-->
    <!--            <texture name="colorOut"/>-->
<!--    <render-pass name="pbr" parent="pbr">-->
<!--        <pipeline name="vke:basic_pbr.pipeline.json"/>-->
<!--    </render-pass>-->
</render-graph>