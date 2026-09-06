package bakery.service;

import bakery.dto.IngredientRequest;
import bakery.dto.IngredientResponse;
import bakery.entity.Ingredient;
import bakery.repository.IngredientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<IngredientResponse> getAll() {
        return ingredientRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public IngredientResponse getById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu"));
        return toResponse(ingredient);
    }

    public IngredientResponse create(IngredientRequest request) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setUnit(request.getUnit());
        ingredient.setCurrentStock(request.getCurrentStock());
        ingredient.setLowStockThreshold(request.getLowStockThreshold());

        Ingredient saved = ingredientRepository.save(ingredient);
        return toResponse(saved);
    }

    public IngredientResponse update(Long id, IngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu"));

        ingredient.setName(request.getName());
        ingredient.setUnit(request.getUnit());
        ingredient.setCurrentStock(request.getCurrentStock());
        ingredient.setLowStockThreshold(request.getLowStockThreshold());

        Ingredient updated = ingredientRepository.save(ingredient);
        return toResponse(updated);
    }

    public void delete(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy nguyên liệu");
        }
        ingredientRepository.deleteById(id);
    }

    private IngredientResponse toResponse(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getUnit(),
                ingredient.getCurrentStock(),
                ingredient.getLowStockThreshold()
        );
    }
}