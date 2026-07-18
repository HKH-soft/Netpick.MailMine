package ir.netpick.platform.gatekeeper.service;

import ir.netpick.platform.gatekeeper.model.PasswordHistory;
import ir.netpick.platform.gatekeeper.model.User;
import ir.netpick.platform.gatekeeper.repository.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordHistoryService {

    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.password.history-size:5}")
    private int historySize;

    @Transactional
    public void recordPassword(User user, String passwordHash) {
        PasswordHistory history = new PasswordHistory();
        history.setUser(user);
        history.setPasswordHash(passwordHash);
        passwordHistoryRepository.save(history);

        trimHistory(user.getId());

        log.debug("Password history recorded for user: {}", user.getEmail());
    }

    public boolean isPasswordReused(User user, String newPassword) {
        List<String> recentHashes = passwordHistoryRepository.findRecentHashesByUserId(user.getId());
        for (String oldHash : recentHashes) {
            if (passwordEncoder.matches(newPassword, oldHash)) {
                log.warn("Password reuse detected for user: {}", user.getEmail());
                return true;
            }
        }
        return false;
    }

    private void trimHistory(UUID userId) {
        List<PasswordHistory> all = passwordHistoryRepository.findRecentByUserId(userId);
        if (all.size() > historySize) {
            List<PasswordHistory> toDelete = all.subList(historySize, all.size());
            passwordHistoryRepository.deleteAll(toDelete);
        }
    }
}
