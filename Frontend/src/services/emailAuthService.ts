// emailAuthService.ts
import api from './api';

export interface DomainAuthResult {
  domain: string;
  checkedAt: string;
  spf: {
    valid: boolean;
    record: string | null;
    hasAllMechanism: boolean;
    allMechanismType: string;
    hasInclude: boolean;
    note?: string;
  };
  dmarc: {
    valid: boolean;
    record: string | null;
    policy: string;
    hasRuf: boolean;
    hasRua: boolean;
    pct: string;
    note?: string;
  };
}

class EmailAuthService {
  private basePath = '/api/v1/mailmine/email-auth';

  async validateDomain(domain: string): Promise<DomainAuthResult> {
    return await api.get<DomainAuthResult>(`${this.basePath}/validate?domain=${encodeURIComponent(domain)}`);
  }

  async validateEmail(email: string): Promise<DomainAuthResult> {
    return await api.get<DomainAuthResult>(`${this.basePath}/validate-email?email=${encodeURIComponent(email)}`);
  }
}

const emailAuthService = new EmailAuthService();
export default emailAuthService;



