package bakery.repository;

import bakery.entity.Ingredient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IngredientRepositoryTest {

    @Autowired
    private IngredientRepository ingredientRepository;
    @Test
    void saveAndFindIngredient() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Bột mì số 11");
        ingredient.setUnit("kg");
        ingredient.setCurrentStock(new BigDecimal("10.5"));
        ingredient.setLowStockThreshold(new BigDecimal("2.0"));

        Ingredient saved = ingredientRepository.save(ingredient);

        Optional<Ingredient> found = ingredientRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bột mì số 11");
        assertThat(found.get().getCurrentStock()).isEqualByComparingTo(new BigDecimal("10.5"));
    }
}