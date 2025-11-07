package ccm.cva.report.application.service;

public enum ReportFormat {
    JSON,
    PDF;

    public static ReportFormat from(String value) {
        if (value == null || value.isBlank()) {
            return JSON;
        }
        return switch (value.trim().toLowerCase()) {
            case "pdf" -> PDF;
            case "json" -> JSON;
            default -> throw new IllegalArgumentException("Unsupported format: " + value);
        };
    }
}
