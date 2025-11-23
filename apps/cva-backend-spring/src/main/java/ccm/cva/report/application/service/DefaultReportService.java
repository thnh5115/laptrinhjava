
package ccm.cva.report.application.service;

import ccm.cva.report.application.dto.CarbonAuditReport;
import ccm.cva.report.application.dto.CreditIssuanceSummary;
import ccm.cva.shared.exception.ResourceNotFoundException;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultReportService implements ReportService {

    private final VerificationRequestRepository verificationRequestRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DefaultReportService(VerificationRequestRepository verificationRequestRepository) {
        this.verificationRequestRepository = verificationRequestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CarbonAuditReport buildReport(Long requestId) { // SỬA: Long
        VerificationRequest request = verificationRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Verification request %s not found".formatted(requestId)));

        CreditIssuanceSummary issuanceSummary = Optional.ofNullable(request.getCreditIssuance())
            .map(this::toSummary)
            .orElse(null);
            
        // SỬA: Dùng LocalDateTime
        LocalDateTime generatedAt = LocalDateTime.now();
        String signature = generateSignature(request, issuanceSummary, generatedAt);

        return new CarbonAuditReport(
            request.getId(),
            request.getOwnerId(),
            request.getTripId(),
            request.getDistanceKm(),
            request.getEnergyKwh(),
            request.getChecksum(),
            request.getStatus(),
            request.getCreatedAt(),
            request.getVerifiedAt(),
            request.getVerifierId(),
            request.getNotes(),
            issuanceSummary,
            signature,
            generatedAt
        );
    }

    @Override
    public byte[] renderPdf(CarbonAuditReport report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph("Carbon Audit Report", headerFont));

            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            // SỬA: formatDateTime
            document.add(new Paragraph("Generated at: " + formatDateTime(report.generatedAt()), metaFont));
            document.add(new Paragraph("Signature: " + report.signature(), metaFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addRow(table, "Request ID", report.requestId().toString());
            addRow(table, "Owner ID", report.ownerId().toString());
            addRow(table, "Trip ID", report.tripId());
            addRow(table, "Checksum", report.checksum());
            addRow(table, "Status", report.status().name());
            // SỬA: formatDateTime
            addRow(table, "Created At", formatDateTime(report.createdAt()));
            addRow(table, "Verified At", formatDateTime(report.verifiedAt()));
            addRow(table, "Verifier ID", report.verifierId() != null ? report.verifierId().toString() : "-");
            addRow(table, "Distance (km)", formatDecimal(report.distanceKm()));
            addRow(table, "Energy (kWh)", formatDecimal(report.energyKwh()));
            addRow(table, "Notes", Optional.ofNullable(report.notes()).orElse("-"));

            CreditIssuanceSummary issuance = report.issuance();
            if (issuance != null) {
                addRow(table, "Issuance ID", issuance.id().toString());
                addRow(table, "CO2 Reduced (kg)", formatDecimal(issuance.co2ReducedKg()));
                addRow(table, "Credits Raw", formatDecimal(issuance.creditsRaw()));
                addRow(table, "Credits Rounded", formatDecimal(issuance.creditsRounded()));
                addRow(table, "Idempotency Key", issuance.idempotencyKey());
                addRow(table, "Correlation ID", Optional.ofNullable(issuance.correlationId()).orElse("-"));
                // SỬA: formatDateTime
                addRow(table, "Issuance Created", formatDateTime(issuance.createdAt()));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("Unable to render PDF report", ex);
        }
    }

    private CreditIssuanceSummary toSummary(CreditIssuance issuance) {
        return new CreditIssuanceSummary(
            issuance.getId(), // Long
            issuance.getCo2ReducedKg(),
            issuance.getCreditsRaw(),
            issuance.getCreditsRounded(),
            issuance.getIdempotencyKey(),
            issuance.getCorrelationId(),
            issuance.getCreatedAt() // LocalDateTime
        );
    }

    // SỬA: Tham số generatedAt là LocalDateTime
    private String generateSignature(VerificationRequest request, CreditIssuanceSummary issuance, LocalDateTime generatedAt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(request.getId().toString().getBytes(StandardCharsets.UTF_8));
            digest.update(request.getChecksum().getBytes(StandardCharsets.UTF_8));
            digest.update(request.getStatus().name().getBytes(StandardCharsets.UTF_8));
            digest.update(request.getCreatedAt().toString().getBytes(StandardCharsets.UTF_8));
            if (request.getVerifiedAt() != null) {
                digest.update(request.getVerifiedAt().toString().getBytes(StandardCharsets.UTF_8));
            }
            if (issuance != null) {
                digest.update(issuance.id().toString().getBytes(StandardCharsets.UTF_8));
                digest.update(issuance.creditsRounded().toPlainString().getBytes(StandardCharsets.UTF_8));
            }
            digest.update(generatedAt.toString().getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label));
        labelCell.setPadding(6f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Paragraph(value));
        valueCell.setPadding(6f);
        table.addCell(valueCell);
    }

    private String formatDecimal(BigDecimal value) {
        return value != null ? value.stripTrailingZeros().toPlainString() : "-";
    }

    // SỬA: Hàm format cho LocalDateTime
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "-";
    }
}