<r>
    <stage-filter uri="file://extension" query="json" op="equals">
        <convert to="config"/>
        <stage-filter uri="file://location/parent" query="language" op="equals">
            <convert to="lang"/>
            <rename>
                <static-part value="language."/>
                <uri-part uri="file://nickname"/>
            </rename>
        </stage-filter>

        <!-- =================== PIPELINES =================== -->
        <stage-filter uri="file://name" query="compute_pipeline" op="contains">
            <convert to="config"/>
            <stage-filter uri="config://field/hello" query="world" op="equals"/>
            <convert to="compute_pipeline"/>
        </stage-filter>
        <stage-filter uri="file://name" query="pipeline" op="contains">
            <convert to="config"/>
            <stage-filter uri="config://field/hello" query="world" op="equals"/>
            <convert to="pipeline"/>
        </stage-filter>
    </stage-filter>
</r>