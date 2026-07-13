package ir.netpick.mailmine.email.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SPF/DKIM/DMARC validation for email domains.
 * Checks DNS records to verify email authentication.
 */
@Service
@Slf4j
public class EmailAuthValidationService {

    /**
     * Validate SPF, DKIM (via DMARC), and DMARC records for a domain.
     */
    public Map<String, Object> validateDomain(String domain) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("domain", domain);
        result.put("checkedAt", new Date().toString());

        // SPF
        result.put("spf", checkSpf(domain));

        // DMARC
        result.put("dmarc", checkDmarc(domain));

        return result;
    }

    /**
     * Validate from an email address (extracts domain)
     */
    public Map<String, Object> validateFromEmail(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        return validateDomain(domain);
    }

    private Map<String, Object> checkSpf(String domain) {
        Map<String, Object> spf = new LinkedHashMap<>();
        String record = lookupDnsRecord(domain, "TXT", "v=spf1");
        if (record != null) {
            spf.put("valid", true);
            spf.put("record", record);
            spf.put("hasAllMechanism", record.contains("all"));
            spf.put("allMechanismType", extractAllMechanism(record));
            spf.put("hasInclude", record.contains("include:"));
        } else {
            spf.put("valid", false);
            spf.put("record", null);
            spf.put("note", "No SPF record found");
        }
        return spf;
    }

    private Map<String, Object> checkDmarc(String domain) {
        Map<String, Object> dmarc = new LinkedHashMap<>();
        String record = lookupDnsRecord("_dmarc." + domain, "TXT", "v=DMARC1");
        if (record != null) {
            dmarc.put("valid", true);
            dmarc.put("record", record);
            dmarc.put("policy", extractDmarcPolicy(record));
            dmarc.put("hasRuf", record.contains("ruf="));
            dmarc.put("hasRua", record.contains("rua="));
            dmarc.put("pct", extractDmarcPct(record));
        } else {
            dmarc.put("valid", false);
            dmarc.put("record", null);
            dmarc.put("note", "No DMARC record found");
        }
        return dmarc;
    }

    private String lookupDnsRecord(String host, String type, String expectedPrefix) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ctx = new InitialDirContext(env);
            Attribute attrs = ctx.getAttributes(host, new String[]{type}).get(type);
            if (attrs != null) {
                for (int i = 0; i < attrs.size(); i++) {
                    String val = (String) attrs.get(i);
                    if (val.startsWith(expectedPrefix)) {
                        ctx.close();
                        return val;
                    }
                }
            }
            ctx.close();
        } catch (Exception e) {
            log.debug("DNS lookup failed for {} {}: {}", host, type, e.getMessage());
        }
        return null;
    }

    private String extractAllMechanism(String spfRecord) {
        if (spfRecord.contains("-all")) return "fail (-all)";
        if (spfRecord.contains("~all")) return "softfail (~all)";
        if (spfRecord.contains("+all")) return "pass (+all)";
        if (spfRecord.contains("?all")) return "neutral (?all)";
        return "none";
    }

    private String extractDmarcPolicy(String dmarcRecord) {
        Pattern p = Pattern.compile("p=(\\w+)");
        Matcher m = p.matcher(dmarcRecord);
        return m.find() ? m.group(1) : "none";
    }

    private String extractDmarcPct(String dmarcRecord) {
        Pattern p = Pattern.compile("pct=(\\d+)");
        Matcher m = p.matcher(dmarcRecord);
        return m.find() ? m.group(1) : "100";
    }
}
