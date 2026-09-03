<r>
    <stage-filter uri="file://extension" query="png" op="equals">
        <decode to="png" using="png"/>
        <convert to="texture"/>
        <rename>
            <uri-part uri="file://location/bundle"/>
        </rename>
    </stage-filter>
</r>