package ir.netpick.platform.filefarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.filefarm.model.Folder;
import ir.netpick.platform.filefarm.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;

    public PageDTO<Folder> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Folder> page = folderRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public List<Folder> getByOwner(UUID ownerId) {
        return folderRepository.findByOwnerIdAndDeletedFalse(ownerId);
    }

    public PageDTO<Folder> getByParent(UUID parentId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Folder> page = folderRepository.findByParentIdAndDeletedFalse(parentId, pageable);
        return PageDTOMapper.map(page);
    }

    public Folder getById(UUID folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder with id [%s] was not found".formatted(folderId)));
    }

    public Folder create(Folder folder) {
        folder.setId(null);
        return folderRepository.save(folder);
    }

    public Folder update(UUID folderId, Folder folder) {
        Folder existing = getById(folderId);
        folder.setId(folderId);
        folder.setCreatedAt(existing.getCreatedAt());
        return folderRepository.save(folder);
    }

    public void delete(UUID folderId) {
        folderRepository.softDelete(folderId);
    }

    public void restore(UUID folderId) {
        folderRepository.restore(folderId);
    }
}