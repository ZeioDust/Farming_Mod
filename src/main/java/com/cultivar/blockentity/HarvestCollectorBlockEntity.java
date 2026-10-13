package com.cultivar.blockentity;

import com.cultivar.entity.HarvesterCartEntity;
import com.cultivar.item.Planting;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModEntities;
import com.cultivar.world.LevelState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class HarvestCollectorBlockEntity extends InventoryBlockEntity implements TickingMachine {
    public static final int RADIUS = 6;
    public static final int SIZE = 18;
    public static final int INTERVAL = 200;
    public static final int MAX_ROUTE = 32;

    public static final int CART_TIMEOUT = HarvesterCartEntity.MAX_AGE + 100;

    private java.util.@org.jspecify.annotations.Nullable UUID cart;
    private long cartSentAt;

    public HarvestCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HARVEST_COLLECTOR.get(), pos, state, SIZE);
    }

    public void cartReturned() {
        cart = null;
        markUpdated();
    }

    private boolean cartOut(ServerLevel level) {
        if (cart == null) {
            return false;
        }
        long age = level.getGameTime() - cartSentAt;
        if (age > CART_TIMEOUT || (age > 20 && level.getEntity(cart) == null)) {
            cart = null;
            setChanged();
            return false;
        }
        return true;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        int free = freeSlots();
        if ((level.getGameTime() + pos.asLong()) % INTERVAL != 0 || free < 2 * HarvesterCartEntity.STACKS_PER_HARVEST || cartOut(level)) {
            return;
        }
        List<BlockPos> route = new ArrayList<>();
        LevelState index = LevelState.get(level);
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RADIUS, -2, -RADIUS), pos.offset(RADIUS, 2, RADIUS))) {
            if (index.hasCrop(p.asLong()) && Planting.isMature(level.getBlockState(p))
                    && level.getBlockEntity(p) instanceof CropBlockEntity crop && !crop.isBlighted()) {
                route.add(p.immutable());
                if (route.size() >= MAX_ROUTE) {
                    break;
                }
            }
        }
        if (route.isEmpty()) {
            return;
        }
        route.sort(Comparator.<BlockPos>comparingInt(BlockPos::getZ).thenComparingInt(p -> (p.getZ() & 1) == 0 ? p.getX() : -p.getX()));
        HarvesterCartEntity entity = ModEntities.HARVESTER_CART.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return;
        }
        entity.assign(pos, route, free);
        level.addFreshEntity(entity);
        cart = entity.getUUID();
        cartSentAt = level.getGameTime();
        setChanged();
    }

    private int freeSlots() {
        int free = 0;
        for (var stack : items) {
            if (stack.isEmpty()) {
                free++;
            }
        }
        return free;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("cart", net.minecraft.core.UUIDUtil.CODEC, cart);
        output.putLong("cart_sent_at", cartSentAt);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        cart = input.read("cart", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        cartSentAt = input.getLongOr("cart_sent_at", 0L);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return false;
    }
}
