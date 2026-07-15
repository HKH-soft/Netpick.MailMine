# Missing Features for Iranian Market - Extension Requirements

Based on the deep dive comparison between Netpick and Odoo, here are essential extensions needed:

## 1. Iranian Banking Integration

### Current Gap
- No Iranian bank support (Odoo supports 28,000+ banks but none from Iran)
- No Sheba/Iban validation for Iranian banks
- No integration with local payment gateways

### Required Extensions
| Feature | Description | Priority |
|---------|-------------|----------|
| **Bank Mellat Integration** | Connect to Mellat API for transaction sync | High |
| **Bank Melli Integration** | Connect to Melli API for transaction sync | High |
| **Bank Saderat Integration** | Connect to Saderat API for transaction sync | Medium |
| **ZarinPal Gateway** | Payment link generation and tracking | High |
| **IDPay Gateway** | Alternative payment gateway integration | Medium |
| **Sheba Validator** | Validate Iranian Sheba numbers | Medium |

---

## 2. Iranian Tax & Compliance

### Current Gap
- No Iranian tax chart of accounts
- No VAT/sales tax handling for Iran
- No Iranian fiscal year support

### Required Extensions
| Feature | Description | Priority |
|---------|-------------|----------|
| **Iranian Tax Codes** | Add Iranian tax categories (9% VAT equivalent) | High |
| **Customs Duty Tracking** | Import/export duty calculations | Medium |
| **Tax Report Templates** | Monthly/quarterly tax reports for Iran | High |
| **Persian Calendar** | Convert Gregorian to Persian dates in reports | High |
| **Invoice Numbering** | Iranian invoice sequence requirements | Medium |

---

## 3. Enhanced Communication Features

### Current Gap
- No Telegram integration (widely used in Iran)
- No Iranian SMS gateway support
- No local email provider integration

### Required Extensions
| Feature | Description | Priority |
|---------|-------------|----------|
| **Telegram Bot Integration** | Send notifications via Telegram | High |
| **SMS.ir Gateway** | SMS notifications via Iranian providers | High |
| **Local Email Templates** | Persian email templates for common workflows | Medium |
| **WhatsApp Business API** | Integration for customer communication | Medium |

---

## 4. Iranian Market Data Sources

### Current Gap
- MailMine doesn't target Iranian business directories
- No Persian website scraping optimization

### Required Extensions
| Feature | Description | Priority |
|---------|-------------|----------|
| **Tiwall Scraper** | Extract business data from Tiwall.com | High |
| **Markaz Scraper** | Extract from Markaz.ir business directory | High |
| **Iranian Yellow Pages** | Integration with local directories | Medium |
| **Persian News Sites** | Extract company news for lead enrichment | Low |
| **Telegram Group Scraper** | Extract from business Telegram groups | Medium |

---

## 5. Multi-Currency & Exchange

### Current Gap
- Basic currency support exists but no IRR exchange rate automation
- No TSE (Tehran Stock Exchange) integration

### Required Extensions
| Feature | Description | Priority |
|---------|-------------|----------|
| **TSE Exchange Rates** | Daily IRR/USD/TMN rates from TSE | High |
| **Currency Converter Widget** | Real-time conversion in UI | Medium |
| **Multi-currency Reports** | Reports in multiple currencies | Medium |
| **Inflation Adjustment** | Adjust historical values for inflation | Low |

---

## 6. Persian-Specific Features

### Current Gap
- Basic Farsi translation exists but limited
- No Persian number formatting
- No right-to-left form improvements

### Required Extensions
| Feature | Description | Priority |
|---------|-------------|----------|
| **Persian Numbers** | Convert Arabic numerals to Persian (۰۱۲۳) | High |
| **RTL Form Enhancements** | Better RTL support in forms | Medium |
| **Persian Date Picker** | Persian calendar date selection | High |
| **Farsi PDF Reports** | Generate reports in Farsi | Medium |
| **Persian SMS Templates** | RTL SMS templates | Low |

---

## 7. Logistics & Shipping (for Import/Export)

### Current Gap
- No Iranian logistics provider integration
- No customs tracking

### Required Extensions
| Feature | Description | Priority |
|---------|-------------|----------|
| **Posta Iran Tracking** | Track shipments via Iranian Post | Medium |
| **Snapp! Logistics** | Integration with Snapp cargo | Medium |
| **Customs Declaration** | Generate customs forms | High |
| **Shipping Cost Calculator** | Calculate based on Iranian routes | Medium |

---

## 8. Mobile & Offline Features

### Current Gap
- No mobile app (Odoo has native mobile)
- No offline capability

### Required Extensions
| Feature | Description | Priority |
|---------|-------------|----------|
| **PWA Support** | Installable mobile experience | High |
| **Offline Mode** | Work without internet connection | Medium |
| **Mobile Notifications** | Push notifications for tasks | Medium |

---

## Priority Matrix

| Priority | Features |
|----------|----------|
| **High** | Banking integration, Tax compliance, Telegram/SMS, Iranian data sources, TSE rates, Persian dates/numbers, Customs forms, PWA |
| **Medium** | WhatsApp API, Currency reports, RTL forms, Logistics tracking, Offline mode |
| **Low** | Inflation adjustment, Farsi PDF, Telegram group scraper |

---

## Implementation Notes

1. **Start with High priority** - These are blockers for Iranian adoption
2. **Partner with local providers** - SMS.ir, ZarinPal for official integrations
3. **Use TSE API** - Free exchange rate API available
4. **Test with personas** - Validate with the 6 Iranian personas defined