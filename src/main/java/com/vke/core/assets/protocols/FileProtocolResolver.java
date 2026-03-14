package com.vke.core.assets.protocols;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.StageFilter;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.utils.FileUtils;
import com.vke.utils.Infallible;

import java.net.URI;
import java.nio.file.Path;

public class FileProtocolResolver implements ProtocolResolver<Infallible> {
    @Override
    public boolean checkProtocolContent(StageFilter filter, StageElement stageElement) throws AssetPipelineException {
        String selector = filter.getSelector();
        return switch (selector) {
            case "extension" -> checkExtension(filter, stageElement);
            case "name" -> checkName(filter, stageElement);
            case "nickname" -> checkNickname(filter, stageElement);
            case "location" -> checkLocation(filter, stageElement);
            default -> throw AssetPipelineException.unknownSelector("file", selector);
        };
    }

    @Override
    public String resolveUri(URI uri, StageElement stageElement) throws AssetPipelineException {
        String selector = uri.getAuthority();
        return switch (selector) {
            case "extension" -> FileUtils.getExtension(stageElement.getPath());
            case "name" -> FileUtils.getFileName(stageElement.getPath());
            case "nickname" -> FileUtils.getFileNickname(stageElement.getPath());
            case "location" -> throw new AssetPipelineException("Contact the dev team if you want this feature, i cant be asked rn.");
            default -> throw AssetPipelineException.unknownSelector("file", selector);
        };
    }

    @Override
    public AssetHandle<?> createHandle(StageElement element) throws AssetPipelineException {
        throw new AssetPipelineException("Unable to turn file:// into a handle!");
    }

    @Override
    public Infallible resolveData(StageElement element) throws AssetPipelineException {
        throw new AssetPipelineException("Unable to turn file:// into data!");
    }

    private boolean checkName(StageFilter filter, StageElement element) {
        String filename = FileUtils.getFileName(element.getPath());
        return filter.applyForString(filename);
    }

    private boolean checkNickname(StageFilter filter, StageElement element) {
        String nickname = FileUtils.getFileNickname(element.getPath());
        return filter.applyForString(nickname);
    }

    private boolean checkExtension(StageFilter filter, StageElement element) {
        String extension = FileUtils.getExtension(element.getPath());
        return filter.applyForString(extension);
    }

    private boolean checkLocation(StageFilter filter, StageElement element) throws AssetPipelineException {
        Path path = element.getPath().normalize();

        String subSelector = filter.getPath();

        if (subSelector == null || subSelector.isBlank()) {
            Path parent = path.getParent();
            if (parent == null) return false;
            return filter.applyForPathString(filter, parent);
        }

        return switch (subSelector) {
            case "parent" -> {
                Path parent = path.getParent();
                if (parent == null) yield false;
                Path directParent = parent.getFileName();
                yield directParent != null && filter.applyForString(directParent.toString());
            }

            case "namespace" -> {
                if (path.getNameCount() < 1) yield false;
                yield filter.applyForString(path.getName(0).toString());
            }

            default -> throw AssetPipelineException.unknownSelector("file://location", subSelector);
        };
    }
}
