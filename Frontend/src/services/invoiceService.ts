// invoiceService.ts
import api, { PageDTO } from "./api";

export interface Invoice {
  id: string;
  invoiceNumber: string;
  customerName: string;
  totalAmount: number;
  taxAmount: number;
  currency: string;
  status: InvoiceStatus;
  issueDate: string;
  dueDate: string;
  paidAt: string | null;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export enum InvoiceStatus {
  DRAFT = "DRAFT",
  SENT = "SENT",
  PAID = "PAID",
  OVERDUE = "OVERDUE",
  CANCELLED = "CANCELLED",
}

export interface FinanceSummary {
  totalRevenue: number;
  totalExpenses: number;
  profit: number;
  totalInvoices: number;
  paidInvoices: number;
  overdueInvoices: number;
}

const basePath = "/api/v1/financefarm/invoices";

const InvoiceService = {
  getAllInvoices: (page: number = 1): Promise<PageDTO<Invoice>> =>
    api.get(`${basePath}?page=${page}`),
  getInvoiceById: (id: string): Promise<Invoice> => api.get(`${basePath}/${id}`),
  createInvoice: (data: Partial<Invoice>): Promise<Invoice> => api.post(basePath, data),
  updateInvoice: (id: string, data: Partial<Invoice>): Promise<Invoice> =>
    api.put(`${basePath}/${id}`, data),
  deleteInvoice: (id: string): Promise<void> => api.delete(`${basePath}/${id}`),
  updateInvoiceStatus: (id: string, status: InvoiceStatus): Promise<Invoice> =>
    api.patch(`${basePath}/${id}/status?status=${status}`, {}),
  getFinanceSummary: (): Promise<FinanceSummary> => api.get(`${basePath}/stats`),
};

export default InvoiceService;