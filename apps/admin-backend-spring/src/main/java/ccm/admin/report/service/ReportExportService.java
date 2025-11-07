package ccm.admin.report.service;

import java.time.LocalDateTime;

/** service - Service Interface - Export data to CSV/Excel/PDF */

public interface ReportExportService {

    
    byte[] exportTransactionsCSV(
        LocalDateTime from,
        LocalDateTime to,
        String status,
        String type,
        String keyword
    );

    
    byte[] exportTransactionsXLSX(
        LocalDateTime from,
        LocalDateTime to,
        String status,
        String type,
        String keyword
    );

    
    byte[] exportTransactionsPDF(
        LocalDateTime from,
        LocalDateTime to,
        String status,
        String type,
        String keyword
    );

    
    byte[] exportUsersCSV(
        String status,
        String role,
        String keyword
    );

    
    byte[] exportUsersXLSX(
        String status,
        String role,
        String keyword
    );

    
    byte[] exportUsersPDF(
        String status,
        String role,
        String keyword
    );
}
