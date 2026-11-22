<r>
    <stage-filter uri="file://extension" query="png" op="equals">
        <decode to="png" using="png"/>
        <convert to="bitmap"/>
        <convert to="texture"/>
        <rename>
            <uri-part uri="file://location/bundle"/>
        </rename>
    </stage-filter>
    <stage-filter uri="file://extension" query="exr" op="equals">
        <decode to="bitmap" using="exr-bmp"/>
        <log>
            <static-part>Format for '</static-part>
            <uri-part value="file://location/bundle"/>
            <static-part value="': "/>
            <uri-part value="bitmap://format"/>
        </log>
        <convert to="texture"/>
        <rename>
            <uri-part uri="file://location/bundle"/>
        </rename>
    </stage-filter>
</r>