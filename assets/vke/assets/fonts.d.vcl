<r>
    <stage-filter uri="file://extension" query="vkz" op="equals">
        <stage-filter uri="file://location/parent" query="fonts" op="equals">
            <convert to="vkz"/>
            <convert to="font"/>
            <rename>
                <static-part>fonts/</static-part>
                <uri-part uri="file://nickname"/>
            </rename>
        </stage-filter>
    </stage-filter>
</r>