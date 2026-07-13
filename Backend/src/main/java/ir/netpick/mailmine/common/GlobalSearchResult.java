package ir.netpick.mailmine.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class GlobalSearchResult {
    private String type;
    private String title;
    private String subtitle;
    private String id;
}
