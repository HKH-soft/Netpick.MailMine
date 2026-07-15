package ir.netpick.platform.core.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Telegram Bot integration for notifications.
 */
@Service
@Slf4j
public class TelegramNotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.chat-id:}")
    private String defaultChatId;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";

    /**
     * Send notification to default chat.
     */
    public boolean sendNotification(String message) {
        return sendNotification(defaultChatId, message);
    }

    /**
     * Send notification to specific chat.
     */
    public boolean sendNotification(String chatId, String message) {
        if (botToken.isEmpty()) {
            log.warn("Telegram bot token not configured");
            return false;
        }

        try {
            String url = String.format(TELEGRAM_API_URL, botToken);
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("text", message);
            payload.put("parse_mode", "HTML");

            ResponseEntity<TelegramResponse> response = restTemplate.postForEntity(
                url, payload, TelegramResponse.class
            );

            return response.getBody() != null && response.getBody().isOk();
        } catch (Exception e) {
            log.error("Failed to send Telegram notification: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Send invoice notification.
     */
    public boolean sendInvoiceNotification(String invoiceNumber, String amount, String customer) {
        String message = String.format(
            "<b>فاکتور جدید</b>\n" +
            "شماره: %s\n" +
            "مبلغ: %s\n" +
            "مشتری: %s",
            invoiceNumber, amount, customer
        );
        return sendNotification(message);
    }

    /**
     * Send payment notification.
     */
    public boolean sendPaymentNotification(String transactionId, String amount, String status) {
        String message = String.format(
            "<b>پرداخت جدید</b>\n" +
            "شناسه: %s\n" +
            "مبلغ: %s\n" +
            "وضعیت: %s",
            transactionId, amount, status
        );
        return sendNotification(message);
    }

    /**
     * Send task reminder.
     */
    public boolean sendTaskReminder(String taskTitle, String dueDate, String assignee) {
        String message = String.format(
            "<b>یادآوری کار</b>\n" +
            "عنوان: %s\n" +
            "مهلت: %s\n" +
            "مسئول: %s",
            taskTitle, dueDate, assignee
        );
        return sendNotification(message);
    }

    @lombok.Data
    private static class TelegramResponse {
        private boolean ok;
        private Result result;
    }

    @lombok.Data
    private static class Result {
        private long message_id;
        private Chat chat;
    }

    @lombok.Data
    private static class Chat {
        private long id;
        private String title;
    }
}