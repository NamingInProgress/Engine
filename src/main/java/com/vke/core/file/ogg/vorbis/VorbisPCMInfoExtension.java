package com.vke.core.file.ogg.vorbis;

import com.vke.core.file.ogg.vorbis.setup.SetupHeader;

public record VorbisPCMInfoExtension(IdentHeader identHeader, CommentHeader commentHeader, SetupHeader setupHeader) {
}
