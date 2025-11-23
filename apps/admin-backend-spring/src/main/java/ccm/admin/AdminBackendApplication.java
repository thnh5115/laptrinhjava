package ccm.admin; // GIỮ NGUYÊN PACKAGE CỦA BẠN (Ví dụ: ccm.admin, ccm.buyer...)

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// --- CÁC IMPORT QUAN TRỌNG ---
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.sql.Connection;
// -----------------------------

@SpringBootApplication
public class AdminBackendApplication { // Tên class của bạn

    // Inject JdbcTemplate để chạy câu lệnh đếm trực tiếp
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(AdminBackendApplication.class, args);
    }

    // --- HÀM KIỂM TRA DỮ LIỆU THỰC TẾ ---
    @Bean
    public CommandLineRunner diagnostic(DataSource dataSource) {
        return args -> {
            System.out.println("\n==================================================");
            System.out.println("🕵️  KẾT QUẢ ĐIỀU TRA DATABASE:");
            try {
                // 1. Xác nhận lại địa chỉ kết nối
                Connection conn = dataSource.getConnection();
                System.out.println("👉 Đang kết nối tới: " + conn.getMetaData().getURL());

                // 2. ĐẾM SỐ DÒNG TRONG BẢNG USERS
                // Lưu ý: Nếu bảng của bạn tên là 'user' (không s), hãy sửa câu lệnh bên dưới
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
                
                System.out.println("👉 Số lượng tài khoản tìm thấy: " + count);
                
                if (count == 0) {
                    System.out.println("❌ KẾT LUẬN: Database đang TRỐNG RỖNG.");
                    System.out.println("   -> Nguyên nhân: Lệnh INSERT đã bị Rollback (hủy) hoặc chưa bao giờ chạy.");
                } else {
                    System.out.println("✅ KẾT LUẬN: Có " + count + " người dùng trong Database.");
                    System.out.println("   -> Nếu Workbench không thấy, bạn đang xem sai Database/Schema rồi!");
                }

            } catch (Exception e) {
                System.out.println("❌ LỖI KHI KIỂM TRA: " + e.getMessage());
                // Nếu lỗi "Table 'ccm.users' doesn't exist", nghĩa là chưa tạo bảng hoặc sai tên bảng
            }
            System.out.println("==================================================\n");
        };
    }
}