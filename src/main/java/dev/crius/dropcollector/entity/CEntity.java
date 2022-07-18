package dev.crius.dropcollector.entity;

import com.google.common.base.Objects;
import dev.crius.dropcollector.entity.item.CItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

@RequiredArgsConstructor @Getter @Setter
public class CEntity {

    private final String name;
    private final String displayName;
    private final Set<CItem> materials;
    private final ItemStack head;
    private final String texture;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CEntity entity = (CEntity) o;
        return Objects.equal(name, entity.name) && Objects.equal(displayName, entity.displayName) && Objects.equal(materials, entity.materials) && Objects.equal(head, entity.head) && Objects.equal(texture, entity.texture);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, displayName, materials, head, texture);
    }
}
