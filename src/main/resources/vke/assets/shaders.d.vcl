<r>
    <stage-filter uri="file://extension" query="frag|fsh" op="matches">
        <convert to="fragshader"/>
        <rename>
            <uri-part uri="file://location/bundle"/>
        </rename>
    </stage-filter>
    <stage-filter uri="file://extension" query="vert|vsh" op="matches">
        <convert to="vertshader"/>
        <rename>
            <uri-part uri="file://location/bundle"/>
        </rename>
    </stage-filter>
    <stage-filter uri="file://extension" query="comp|csh" op="matches">
        <convert to="compshader"/>
        <rename>
            <uri-part uri="file://location/bundle"/>
        </rename>
    </stage-filter>
    <stage-filter uri="file://extension" query="glsl" op="matches">
        <convert to="vertshader"/>
        <rename>
            <uri-part uri="file://location/bundle"/>
        </rename>
    </stage-filter>
</r>