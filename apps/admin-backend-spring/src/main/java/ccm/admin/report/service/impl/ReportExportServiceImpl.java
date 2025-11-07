package ccm.admin.report.service.impl;

import ccm.admin.report.entity.ReportHistory;
import ccm.admin.report.repository.ReportHistoryRepository;
import ccm.admin.report.service.ReportExportService;
import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.repository.TransactionRepository;
import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.user.spec.UserSpecification;
import ccm.common.spec.BaseSpecification;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Report - Service Implementation - Business logic for Report operations
 */

public class ReportExportServiceImpl implements ReportExportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ReportHistoryRepository reportHistoryRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Export data - modifies data
     */
    @Override
    public byte[] exportTransactionsCSV(
            LocalDateTime from,
            LocalDateTime to,
            String status,
            String type,
            String keyword
    ) {
        log.info("Exporting transactions to CSV: from={}, to={}, status={}, type={}, keyword={}",
                from, to, status, type, keyword);

        List<Transaction> transactions = fetchTransactions(from, to, status, type, keyword);

        try (StringWriter sw = new StringWriter(); CSVWriter writer = new CSVWriter(sw)) {

            writer.writeNext(new String[]{
                "ID", "Code", "Buyer Email", "Seller Email",
                "Amount", "Total Price", "Status", "Type", "Created At"
            });

            for (Transaction tx : transactions) {
                writer.writeNext(new String[]{
                    String.valueOf(tx.getId()),
                    tx.getTransactionCode(),
                    tx.getBuyerEmail(),
                    tx.getSellerEmail(),
                    String.valueOf(tx.getAmount()),
                    String.valueOf(tx.getTotalPrice()),
                    tx.getStatus().name(),
                    tx.getType().name(),
                    tx.getCreatedAt().format(DATE_FORMATTER)
                });
            }

            return sw.toString().getBytes();

        } catch (IOException e) {
            log.error("Failed to export transactions to CSV", e);
            throw new RuntimeException("Failed to export transactions to CSV", e);
        }
    }

    /**
     * Export data - modifies data
     */
    @Override
    public byte[] exportTransactionsXLSX(
            LocalDateTime from,
            LocalDateTime to,
            String status,
            String type,
            String keyword
    ) {
        log.info("Exporting transactions to XLSX: from={}, to={}, status={}, type={}, keyword={}",
                from, to, status, type, keyword);

        List<Transaction> transactions = fetchTransactions(from, to, status, type, keyword);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Transactions");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Code", "Buyer Email", "Seller Email",
                "Amount", "Total Price", "Status", "Type", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Transaction tx : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(tx.getId());
                row.createCell(1).setCellValue(tx.getTransactionCode());
                row.createCell(2).setCellValue(tx.getBuyerEmail());
                row.createCell(3).setCellValue(tx.getSellerEmail());
                row.createCell(4).setCellValue(tx.getAmount());
                row.createCell(5).setCellValue(tx.getTotalPrice());
                row.createCell(6).setCellValue(tx.getStatus().name());
                row.createCell(7).setCellValue(tx.getType().name());
                row.createCell(8).setCellValue(tx.getCreatedAt().format(DATE_FORMATTER));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to export transactions to XLSX", e);
            throw new RuntimeException("Failed to export transactions to XLSX", e);
        }
    }

    /**
     * Export data - modifies data
     */
    @Override
    public byte[] exportTransactionsPDF(
            LocalDateTime from,
            LocalDateTime to,
            String status,
            String type,
            String keyword
    ) {
        log.info("Exporting transactions to PDF: from={}, to={}, status={}, type={}, keyword={}",
                from, to, status, type, keyword);

        List<Transaction> transactions = fetchTransactions(from, to, status, type, keyword);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Transactions Report");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("ID    Code           Buyer              Amount    Status       Created");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 9);
                float y = 700;
                int count = 0;
                for (Transaction tx : transactions) {
                    if (count++ >= 30) {
                        break;
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    String line = String.format("%-5d %-14s %-18s %8.2f %-12s %s",
                            tx.getId(),
                            truncate(tx.getTransactionCode(), 14),
                            truncate(tx.getBuyerEmail(), 18),
                            tx.getAmount(),
                            tx.getStatus().name(),
                            tx.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    );
                    contentStream.showText(line);
                    contentStream.endText();
                    y -= 15;
                }

                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("Total: " + transactions.size() + " transactions (showing max 30)");
                contentStream.endText();
            }

            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to export transactions to PDF", e);
            throw new RuntimeException("Failed to export transactions to PDF", e);
        }
    }

    /**
     * Export data - modifies data
     */
    @Override
    public byte[] exportUsersCSV(String status, String role, String keyword) {
        log.info("Exporting users to CSV: status={}, role={}, keyword={}", status, role, keyword);

        List<User> users = fetchUsers(status, role, keyword);

        try (StringWriter sw = new StringWriter(); CSVWriter writer = new CSVWriter(sw)) {

            writer.writeNext(new String[]{
                "ID", "Email", "Full Name", "Role", "Status", "Created At"
            });

            for (User user : users) {
                writer.writeNext(new String[]{
                    String.valueOf(user.getId()),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole() != null ? user.getRole().getName() : "N/A",
                    user.getStatus().name(),
                    user.getCreatedAt().format(DATE_FORMATTER)
                });
            }

            return sw.toString().getBytes();

        } catch (IOException e) {
            log.error("Failed to export users to CSV", e);
            throw new RuntimeException("Failed to export users to CSV", e);
        }
    }

    /**
     * Export data - modifies data
     */
    @Override
    public byte[] exportUsersXLSX(String status, String role, String keyword) {
        log.info("Exporting users to XLSX: status={}, role={}, keyword={}", status, role, keyword);

        List<User> users = fetchUsers(status, role, keyword);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Users");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Email", "Full Name", "Role", "Status", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getEmail());
                row.createCell(2).setCellValue(user.getFullName());
                row.createCell(3).setCellValue(user.getRole() != null ? user.getRole().getName() : "N/A");
                row.createCell(4).setCellValue(user.getStatus().name());
                row.createCell(5).setCellValue(user.getCreatedAt().format(DATE_FORMATTER));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to export users to XLSX", e);
            throw new RuntimeException("Failed to export users to XLSX", e);
        }
    }

    /**
     * Export data - modifies data
     */
    @Override
    public byte[] exportUsersPDF(String status, String role, String keyword) {
        log.info("Exporting users to PDF: status={}, role={}, keyword={}", status, role, keyword);

        List<User> users = fetchUsers(status, role, keyword);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Users Report");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("ID    Email                      Full Name              Role        Status");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 9);
                float y = 700;
                int count = 0;
                for (User user : users) {
                    if (count++ >= 30) {
                        break;
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    String line = String.format("%-5d %-26s %-22s %-11s %s",
                            user.getId(),
                            truncate(user.getEmail(), 26),
                            truncate(user.getFullName(), 22),
                            user.getRole() != null ? truncate(user.getRole().getName(), 11) : "N/A",
                            user.getStatus().name()
                    );
                    contentStream.showText(line);
                    contentStream.endText();
                    y -= 15;
                }

                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("Total: " + users.size() + " users (showing max 30)");
                contentStream.endText();
            }

            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to export users to PDF", e);
            throw new RuntimeException("Failed to export users to PDF", e);
        }
    }

    private List<Transaction> fetchTransactions(
            LocalDateTime from,
            LocalDateTime to,
            String status,
            String type,
            String keyword
    ) {
        Specification<Transaction> spec = BaseSpecification.<Transaction>builder()
                .keyword(keyword, "buyerEmail", "sellerEmail", "transactionCode")
                .enumEquals("status", status, ccm.admin.transaction.entity.enums.TransactionStatus.class)
                .enumEquals("type", type, ccm.admin.transaction.entity.enums.TransactionType.class)
                .dateTimeRange("createdAt", from, to)
                .build();

        return transactionRepository.findAll(spec);
    }

    private List<User> fetchUsers(String status, String role, String keyword) {
        Specification<User> spec = BaseSpecification.<User>builder()
                .keyword(keyword, "email", "fullName")
                .enumEquals("status", status, AccountStatus.class)
                .build();

        if (role != null && !role.isBlank()) {
            spec = BaseSpecification.and(spec, UserSpecification.roleEquals(role));
        }

        spec = BaseSpecification.and(spec, UserSpecification.fetchRole());

        return userRepository.findAll(spec);
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }

    /**
     * Save report generation history
     *
     * @param type Report type (TRANSACTION, USER, etc.)
     * @param format Export format (CSV, EXCEL, PDF)
     * @param adminId Admin who generated the report
     * @param from Start date for report
     * @param to End date for report
     * @param parameters Additional parameters
     */
    private void saveReportHistory(
            String type,
            String format,
            Long adminId,
            LocalDateTime from,
            LocalDateTime to,
            String parameters
    ) {
        try {
            ReportHistory history = ReportHistory.builder()
                    .type(type)
                    .format(format)
                    .generatedBy(adminId != null ? adminId : 1L) // Default to admin ID 1
                    .startDate(from != null ? from.toLocalDate() : null)
                    .endDate(to != null ? to.toLocalDate() : null)
                    .parameters(parameters)
                    .filePath("Generated in-memory (not persisted)")
                    .build();

            reportHistoryRepository.save(history);
            log.info("Saved report history: type={}, format={}, generatedBy={}", type, format, adminId);
        } catch (Exception e) {
            log.error("Failed to save report history: {}", e.getMessage(), e);
            // Don't fail the export if history save fails
        }
    }
}
