package ccm.cva.report.application.service;

import ccm.admin.credit.entity.CarbonCredit;
import ccm.admin.credit.repository.CarbonCreditRepository;
import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.cva.report.application.dto.CarbonAuditReport;
import ccm.cva.report.application.dto.CreditIssuanceSummary;
import ccm.cva.shared.exception.ResourceNotFoundException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultReportService implements ReportService {

    private final JourneyRepository journeyRepository;
    private final CarbonCreditRepository carbonCreditRepository;

    public DefaultReportService(JourneyRepository journeyRepository, CarbonCreditRepository carbonCreditRepository) {
        this.journeyRepository = journeyRepository;
        this.carbonCreditRepository = carbonCreditRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CarbonAuditReport buildReport(Long journeyId) {
        Journey journey = journeyRepository.findById(journeyId)
            .orElseThrow(() -> new ResourceNotFoundException("Journey %s not found".formatted(journeyId)));

        CreditIssuanceSummary issuanceSummary = carbonCreditRepository.findByJourneyId(journeyId)
            .map(this::toSummary)
            .orElse(null);
        Instant generatedAt = Instant.now();
        String signature = generateSignature(journey, issuanceSummary, generatedAt);

        return new CarbonAuditReport(
            journey.getId(),
            journey.getUserId(),
            journey.getJourneyDate(),
            journey.getStartLocation(),
            journey.getEndLocation(),
            journey.getDistanceKm(),
            journey.getEnergyUsedKwh(),
            journey.getCreditsGenerated(),
            journey.getStatus(),
            journey.getCreatedAt(),
            journey.getVerifiedAt(),
            journey.getVerifiedBy(),
            journey.getRejectionReason(),
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
            document.add(new Paragraph("Generated at: " + report.generatedAt(), metaFont));
            document.add(new Paragraph("Signature: " + report.signature(), metaFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addRow(table, "Journey ID", String.valueOf(report.journeyId()));
            addRow(table, "Owner ID", String.valueOf(report.ownerId()));
            addRow(table, "Journey Date", Optional.ofNullable(report.journeyDate()).map(Object::toString).orElse("-"));
            addRow(table, "Route", Optional.ofNullable(report.startLocation()).orElse("-") + " -> " + Optional.ofNullable(report.endLocation()).orElse("-"));
            addRow(table, "Status", report.status().name());
            addRow(table, "Created At", formatDateTime(report.createdAt()));
            addRow(table, "Verified At", formatDateTime(report.verifiedAt()));
            addRow(table, "Verifier ID", report.verifierId() != null ? report.verifierId().toString() : "-");
            addRow(table, "Distance (km)", formatDecimal(report.distanceKm()));
            addRow(table, "Energy (kWh)", formatDecimal(report.energyKwh()));
            addRow(table, "Credits Generated", formatDecimal(report.creditsGenerated()));
            addRow(table, "Rejection Reason", Optional.ofNullable(report.rejectionReason()).orElse("-"));

            CreditIssuanceSummary issuance = report.credit();
            if (issuance != null) {
                addRow(table, "Credit ID", issuance.id().toString());
                addRow(table, "Amount", formatDecimal(issuance.amount()));
                addRow(table, "Status", issuance.status().name());
                addRow(table, "Price", formatDecimal(issuance.pricePerCredit()));
                addRow(table, "Listed At", formatDateTime(issuance.listedAt()));
                addRow(table, "Sold At", formatDateTime(issuance.soldAt()));
                addRow(table, "Created At", formatDateTime(issuance.createdAt()));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("Unable to render PDF report", ex);
        }
    }

    private CreditIssuanceSummary toSummary(CarbonCredit credit) {
        return new CreditIssuanceSummary(
            credit.getId(),
            credit.getAmount(),
            credit.getStatus(),
            credit.getPricePerCredit(),
            credit.getListedAt(),
            credit.getSoldAt(),
            credit.getCreatedAt()
        );
    }

    private String generateSignature(Journey journey, CreditIssuanceSummary issuance, Instant generatedAt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(String.valueOf(journey.getId()).getBytes(StandardCharsets.UTF_8));
            digest.update(String.valueOf(journey.getUserId()).getBytes(StandardCharsets.UTF_8));
            digest.update(journey.getStatus().name().getBytes(StandardCharsets.UTF_8));
            if (journey.getCreditsGenerated() != null) {
                digest.update(journey.getCreditsGenerated().toPlainString().getBytes(StandardCharsets.UTF_8));
            }
            if (journey.getCreatedAt() != null) {
                digest.update(journey.getCreatedAt().toString().getBytes(StandardCharsets.UTF_8));
            }
            if (journey.getVerifiedAt() != null) {
                digest.update(journey.getVerifiedAt().toString().getBytes(StandardCharsets.UTF_8));
            }
            if (issuance != null) {
                digest.update(issuance.id().toString().getBytes(StandardCharsets.UTF_8));
                digest.update(issuance.amount().toPlainString().getBytes(StandardCharsets.UTF_8));
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

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toString() : "-";
    }
}
