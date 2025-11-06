package ccm.buyer.dto.response;

import java.time.LocalDateTime;

public record InvoiceResponse(
    Long id,
    Long trId,
    LocalDateTime issueDate,
    String filePath
) {}
