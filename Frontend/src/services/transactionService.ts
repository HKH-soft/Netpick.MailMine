// transactionService.ts
import api, { PageDTO } from "./api";

export interface Transaction {
  id: string;
  amount: number;
  type: TransactionType;
  category: string;
  description: string;
  date: string;
  invoiceId: string | null;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export enum TransactionType {
  INCOME = "INCOME",
  EXPENSE = "EXPENSE",
}

const basePath = "/api/v1/financefarm/transactions";

const TransactionService = {
  getAllTransactions: (page: number = 1): Promise<PageDTO<Transaction>> =>
    api.get(`${basePath}?page=${page}`),
  getTransactionById: (id: string): Promise<Transaction> => api.get(`${basePath}/${id}`),
  createTransaction: (data: Partial<Transaction>): Promise<Transaction> =>
    api.post(basePath, data),
  updateTransaction: (id: string, data: Partial<Transaction>): Promise<Transaction> =>
    api.put(`${basePath}/${id}`, data),
  deleteTransaction: (id: string): Promise<void> => api.delete(`${basePath}/${id}`),
  importTransactions: (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);
    return api.post(`${basePath}/import`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
};

export default TransactionService;