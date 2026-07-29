package ir.netpick.platform.core;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

@Converter(autoApply = true)
public class UUIDAttributeConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        if (uuid == null) return null;
        return uuid.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        return UUID.fromString(dbData);
    }
}
