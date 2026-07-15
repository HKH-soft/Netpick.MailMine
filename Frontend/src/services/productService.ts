// productService.ts
import api, { PageDTO } from "./api";

export interface Product {
  id: string;
  productName: string;
  sku: string;
  quantity: number;
  minQuantity: number;
  unitPrice: number;
  currency: string;
  warehouseId: string | null;
  categoryId: string | null;
  lastStockUpdate: string;
  createdAt: string;
  updatedAt: string;
}

export interface StockMovement {
  id: string;
  productId: string;
  quantity: number;
  type: StockMovementType;
  reason: string;
  movedBy: string;
  movementDate: string;
  createdAt: string;
  updatedAt: string;
}

export enum StockMovementType {
  IN = "IN",
  OUT = "OUT",
  ADJUSTMENT = "ADJUSTMENT",
}

const basePath = "/api/v1/inventoryfarm/products";
const movementPath = "/api/v1/inventoryfarm/stock-movements";

const ProductService = {
  getAllProducts: (page: number = 1): Promise<PageDTO<Product>> =>
    api.get(`${basePath}?page=${page}`),
  getProductById: (id: string): Promise<Product> => api.get(`${basePath}/${id}`),
  getProductBySku: (sku: string): Promise<Product> => api.get(`${basePath}/sku/${sku}`),
  getLowStockProducts: (): Promise<Product[]> => api.get(`${basePath}/low-stock`),
  createProduct: (data: Partial<Product>): Promise<Product> => api.post(basePath, data),
  updateProduct: (id: string, data: Partial<Product>): Promise<Product> =>
    api.put(`${basePath}/${id}`, data),
  deleteProduct: (id: string): Promise<void> => api.delete(`${basePath}/${id}`),
  adjustStock: (id: string, quantity: number, type: StockMovementType, reason?: string): Promise<Product> =>
    api.put(`${basePath}/${id}/stock?quantity=${quantity}&type=${type}${reason ? `&reason=${reason}` : ""}`, {}),
};

const StockMovementService = {
  getAllMovements: (page: number = 1): Promise<PageDTO<StockMovement>> =>
    api.get(`${movementPath}?page=${page}`),
  getMovementsByProduct: (productId: string, page: number = 1): Promise<PageDTO<StockMovement>> =>
    api.get(`${movementPath}/product/${productId}?page=${page}`),
  createMovement: (data: Partial<StockMovement>): Promise<StockMovement> => api.post(movementPath, data),
  deleteMovement: (id: string): Promise<void> => api.delete(`${movementPath}/${id}`),
};

export { StockMovementService };
export default ProductService;