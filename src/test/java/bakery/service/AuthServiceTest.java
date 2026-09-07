package bakery.service;

import bakery.dto.LoginRequest;
import bakery.dto.RegisterRequest;
import bakery.entity.User;
import bakery.entity.UserRole;
import bakery.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_thanhCong_traVeUserDaLuu() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@bakery.test");
        request.setPassword("123456");

        when(userRepository.findByEmail("new@bakery.test")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(request);

        assertThat(result.getEmail()).isEqualTo("new@bakery.test");
        assertThat(result.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(result.getRole()).isEqualTo(UserRole.OWNER);
        assertThat(result.getIsActive()).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_trungEmail_nemException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@bakery.test");
        request.setPassword("123456");

        User existingUser = new User();
        existingUser.setEmail("existing@bakery.test");
        when(userRepository.findByEmail("existing@bakery.test")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email đã được sử dụng");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_saiMatKhau_nemException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("owner@bakery.test");
        request.setPassword("wrong-password");

        User user = new User();
        user.setEmail("owner@bakery.test");
        user.setPasswordHash("hashed-real-password");

        when(userRepository.findByEmail("owner@bakery.test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-real-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sai email hoặc mật khẩu");
    }
}
