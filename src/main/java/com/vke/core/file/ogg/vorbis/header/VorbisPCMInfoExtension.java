package com.vke.core.file.ogg.vorbis.header;

import com.vke.core.file.ogg.vorbis.header.setup.SetupHeader;

public record VorbisPCMInfoExtension(IdentHeader identHeader, CommentHeader commentHeader, SetupHeader setupHeader) {
}
