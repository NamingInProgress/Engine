<r>
    <stage-filter uri="file://extension" query="wav" op="equals">
        <stage-filter uri="file://location/parent" query="preload" op="equals" else="wav_not_preload">
            <decode to="wav" using="wav"/>
            <convert to="audio_pre"/>
        </stage-filter>
        <filter-else tag="wav_not_preload">
            <log level="error">
                <uri-part uri="file://location/bundle"/>
                <static-part value=": WAV files can only be preloaded as of right now!"/>
            </log>
        </filter-else>
    </stage-filter>
</r>