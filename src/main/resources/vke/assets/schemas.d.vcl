<lolISpendTimeSoYouCanNameThisRootTagWHATEVEYouWantImSoProud>
    <stage-filter uri="file://name" query="^.*\.schema\.json$" op="matches">
        <convert to="config"/>
        <convert to="schema"/>
        <log>
            <static-part value="schema found: "/>
            <uri-part uri="file://location/bundle"/>
        </log>
    </stage-filter>
    <stage-filter uri="file://extension" query="vks" op="equals">
        <convert to="config"/>
        <convert to="schema"/>
        <log>
            <static-part value="schema found: "/>
            <uri-part uri="file://location/bundle"/>
        </log>
    </stage-filter>
</lolISpendTimeSoYouCanNameThisRootTagWHATEVEYouWantImSoProud>