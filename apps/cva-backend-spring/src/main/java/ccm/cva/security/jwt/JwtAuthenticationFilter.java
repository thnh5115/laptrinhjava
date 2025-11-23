package ccm.cva.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Kiểm tra Header có Token không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Lấy Token ra
        jwt = authHeader.substring(7);

        try {
            username = jwtService.extractUsername(jwt);

            // 3. Nếu có username và chưa đăng nhập
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 4. Kiểm tra hạn Token
                if (!jwtService.isTokenExpired(jwt)) {
                    
                    // TẠO USER ẢO: Vì Token đã hợp lệ (đúng chữ ký Secret Key),
                    // ta tin tưởng user này là CVA Officer hoặc Admin.
                    // Ta cấp quyền ROLE_CVA_OFFICER để qua được SecurityConfig.
                    UserDetails userDetails = new User(
                        username, 
                        "", 
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_CVA_OFFICER"))
                    );

                    // Xác thực thành công
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Nếu token lỗi, clear context để trả về 403
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
