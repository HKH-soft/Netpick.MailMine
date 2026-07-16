// customsService.ts
import api, { PageDTO } from './api';

export interface CustomsDeclaration {
  id: string;
  declarationNumber: string;
  invoiceId: string;
  declarationDate: string;
  customsOffice?: string;
  originCountry?: string;
  destinationCountry?: string;
  hsCode?: string;
  productDescription?: string;
  quantity?: number;
  unit?: string;
  customsValue?: number;
  dutyAmount?: number;
  vatAmount?: number;
  totalTax?: number;
  status: string;
  trackingNumber?: string;
}

class CustomsService {
  private basePath = '/api/customs-declarations';

  public async getAllDeclarations(page: number = 1): Promise<PageDTO<CustomsDeclaration>> {
    return await api.get<PageDTO<CustomsDeclaration>>(`${this.basePath}?pageNumber=${page}`);
  }

  public async getDeclarationsByStatus(status: string, page: number = 1): Promise<PageDTO<CustomsDeclaration>> {
    return await api.get<PageDTO<CustomsDeclaration>>(`${this.basePath}/status/${status}?pageNumber=${page}`);
  }

  public async getDeclaration(id: string): Promise<CustomsDeclaration> {
    return await api.get<CustomsDeclaration>(`${this.basePath}/${id}`);
  }

  public async getByDeclarationNumber(number: string): Promise<CustomsDeclaration> {
    return await api.get<CustomsDeclaration>(`${this.basePath}/number/${number}`);
  }

  public async createDeclaration(declaration: Partial<CustomsDeclaration>): Promise<CustomsDeclaration> {
    return await api.post<CustomsDeclaration>(this.basePath, declaration);
  }

  public async updateDeclaration(id: string, declaration: Partial<CustomsDeclaration>): Promise<CustomsDeclaration> {
    return await api.put<CustomsDeclaration>(`${this.basePath}/${id}`, declaration);
  }

  public async deleteDeclaration(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }

  public async submitDeclaration(id: string): Promise<CustomsDeclaration> {
    return await api.post<CustomsDeclaration>(`${this.basePath}/${id}/submit`, {});
  }

  public async approveDeclaration(id: string): Promise<CustomsDeclaration> {
    return await api.post<CustomsDeclaration>(`${this.basePath}/${id}/approve`, {});
  }
}

const customsService = new CustomsService();
export default customsService;