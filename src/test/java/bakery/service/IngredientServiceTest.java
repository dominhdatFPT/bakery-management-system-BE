package bakery.service;

import bakery.dto.IngredientRequest;
import bakery.dto.IngredientResponse;
import bakery.entity.Ingredient;
import bakery.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    private IngredientRequest buildValidRequest() {
        IngredientRequest request = new IngredientRequest();
        request.setName("Bột mì");
        request.setUnit("kg");
        request.setCurrentStock(new BigDecimal("50"));
        request.setLowStockThreshold(new BigDecimal("10"));
        return request;
    }

    @Test
    void create_hopLe_traVeResponseDungDuLieu() {
        IngredientRequest request = buildValidRequest();

        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> {
            Ingredient saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        IngredientResponse response = ingredientService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Bột mì");
        assertThat(response.getUnit()).isEqualTo("kg");
        assertThat(response.getCurrentStock()).isEqualByComparingTo("50");
        assertThat(response.getLowStockThreshold()).isEqualByComparingTo("10");
        verify(ingredientRepository).save(any(Ingredient.class));
    }

    @Test
    void update_khongTonTai_nemException() {
        Long missingId = 99L;
        when(ingredientRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.update(missingId, buildValidRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Không tìm thấy nguyên liệu");

        verify(ingredientRepository, never()).save(any(Ingredient.class));
    }

    @Test
    void update_hopLe_capNhatDungCacField() {
        Long id = 1L;
        Ingredient existing = new Ingredient();
        existing.setId(id);
        existing.setName("Bột mì cũ");
        existing.setUnit("kg");
        existing.setCurrentStock(new BigDecimal("20"));
        existing.setLowStockThreshold(new BigDecimal("5"));

        IngredientRequest request = new IngredientRequest();
        request.setName("Bột mì mới");
        request.setUnit("kg");
        request.setCurrentStock(new BigDecimal("80"));
        request.setLowStockThreshold(new BigDecimal("10"));

        when(ingredientRepository.findById(id)).thenReturn(Optional.of(existing));
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IngredientResponse response = ingredientService.update(id, request);

        assertThat(response.getName()).isEqualTo("Bột mì mới");
        assertThat(response.getCurrentStock()).isEqualByComparingTo("80");
    }

    @Test
    void delete_khongTonTai_nemException() {
        Long missingId = 99L;
        when(ingredientRepository.existsById(missingId)).thenReturn(false);

        assertThatThrownBy(() -> ingredientService.delete(missingId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Không tìm thấy nguyên liệu");

        verify(ingredientRepository, never()).deleteById(any());
    }

    @Test
    void delete_hopLe_goiDeleteById() {
        Long id = 1L;
        when(ingredientRepository.existsById(id)).thenReturn(true);

        ingredientService.delete(id);

        verify(ingredientRepository).deleteById(id);
    }
}
