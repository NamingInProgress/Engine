<lolISpendTimeSoYouCanNameThisRootTagWHATEVEYouWantImSoProud>
    <stage-filter uri="file://name" query="^.*\.schema\.json$" op="matches">
        <convert to="config"/>
        <convert to="schema"/>
        <log level="warn">
            <static-part value="Found schema: "/>
            <uri-part uri="file://location/bundle"/>
            <static-part value=" -> Maybe update to vks soon?"/>
        </log>
    </stage-filter>
    <stage-filter uri="file://extension" query="vks" op="equals">
        <convert to="config"/>
        <convert to="schema"/>
        <log>
            <static-part value="Found fancy vks schema: "/>
            <uri-part uri="file://location/bundle"/>
        </log>
    </stage-filter>
</lolISpendTimeSoYouCanNameThisRootTagWHATEVEYouWantImSoProud>