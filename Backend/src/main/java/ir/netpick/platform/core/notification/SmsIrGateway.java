package ir.netpick.platform.core.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SMS.ir Gateway integration for Iranian SMS notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsIrGateway {

    private final RestTemplate restTemplate;

    @Value("${smsir.api.key:}")
    private String apiKey;

    @Value("${smsir.api.secret-key:}")
    private String secretKey;

    @Value("${smsir.api.base-url:https://api.sms.ir/v1}")
    private String baseUrl;

    @Value("${smsir.sender-number:}")
    private String senderNumber;

    /**
     * Send SMS to single recipient.
     */
    public boolean sendSms(String phoneNumber, String message) {
        return sendSms(List.of(phoneNumber), message);
    }

    /**
     * Send SMS to multiple recipients.
     */
    public boolean sendSms(List<String> phoneNumbers, String message) {
        if (apiKey.isEmpty() || secretKey.isEmpty()) {
            log.warn("SMS.ir credentials not configured");
            return false;
        }

        try {
            String url = baseUrl + "/send/bulk";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            headers.set("secret-key", secretKey);

            Map<String, Object> payload = new HashMap<>();
            payload.put("lineNumber", senderNumber);
            payload.put("messageText", message);
            payload.put("mobiles", phoneNumbers);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<SmsResponse> response = restTemplate.postForEntity(url, entity, SmsResponse.class);

            return response.getBody() != null && response.getBody().isSuccess();
        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Send verification code.
     */
    public boolean sendVerificationCode(String phoneNumber, String code) {
        String message = String.format("کد تایید شما: %s", code);
        return sendSms(phoneNumber, message);
    }

    /**
     * Send invoice notification.
     */
    public boolean sendInvoiceNotification(String phoneNumber, String invoiceNumber, String amount) {
        String message = String.format("فاکتور جدید - شماره: %s - مبلغ: %s ریال", invoiceNumber, amount);
        return sendSms(phoneNumber, message);
    }

    /**
     * Send payment confirmation.
     */
    public boolean sendPaymentConfirmation(String phoneNumber, String transactionId, String amount) {
        String message = String.format("پرداخت شما ثبت شد - شناسه: %s - مبلغ: %s ریال", transactionId, amount);
        return sendSms(phoneNumber, message);
    }

    /**
     * Send low stock alert.
     */
    public boolean sendLowStockAlert(String phoneNumber, String productName, int currentStock, int minStock) {
        String message = String.format("هشدار موجودی کم - محصول: %s - موجودی فعلی: %d - حداقل موجودی: %d", 
            productName, currentStock, minStock);
        return sendSms(phoneNumber, message);
    }

    private static class SmsResponse {
        private boolean success;
        private int messageId;
        private String message;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public int getMessageId() { return messageId; }
        public void setMessageId(int messageId) { this.messageId = messageId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}