# Netpick.MailMine — Accurate Implementation Plan

> Grounded in actual codebase state as of 2026-07-14.

---

## Current State Summary

| Layer | What Exists | What's Missing |
|-------|------------|----------------|
| **Backend Controllers** | 5 stub controllers (Deal, File, Finance, Inventory, Task) — all return `List.of()` / `null` | Service injection, real logic |
| **Backend Entities** | 0 farm entities | All entity classes |
| **Backend Repositories** | 0 farm repositories | All repo interfaces |
| **Backend Services** | 0 farm services | All service interfaces + impls |
| **Backend DTOs** | 5 DTO records already defined | Request/response DTOs for complex ops |
| **Backend Constants** | 5 constants classes already defined | — |
| **Frontend Pages** | 5 placeholder `page.tsx` (heading only) | Layout, real pages, sub-routes |
| **Frontend Services** | 0 farm API services | All service classes |
| **Frontend Hooks** | 0 farm hooks | All hooks |
| **Frontend Components** | 0 farm components | All components |
| **Sidebar Nav** | No farm entries in `AppSidebar.tsx` | Nav groups for each module |
| **Route Layout** | Farm pages in `(farm)` groups — **outside admin layout** (no sidebar/header) | Move into `(admin)` or add layout |

### Existing Patterns to Follow

| Pattern | Reference File | Convention |
|---------|---------------|------------|
| Entity base | `core/BaseEntity.java` | `@MappedSuperclass`, UUID id, `createdAt`, `updatedAt`, `deleted` (soft delete) |
| Repository | `mailmine/repository/ContactRepository.java` | `extends JpaRepository`, `findByDeletedFalse()` for soft-delete |
| Service | `mailmine/service/base/ContactService.java` | `@Service`, `@RequiredArgsConstructor`, uses `PageDTOMapper.map()` |
| Controller | `mailmine/controller/ContactController.java` | `@RestController`, `@PreAuthorize`, injects service |
| Frontend Service | `services/contactService.ts` | Class with `basePath`, uses `api.get/post/put/delete` |
| Frontend Hook | `hooks/useContacts.ts` | `useState` + `useCallback` + `useEffect`, returns `{ items, loading, error, refetch }` |
| Frontend Page | `app/(admin)/email/page.tsx` | Under `(admin)` layout, uses sidebar |

---

## Phase 0: Fix Route Architecture (PREREQUISITE)

**Problem:** Farm pages sit in standalone route groups `(dealfarm)`, `(taskfarm)`, etc. with **no layout** — no sidebar, no header, no auth protection. They're invisible to users.

**Fix: Move all farm pages into `(admin)` route group.**

### Files to Change

1. **Move pages:**
   - `app/(dealfarm)/page.tsx` → `app/(admin)/(dealfarm)/page.tsx`
   - `app/(filefarm)/page.tsx` → `app/(admin)/(filefarm)/page.tsx`
   - `app/(financefarm)/page.tsx` → `app/(admin)/(financefarm)/page.tsx`
   - `app/(inventoryfarm)/page.tsx` → `app/(admin)/(inventoryfarm)/page.tsx`
   - `app/(taskfarm)/page.tsx` → `app/(admin)/(taskfarm)/page.tsx`

2. **Add sidebar nav entries** in `AppSidebar.tsx`:
   ```typescript
   {
     icon: <DealIcon />,  // or reuse PageIcon
     name: "DealPick",
     subItems: [
       { name: "Deals", path: "/dealfarm", pro: false },
     ],
   },
   {
     icon: <FileIcon />,
     name: "FilePick",
     subItems: [
       { name: "Files", path: "/filefarm", pro: false },
     ],
   },
   {
     icon: <FinanceIcon />,
     name: "FinancePick",
     subItems: [
       { name: "Invoices", path: "/financefarm", pro: false },
     ],
   },
   {
     icon: <InventoryIcon />,
     name: "InventoryPick",
     subItems: [
       { name: "Inventory", path: "/inventoryfarm", pro: false },
     ],
   },
   {
     icon: <TaskIcon />,
     name: "TaskPick",
     subItems: [
       { name: "Tasks", path: "/taskfarm", pro: false },
       { name: "Projects", path: "/taskfarm/projects", pro: false },
     ],
   },
   ```

---

## Phase 1: DealFarm (CRM & Sales Pipeline)

**Why first:** Core business value — CRM is the most universally needed module.

### Backend — Entity, Repository, Service, Controller

| # | File | Action | Details |
|---|------|--------|---------|
| 1.1 | `dealfarm/model/Deal.java` | **Create** | Extends `BaseEntity`. Fields: `title`, `description`, `stage` (enum), `value` (BigDecimal), `currency`, `contactId` (UUID), `ownerId` (UUID), `closedAt`, `probability` (Integer), `expectedCloseDate` (LocalDateTime) |
| 1.2 | `dealfarm/model/DealStage.java` | **Create** | Enum: `PROSPECTING`, `QUALIFICATION`, `PROPOSAL`, `NEGOTIATION`, `CLOSED_WON`, `CLOSED_LOST` |
| 1.3 | `dealfarm/repository/DealRepository.java` | **Create** | `extends JpaRepository<Deal, UUID>`. Methods: `findByDeletedFalse(Pageable)`, `findByStage(DealStage, Pageable)`, `findByOwnerId(UUID, Pageable)` |
| 1.4 | `dealfarm/service/DealService.java` | **Create** | Following `ContactService` pattern. Methods: `getAll(page)`, `getById(id)`, `create(dto)`, `update(id, dto)`, `delete(id)`, `getByStage(stage, page)`, `getStats()` |
| 1.5 | `dealfarm/controller/DealController.java` | **Edit** | Wire `DealService`, replace stubs with real calls. Follow `ContactController` pattern with `@PreAuthorize` |
| 1.6 | `dealfarm/dto/DealDTO.java` | **Edit** | Add `probability`, `expectedCloseDate` fields |
| 1.7 | `dealfarm/dto/DealStatsDTO.java` | **Create** | `totalDeals`, `totalValue`, `winRate`, `dealsByStage` map |

### Frontend — Service, Hook, Components, Page

| # | File | Action | Details |
|---|------|--------|---------|
| 1.8 | `services/dealService.ts` | **Create** | Following `contactService.ts` pattern. `basePath = '/api/v1/dealfarm/deals'` |
| 1.9 | `hooks/useDeals.ts` | **Create** | Following `useContacts.ts` pattern. `useDeals(page)`, `useDeal(id)`, `useDealStats()` |
| 1.10 | `hooks/index.ts` | **Edit** | Add `export * from './useDeals'` |
| 1.11 | `components/deals/DealList.tsx` | **Create** | Table view with stage badges, sort/filter, pagination |
| 1.12 | `components/deals/DealForm.tsx` | **Create** | Create/edit form: title, stage dropdown, value input, contact picker |
| 1.13 | `components/deals/DealCard.tsx` | **Create** | Kanban card (stage badge, value, owner) |
| 1.14 | `components/deals/KanbanBoard.tsx` | **Create** | Columns per stage, drag-drop with `react-dnd` (already in package.json) |
| 1.15 | `app/(admin)/(dealfarm)/page.tsx` | **Edit** | Replace placeholder with KanbanBoard + DealList toggle |

### Validation Criteria
- [ ] `GET /api/v1/dealfarm/deals` returns paginated deals
- [ ] `POST /api/v1/dealfarm/deals` creates deal with stage
- [ ] Kanban board shows deals in stage columns
- [ ] Deal form validates required fields
- [ ] Sidebar shows "DealPick" with "Deals" link

---

## Phase 2: TaskFarm (Tasks & Projects)

**Why second:** Highest daily-use potential — every user interacts with tasks daily.

### Backend — Entity, Repository, Service, Controller

| # | File | Action | Details |
|---|------|--------|---------|
| 2.1 | `taskfarm/model/Task.java` | **Create** | Extends `BaseEntity`. Fields: `title`, `description`, `status` (enum), `priority` (enum), `projectId` (UUID), `assigneeId` (UUID), `creatorId` (UUID), `dueDate`, `completedAt`, `order` (Integer for position) |
| 2.2 | `taskfarm/model/TaskStatus.java` | **Create** | Enum: `TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE` |
| 2.3 | `taskfarm/model/TaskPriority.java` | **Create** | Enum: `LOW`, `MEDIUM`, `HIGH`, `URGENT` |
| 2.4 | `taskfarm/model/Project.java` | **Create** | Extends `BaseEntity`. Fields: `name`, `description`, `ownerId`, `status` |
| 2.5 | `taskfarm/model/Board.java` | **Create** | Extends `BaseEntity`. Fields: `name`, `projectId`, `columns` (JSON or relation) |
| 2.6 | `taskfarm/repository/TaskRepository.java` | **Create** | `findByDeletedFalse(Pageable)`, `findByStatus(TaskStatus, Pageable)`, `findByAssigneeId(UUID, Pageable)`, `findByProjectId(UUID, Pageable)`, `findByDueDateBetween(start, end, Pageable)` |
| 2.7 | `taskfarm/repository/ProjectRepository.java` | **Create** | Standard CRUD + `findByOwnerId(UUID, Pageable)` |
| 2.8 | `taskfarm/service/TaskService.java` | **Create** | CRUD + status transitions + `reorder(taskId, newOrder)` |
| 2.9 | `taskfarm/service/ProjectService.java` | **Create** | CRUD + task count per project |
| 2.10 | `taskfarm/controller/TaskController.java` | **Edit** | Wire `TaskService`, add status filter, project filter |
| 2.11 | `taskfarm/controller/ProjectController.java` | **Create** | CRUD for projects |
| 2.12 | `taskfarm/dto/ProjectDTO.java` | **Create** | Record for project data |

### Frontend — Service, Hook, Components, Pages

| # | File | Action | Details |
|---|------|--------|---------|
| 2.13 | `services/taskService.ts` | **Create** | `basePath = '/api/v1/taskfarm/tasks'` |
| 2.14 | `services/projectService.ts` | **Create** | `basePath = '/api/v1/taskfarm/projects'` |
| 2.15 | `hooks/useTasks.ts` | **Create** | `useTasks(page, filters)`, `useTask(id)` |
| 2.16 | `hooks/useProjects.ts` | **Create** | `useProjects(page)` |
| 2.17 | `hooks/index.ts` | **Edit** | Add exports |
| 2.18 | `components/tasks/TaskBoard.tsx` | **Create** | Trello-style board with columns, `react-dnd` drag-drop |
| 2.19 | `components/tasks/TaskCard.tsx` | **Create** | Card with title, priority badge, assignee avatar, due date |
| 2.20 | `components/tasks/TaskList.tsx` | **Create** | Table view with filters |
| 2.21 | `components/tasks/TaskForm.tsx` | **Create** | Create/edit form |
| 2.22 | `components/tasks/TaskDetailSidebar.tsx` | **Create** | Slide-over panel with comments, activity |
| 2.23 | `app/(admin)/(taskfarm)/page.tsx` | **Edit** | Board view (default) |
| 2.24 | `app/(admin)/(taskfarm)/list/page.tsx` | **Create** | Table view |

### Validation Criteria
- [ ] Board view shows tasks in status columns
- [ ] Drag-drop moves task between columns
- [ ] Task form validates required fields
- [ ] Tasks filterable by assignee, priority, due date
- [ ] Sidebar shows "TaskPick" with "Tasks" + "Projects" links

---

## Phase 3: FileFarm (Documents & File Management)

### Backend

| # | File | Action | Details |
|---|------|--------|---------|
| 3.1 | `filefarm/model/FileEntity.java` | **Create** | Extends `BaseEntity`. Fields: `fileName`, `originalFileName`, `mimeType`, `fileSize` (Long), `filePath`, `folderId` (UUID), `ownerId` (UUID). **Note:** Use `FileEntity` not `File` to avoid conflict with `java.io.File` |
| 3.2 | `filefarm/model/Folder.java` | **Create** | Extends `BaseEntity`. Fields: `name`, `parentId` (UUID), `ownerId` (UUID), `path` (String for breadcrumb) |
| 3.3 | `filefarm/repository/FileRepository.java` | **Create** | `findByFolderId(UUID, Pageable)`, `findByOwnerId(UUID, Pageable)`, `findByOriginalFileNameContaining(String, Pageable)` |
| 3.4 | `filefarm/repository/FolderRepository.java` | **Create** | `findByParentId(UUID, Pageable)`, `findByOwnerId(UUID)` |
| 3.5 | `filefarm/service/FileService.java` | **Create** | Upload with `MultipartFile`, metadata extraction, storage (local for dev), soft delete |
| 3.6 | `filefarm/service/FolderService.java` | **Create** | CRUD + hierarchy management |
| 3.7 | `filefarm/controller/FileController.java` | **Edit** | Wire service, handle `MultipartFile` uploads, replace stubs |
| 3.8 | `filefarm/controller/FolderController.java` | **Create** | CRUD for folders |
| 3.9 | `filefarm/dto/FolderDTO.java` | **Create** | Record for folder data |

### Frontend

| # | File | Action | Details |
|---|------|--------|---------|
| 3.10 | `services/fileService.ts` | **Create** | Upload with `FormData`, download, list |
| 3.11 | `services/folderService.ts` | **Create** | CRUD + hierarchy |
| 3.12 | `hooks/useFiles.ts` | **Create** | `useFiles(folderId, page)`, `useFile(id)` |
| 3.13 | `hooks/useFolders.ts` | **Create** | `useFolders(parentId)` |
| 3.14 | `components/files/FileExplorer.tsx` | **Create** | Tree view + breadcrumb nav |
| 3.15 | `components/files/FileGrid.tsx` | **Create** | Grid/list toggle, file icons by mime type |
| 3.16 | `components/files/FileUpload.tsx` | **Create** | Drag-drop zone, progress indicator, bulk upload |
| 3.17 | `app/(admin)/(filefarm)/page.tsx` | **Edit** | FileExplorer + FileGrid |

### Validation Criteria
- [ ] File upload creates record + stores file locally
- [ ] Folder hierarchy works (navigate in/out)
- [ ] File grid shows icons by type
- [ ] Drag-drop upload works
- [ ] Sidebar shows "FilePick" with "Files" link

---

## Phase 4: FinanceFarm (Accounting & Invoicing)

### Backend

| # | File | Action | Details |
|---|------|--------|---------|
| 4.1 | `financefarm/model/Invoice.java` | **Create** | Extends `BaseEntity`. Fields: `invoiceNumber`, `customerName`, `totalAmount` (BigDecimal), `taxAmount` (BigDecimal), `currency`, `status` (enum), `issueDate`, `dueDate`, `paidAt`, `createdBy` |
| 4.2 | `financefarm/model/InvoiceStatus.java` | **Create** | Enum: `DRAFT`, `SENT`, `PAID`, `OVERDUE`, `CANCELLED` |
| 4.3 | `financefarm/model/InvoiceLineItem.java` | **Create** | Extends `BaseEntity`. Fields: `invoiceId`, `description`, `quantity`, `unitPrice`, `total` |
| 4.4 | `financefarm/model/Transaction.java` | **Create** | Extends `BaseEntity`. Fields: `amount`, `type` (INCOME/EXPENSE), `category`, `description`, `date`, `invoiceId` (optional) |
| 4.5 | `financefarm/repository/InvoiceRepository.java` | **Create** | `findByStatus(InvoiceStatus, Pageable)`, `findByCreatedBy(UUID, Pageable)` |
| 4.6 | `financefarm/repository/TransactionRepository.java` | **Create** | `findByDateBetween(start, end, Pageable)`, `findByType(TransactionType, Pageable)` |
| 4.7 | `financefarm/service/InvoiceService.java` | **Create** | CRUD + status transitions + line items |
| 4.8 | `financefarm/service/TransactionService.java` | **Create** | CRUD + CSV import + summary queries |
| 4.9 | `financefarm/controller/InvoiceController.java` | **Edit** | Wire service |
| 4.10 | `financefarm/controller/TransactionController.java` | **Create** | CRUD + import endpoint |
| 4.11 | `financefarm/dto/TransactionDTO.java` | **Create** | Record |
| 4.12 | `financefarm/dto/InvoiceLineItemDTO.java` | **Create** | Record |
| 4.13 | `financefarm/dto/FinanceSummaryDTO.java` | **Create** | Revenue, expenses, profit |

### Frontend

| # | File | Action | Details |
|---|------|--------|---------|
| 4.14 | `services/invoiceService.ts` | **Create** | CRUD + status updates |
| 4.15 | `services/transactionService.ts` | **Create** | CRUD + CSV import |
| 4.16 | `hooks/useInvoices.ts` | **Create** | `useInvoices(page, status)`, `useInvoice(id)` |
| 4.17 | `hooks/useTransactions.ts` | **Create** | `useTransactions(page, dateRange)` |
| 4.18 | `components/finance/InvoiceList.tsx` | **Create** | Table with status badges |
| 4.19 | `components/finance/InvoiceForm.tsx` | **Create** | Line items editor |
| 4.20 | `components/finance/FinanceDashboard.tsx` | **Create** | Revenue/expense charts (ApexCharts already in deps) |
| 4.21 | `components/finance/TransactionList.tsx` | **Create** | Table with import button |
| 4.22 | `app/(admin)/(financefarm)/page.tsx` | **Edit** | FinanceDashboard + InvoiceList |

---

## Phase 5: InventoryFarm (Stock & Warehouse)

### Backend

| # | File | Action | Details |
|---|------|--------|---------|
| 5.1 | `inventoryfarm/model/Product.java` | **Create** | Extends `BaseEntity`. Fields: `productName`, `sku`, `quantity`, `minQuantity`, `unitPrice` (BigDecimal), `currency`, `warehouseId`, `categoryId`, `lastStockUpdate` |
| 5.2 | `inventoryfarm/model/Warehouse.java` | **Create** | Extends `BaseEntity`. Fields: `name`, `location`, `ownerId` |
| 5.3 | `inventoryfarm/model/Category.java` | **Create** | Extends `BaseEntity`. Fields: `name`, `parentId` |
| 5.4 | `inventoryfarm/model/StockMovement.java` | **Create** | Extends `BaseEntity`. Fields: `productId`, `quantity`, `type` (IN/OUT/ADJUSTMENT), `reason`, `movedBy` |
| 5.5 | `inventoryfarm/repository/ProductRepository.java` | **Create** | `findByWarehouseId(UUID, Pageable)`, `findBySku(String)`, `findByQuantityLessThanEqual(int, Pageable)` (low stock) |
| 5.6 | `inventoryfarm/repository/StockMovementRepository.java` | **Create** | `findByProductId(UUID, Pageable)`, `findByDateBetween(start, end)` |
| 5.7 | `inventoryfarm/service/ProductService.java` | **Create** | CRUD + stock adjustment + low stock alerts |
| 5.8 | `inventoryfarm/service/StockMovementService.java` | **Create** | Record movements, history |
| 5.9 | `inventoryfarm/controller/ProductController.java` | **Edit** | Wire service |
| 5.10 | `inventoryfarm/controller/StockMovementController.java` | **Create** | Movements + history |
| 5.11 | `inventoryfarm/dto/ProductDTO.java` | **Create** | Record (update existing `InventoryDTO` → `ProductDTO`) |
| 5.12 | `inventoryfarm/dto/StockMovementDTO.java` | **Create** | Record |

### Frontend

| # | File | Action | Details |
|---|------|--------|---------|
| 5.13 | `services/productService.ts` | **Create** | CRUD + stock ops |
| 5.14 | `hooks/useProducts.ts` | **Create** | `useProducts(page, warehouse)`, `useLowStock()` |
| 5.15 | `components/inventory/ProductList.tsx` | **Create** | Table with stock level badges |
| 5.16 | `components/inventory/StockAdjustment.tsx` | **Create** | In/out form with reason |
| 5.17 | `components/inventory/StockMovementHistory.tsx` | **Create** | Table of movements |
| 5.18 | `app/(admin)/(inventoryfarm)/page.tsx` | **Edit** | ProductList + low stock alerts |

---

## Phase 6: Cross-Module Infrastructure

### 6.1 Notification System
| File | Action | Details |
|------|--------|---------|
| Backend: Extend `NotificationService` | Edit | Add farm event hooks (deal created, task assigned, low stock) |
| Frontend: `components/notifications/NotificationCenter.tsx` | Create | Bell icon dropdown in header |
| Frontend: `hooks/useNotifications.ts` | Create | Poll or SSE for new notifications |

### 6.2 Audit Trail for Farm Modules
| File | Action | Details |
|------|--------|---------|
| Backend: Extend `AuditTrailService` | Edit | Add farm entity change tracking |
| Frontend: `components/common/ActivityTimeline.tsx` | Create | Reusable timeline component |

### 6.3 Global Search Integration
| File | Action | Details |
|------|--------|---------|
| Backend: Extend `GlobalSearchController` | Edit | Add farm entity search results |
| Frontend: Extend search component | Edit | Show farm results in global search |

---

## File Count Summary

| Module | Backend Files | Frontend Files | Total |
|--------|--------------|----------------|-------|
| Phase 0 (Route Fix) | 0 | 7 (move + sidebar) | 7 |
| Phase 1 (DealFarm) | 7 | 8 | 15 |
| Phase 2 (TaskFarm) | 11 | 12 | 23 |
| Phase 3 (FileFarm) | 9 | 8 | 17 |
| Phase 4 (FinanceFarm) | 13 | 9 | 22 |
| Phase 5 (InventoryFarm) | 12 | 6 | 18 |
| Phase 6 (Cross-Module) | 3 | 3 | 6 |
| **Total** | **55** | **53** | **108** |

---

## Verification Checklist

- [ ] Farm pages accessible via sidebar navigation
- [ ] All CRUD operations work end-to-end per module
- [ ] Soft-delete pattern consistent across all modules
- [ ] Pagination works on all list views
- [ ] Forms validate required fields client-side
- [ ] `docker compose build` succeeds
- [ ] No compile errors in backend or frontend
- [ ] Each module follows existing codebase conventions
