# NOTES.md — Nhật ký vấn đề phát sinh

Ghi lại các lỗi/vấn đề gặp phải trong quá trình làm project, kèm nguyên nhân và cách fix, để ôn tập lại sau này.

## 1. Mockito không có sẵn trong Spring Boot 4.1.1 (Ngày 7)

**Hiện tượng:** Viết unit test dùng `@Mock`, `@InjectMocks` nhưng biên dịch lỗi vì không tìm thấy các class của Mockito.

**Nguyên nhân:** Spring Boot 4.1.1 dùng các starter test theo module riêng (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`,...) thay vì 1 starter to `spring-boot-starter-test` như bản cũ. Các starter mới này **không** kèm theo Mockito.

**Cách fix:** Thêm thủ công 2 dependency vào `pom.xml` (scope `test`):
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 2. Lỗi biên dịch test: `package bakery.dto does not exist` (Ngày 7)

**Hiện tượng:** Chạy `mvnw test` báo lỗi không tìm thấy package `bakery.dto`, dù code chính vẫn chạy bình thường.

**Nguyên nhân:** Thư mục vật lý tên là `DTO` (viết hoa) nhưng khai báo trong code là `package bakery.dto;` (viết thường). Compile code chính (main) thì Windows/Java "bỏ qua" được sự khác biệt này, nhưng bước **test-compile** lại so khớp nghiêm ngặt với cấu trúc thư mục trong `target/classes`, nên bị lỗi.

**Cách fix:** Đổi tên thư mục `DTO` → `dto` cho khớp với tên package. Trên Windows (NTFS không phân biệt hoa/thường nhưng vẫn giữ nguyên chữ), phải đổi tên 2 bước: `DTO` → `dto_tmp` → `dto`.

**Bài học:** Tên thư mục Java LUÔN phải khớp chính xác (kể cả hoa/thường) với tên package khai báo trong file `.java`.

---

## 3. "403 Forbidden" khi gọi `/api/auth/register` dù đã cấu hình `permitAll()` (Ngày 8)

**Hiện tượng:** Gọi `POST /api/auth/register` qua Postman/curl luôn bị 403, dù `SecurityConfig` đã có:
```java
.requestMatchers("/api/auth/**").permitAll()
```

**Quá trình tìm nguyên nhân:** Loại trừ lần lượt: JWT filter (không phải, filter luôn cho request đi qua), Postman/curl (không phải, cả 2 đều bị 403 giống nhau), tiến trình cũ chưa tắt (không phải, tiến trình mới), build cũ chưa cập nhật (không phải, kiểm tra bytecode thấy đúng).

**Nguyên nhân thật sự:** Bật log debug của Spring Security (`logging.level.org.springframework.security: DEBUG`) thì thấy: request `/api/auth/register` được `permitAll()` xử lý ĐÚNG (log ghi rõ "Secured POST /api/auth/register"). Nhưng ngay sau đó, `AuthService.register()` ném ra `RuntimeException("Email đã được sử dụng")` vì email test đã đăng ký từ trước. Vì code **không có nơi nào bắt exception này** (không có `@ControllerAdvice`), Spring Boot tự động forward request sang `/error` để tạo trang lỗi mặc định. Request `/error` này là **một request MỚI, riêng biệt**, và nó lại phải đi qua Spring Security lần nữa — nhưng `/error` không nằm trong `permitAll()`, nên bị chặn → trả về 403.

**Tóm gọn:** 403 không phải do bug ở security, mà là **hậu quả phụ** của một exception nghiệp vụ (email trùng) không được xử lý đúng cách.

**Cách fix tạm thời:** Đăng ký bằng email hoàn toàn mới, chưa từng dùng.

**Cách fix triệt để (đã áp dụng ngày 8):** Thêm class `GlobalExceptionHandler` dùng `@RestControllerAdvice` + `@ExceptionHandler(RuntimeException.class)` để bắt mọi `RuntimeException` chưa được xử lý ngay tại tầng controller, trả về `400 Bad Request` kèm message rõ ràng — thay vì để Spring tự forward sang `/error` rồi bị Security chặn.

**Bài học:** Luôn cần một nơi xử lý exception tập trung (`@ControllerAdvice`) ngay từ đầu, để tránh lỗi HTTP status bị sai lệch (403 giả) khi có exception chưa được bắt.

---

## 4. CORS chặn request từ FE (React, `localhost:5173`) sang BE (`localhost:8080`) (Ngày 8)

**Hiện tượng:** Trang Login (React) gọi API `/api/auth/login` bị lỗi, Console của Chrome báo:
```
Access to XMLHttpRequest at 'http://localhost:8080/api/auth/login' from origin
'http://localhost:5173' has been blocked by CORS policy: ... No
'Access-Control-Allow-Origin' header is present.
```

**Nguyên nhân:** CORS (Cross-Origin Resource Sharing) là cơ chế bảo mật của trình duyệt: khi trang web ở origin A (`localhost:5173`) gọi API ở origin B (`localhost:8080`), trình duyệt sẽ chặn lại nếu server B không xác nhận "cho phép A gọi". Postman/curl không bị ảnh hưởng vì không phải trình duyệt — đó là lý do trước đó test bằng Postman không thấy lỗi này.

**Cách fix:** Thêm cấu hình CORS vào `SecurityConfig.java`:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:5173"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```
Và bật nó lên trong filter chain: `http.cors(cors -> {})...`

**Bài học:** Bất cứ khi nào FE và BE chạy ở 2 port khác nhau (rất phổ biến khi dev local), phải cấu hình CORS ở BE để trình duyệt cho phép gọi qua lại.

---

## 5. Dọn dẹp code sau khi debug xong (Ngày 8, review tuần 1)

Sau khi tìm ra nguyên nhân 2 lỗi trên, đã dọn lại:
- Xóa dòng `System.out.println("[AUTH-DEBUG] ...")` còn sót lại trong `AuthController.register()`.
- Xóa cấu hình log debug tạm thời (`logging.level.org.springframework.security: DEBUG`, `logging.file.name: app-startup.log`) trong `application.yaml`.
- Gỡ đoạn ghi file `jwt-debug.log` tạm thời trong `JwtAuthenticationFilter.java`.
- Sửa lỗi format nhỏ ở `UserRole.java` (thừa khoảng trắng, thụt lề lệch).

**Bài học chung:** Code debug tạm thời (println, ghi log ra file...) rất hữu ích lúc tìm lỗi, nhưng phải nhớ dọn lại sau khi xong — nếu không sẽ để lộ thông tin nhạy cảm (như email người dùng) ra console/log, hoặc làm rối code về sau.
