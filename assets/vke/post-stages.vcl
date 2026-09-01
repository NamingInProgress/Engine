<stages>
    <simple-stage>
        <name>blur</name>
        <pipeline>blur.pipeline.json</pipeline>
        <uniforms>
            <uniform name="radius" type="float" default="1.0"/>
        </uniforms>
    </simple-stage>
    <simple-stage>
        <name>invert_colors</name>
        <pipeline>invert_colors.pipeline.json</pipeline>
    </simple-stage>
    <simple-stage>
        <name>idk_something</name>
        <pipeline>idk_something.pipeline.json</pipeline>
    </simple-stage>
</stages>