<r>
    <stage-filter uri="file://extension" query="obj" op="equals">
        <cache-asset for="meshprefab">
            <decode to="obj" using="obj"/>
            <convert to="meshprefab"/>
        </cache-asset>
        <rename>
            <uri-part uri="file://location/bundle"/>
        </rename>
    </stage-filter>
</r>