package com.cultivar.datagen;

import com.cultivar.Cultivar;
import com.cultivar.block.AmendmentHopperBlock;
import com.cultivar.block.BurnPanBlock;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.block.IrrigationLineBlock;
import com.cultivar.block.ManagedSoilBlock;
import com.cultivar.block.PollinationUnitBlock;
import com.cultivar.data.CropSpecies;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CultivarModelProvider extends ModelProvider {
    private static final TextureSlot POST = TextureSlot.create("post");
    private static final TextureSlot PLATE = TextureSlot.create("plate");
    private static final TextureSlot PAN = TextureSlot.create("pan");
    private static final TextureSlot PIPE = TextureSlot.create("pipe");
    private static final TextureSlot PORTAL = TextureSlot.create("portal");

    public CultivarModelProvider(PackOutput output) {
        super(output, Cultivar.MODID);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Cultivar.MODID, path);
    }

    private static Material blockTex(String name) {
        return new Material(id("block/" + name));
    }

    private static ModelTemplate parent(String name, TextureSlot... slots) {
        return ModelTemplates.create(Cultivar.MODID + ":" + name, slots);
    }

    @Override
    protected void registerModels(BlockModelGenerators blocks, ItemModelGenerators items) {
        for (CropSpecies species : CropSpecies.VALUES) {
            crop(blocks, ModBlocks.crop(species), species.id());
            items.generateFlatItem(ModItems.seedPacket(species), ModelTemplates.FLAT_ITEM);
            items.generateFlatItem(ModItems.produce(species), ModelTemplates.FLAT_ITEM);
        }
        crop(blocks, ModBlocks.GRAFT_UNION.get(), "graft_union");

        soil(blocks, ModBlocks.MANAGED_SOIL.get());
        soil(blocks, ModBlocks.BREEDING_PLOT.get());
        for (Block block : new Block[]{ModBlocks.DEPLETED_SOIL.get(), ModBlocks.ISOLATION_SCREEN.get(), ModBlocks.CANOPY_LEAFMASS.get(),
                ModBlocks.CANOPY_PORTAL_FRAME.get()}) {
            blocks.createTrivialCube(block);
        }

        machine(blocks, ModBlocks.GENOME_LAB.get(), "genome_lab", "genome_lab_side", "genome_lab_top", "lab_bottom");
        machine(blocks, ModBlocks.SEED_BANK.get(), "seed_bank", "seed_bank_side", "seed_bank_top", "lab_bottom");
        machine(blocks, ModBlocks.SEED_VAULT_DRAWER.get(), "seed_vault_drawer", "seed_bank_side", "seed_bank_top", "lab_bottom");
        machine(blocks, ModBlocks.MUTAGEN_CHAMBER.get(), "mutagen_chamber", "mutagen_chamber_side", "mutagen_chamber_top", "lab_bottom");
        machine(blocks, ModBlocks.THERMAL_SHOCK_UNIT.get(), "thermal_shock_unit", "thermal_shock_unit_side", "thermal_shock_unit_top",
                "metal_bottom");
        machine(blocks, ModBlocks.CLIMATE_CONTROLLER.get(), "climate_controller", "climate_controller_side", "climate_controller_top",
                "lab_bottom");
        machine(blocks, ModBlocks.COMPOST_BIN.get(), "compost_bin", "compost_bin_side", "compost_bin_top", "wood_bottom");
        machine(blocks, ModBlocks.HARVEST_COLLECTOR.get(), "harvest_collector", "harvest_collector_side", "harvest_collector_top",
                "wood_bottom");
        machine(blocks, ModBlocks.NUTRIENT_SPREADER.get(), "nutrient_spreader", "nutrient_spreader_side", "nutrient_spreader_top",
                "metal_bottom");
        machine(blocks, ModBlocks.HUMIDIFIER.get(), "humidifier", "humidifier_side", "humidifier_top", "lab_bottom");
        machine(blocks, ModBlocks.DEHUMIDIFIER.get(), "dehumidifier", "dehumidifier_side", "dehumidifier_top", "lab_bottom");
        machine(blocks, ModBlocks.PRODUCE_EXCHANGE.get(), "produce_exchange", "produce_exchange_side", "produce_exchange_top", "wood_bottom");
        machine(blocks, ModBlocks.CONTRACT_BOARD.get(), "contract_board", "contract_board_side", "contract_board_top", "wood_bottom");
        machine(blocks, ModBlocks.PROCESSING_STATION.get(), "processing_station", "processing_station_side", "processing_station_top",
                "metal_bottom");
        machine(blocks, ModBlocks.GENE_EDITOR.get(), "gene_editor", "gene_editor_side", "gene_editor_top", "lab_bottom");
        machine(blocks, ModBlocks.SHOW_BENCH.get(), "show_bench", "show_bench_side", "show_bench_top", "wood_bottom");
        hopper(blocks, ModBlocks.LIME_HOPPER.get(), "lime");
        hopper(blocks, ModBlocks.SULFUR_HOPPER.get(), "sulfur");
        Block growLight = ModBlocks.GROW_LIGHT.get();
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(growLight, BlockModelGenerators.plainVariant(
                ModelTemplates.CUBE_BOTTOM_TOP.create(growLight, new TextureMapping().put(TextureSlot.SIDE, blockTex("grow_light_side"))
                        .put(TextureSlot.TOP, blockTex("grow_light_top")).put(TextureSlot.BOTTOM, blockTex("grow_light_bottom")), blocks.modelOutput))));
        blocks.createTrivialBlock(ModBlocks.GREENHOUSE_PANE.get(), TexturedModel.CUBE.updateTexture(TextureMapping::forceAllTranslucent));
        blocks.createAxisAlignedPillarBlock(ModBlocks.CANOPY_BARK.get(), TexturedModel.COLUMN);
        blocks.createCrossBlock(ModBlocks.WILD_VINE_BLOCK.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        blocks.registerSimpleFlatItemModel(ModBlocks.WILD_VINE_BLOCK.get());

        Block unit = ModBlocks.POLLINATION_UNIT.get();
        MultiVariant unitOff = BlockModelGenerators.plainVariant(ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(unit,
                machineTextures("pollination_unit_front", "pollination_unit_side", "pollination_unit_top", "metal_bottom"), blocks.modelOutput));
        MultiVariant unitOn = BlockModelGenerators.plainVariant(ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(unit, "_active",
                machineTextures("pollination_unit_front_active", "pollination_unit_side", "pollination_unit_top", "metal_bottom"),
                blocks.modelOutput));
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(unit)
                .with(BlockModelGenerators.createBooleanModelDispatch(PollinationUnitBlock.ACTIVE, unitOn, unitOff))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));

        Block pan = ModBlocks.BURN_PAN.get();
        ModelTemplate panTemplate = parent("template_pan", PAN);
        MultiVariant panOff = BlockModelGenerators.plainVariant(panTemplate.create(pan, new TextureMapping().put(PAN, blockTex("burn_pan")), blocks.modelOutput));
        MultiVariant panOn = BlockModelGenerators.plainVariant(panTemplate.createWithSuffix(pan, "_lit",
                new TextureMapping().put(PAN, blockTex("burn_pan_lit")), blocks.modelOutput));
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(pan).with(BlockModelGenerators.createBooleanModelDispatch(BurnPanBlock.LIT, panOn, panOff)));

        Block line = ModBlocks.IRRIGATION_LINE.get();
        ModelTemplate pipeTemplate = parent("template_pipe", PIPE);
        MultiVariant lineDry = BlockModelGenerators.plainVariant(pipeTemplate.create(line, new TextureMapping().put(PIPE, blockTex("irrigation_line")),
                blocks.modelOutput));
        MultiVariant lineFed = BlockModelGenerators.plainVariant(pipeTemplate.createWithSuffix(line, "_fed",
                new TextureMapping().put(PIPE, blockTex("irrigation_line_fed")), blocks.modelOutput));
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(line).with(BlockModelGenerators.createBooleanModelDispatch(IrrigationLineBlock.FED, lineFed, lineDry)));

        Block sprinkler = ModBlocks.SPRINKLER.get();
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(sprinkler, BlockModelGenerators.plainVariant(
                parent("template_sprinkler", TextureSlot.SIDE, TextureSlot.TOP).create(sprinkler,
                        new TextureMapping().put(TextureSlot.SIDE, blockTex("sprinkler")).put(TextureSlot.TOP, blockTex("sprinkler_top")), blocks.modelOutput))));
        Block marker = ModBlocks.TRIAL_MARKER.get();
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(marker, BlockModelGenerators.plainVariant(
                parent("template_post", POST, PLATE).create(marker,
                        new TextureMapping().put(POST, blockTex("trial_marker")).put(PLATE, blockTex("trial_marker_plate")), blocks.modelOutput))));
        Block portal = ModBlocks.CANOPY_PORTAL.get();
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(portal, BlockModelGenerators.plainVariant(
                parent("template_portal", PORTAL).create(portal, new TextureMapping().put(PORTAL, blockTex("canopy_portal").withForceTranslucent(true)),
                        blocks.modelOutput))));

        for (Item item : new Item[]{ModItems.GENOME_CARD.get(), ModItems.ISOLATION_BAG.get(), ModItems.COMPOST.get(), ModItems.LIME_POWDER.get(),
                ModItems.SULFUR_POWDER.get(), ModItems.NUTRIENT_BLEND_N.get(), ModItems.NUTRIENT_BLEND_P.get(), ModItems.NUTRIENT_BLEND_K.get(),
                ModItems.MUTAGEN_VIAL.get(), ModItems.FIELD_MANUAL.get(), ModItems.FIELD_NOTEBOOK.get(), ModItems.PEDIGREE_SHEET.get(), ModItems.TRIAL_TAG.get(),
                ModItems.WILD_SEED.get(), ModItems.CANOPY_RESIN.get(), ModItems.CANOPY_KEY.get(), ModItems.BLIGHT_SAMPLE.get(),
                ModItems.GREEN_MANURE.get(), ModItems.FIELD_MEAL.get(), ModItems.SOY_OIL.get(), ModItems.LINEN_CANVAS.get(), ModItems.STARCH.get(),
                ModItems.BIOPLASTIC.get(), ModItems.PICKLED_BRASSICA.get(), ModItems.GOURD_PUREE.get(), ModItems.BIOFUEL_CAKE.get(),
                ModItems.RATION_PACK.get(), ModItems.EDIT_REAGENT.get(), ModItems.GENE_EDITOR_BLUEPRINT.get(), ModItems.RIBBON_BRONZE.get(),
                ModItems.RIBBON_SILVER.get(), ModItems.RIBBON_GOLD.get()}) {
            items.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
        for (Item item : new Item[]{ModItems.SOIL_PROBE.get(), ModItems.HAND_LENS.get(), ModItems.POLLINATION_BRUSH.get(), ModItems.GRAFTING_KNIFE.get()}) {
            items.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
        }
    }

    private static void crop(BlockModelGenerators blocks, Block block, String texturePrefix) {
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (int stage = 0; stage <= CultivarCropBlock.MAX_STAGE; stage++) {
            for (int vigor = 0; vigor <= 2; vigor++) {
                for (int density = 0; density <= 2; density++) {
                    ModelTemplate template = parent("crop_v" + vigor + "_d" + density, TextureSlot.CROP);
                    Identifier model = template.createWithSuffix(block, "_s" + stage + "_v" + vigor + "_d" + density,
                            TextureMapping.crop(blockTex(texturePrefix + "_stage" + stage)), blocks.modelOutput);
                    generator.with(BlockModelGenerators.condition().term(CultivarCropBlock.STAGE, stage).term(CultivarCropBlock.VIGOR, vigor)
                            .term(CultivarCropBlock.DENSITY, density), BlockModelGenerators.plainVariant(model));
                }
            }
        }
        for (int vigor = 0; vigor <= 2; vigor++) {
            generator.with(BlockModelGenerators.condition().term(CultivarCropBlock.BLIGHTED, true).term(CultivarCropBlock.VIGOR, vigor),
                    BlockModelGenerators.plainVariant(id("block/blight_marker_v" + vigor)));
        }
        generator.with(BlockModelGenerators.condition().term(CultivarCropBlock.BAGGED, true),
                BlockModelGenerators.plainVariant(id("block/isolation_bag_overlay")));
        blocks.blockStateOutput.accept(generator);
    }

    private static TextureMapping machineTextures(String front, String side, String top, String bottom) {
        return new TextureMapping().put(TextureSlot.FRONT, blockTex(front)).put(TextureSlot.SIDE, blockTex(side))
                .put(TextureSlot.TOP, blockTex(top)).put(TextureSlot.BOTTOM, blockTex(bottom)).put(TextureSlot.PARTICLE, blockTex(side));
    }

    private static void machine(BlockModelGenerators blocks, Block block, String name, String side, String top, String bottom) {
        MultiVariant model = BlockModelGenerators.plainVariant(ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(block,
                machineTextures(name + "_front", side, top, bottom), blocks.modelOutput));
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, model).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private static void hopper(BlockModelGenerators blocks, Block block, String kind) {
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(AmendmentHopperBlock.LEVEL).generate(level -> BlockModelGenerators.plainVariant(level == 0
                        ? ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(block,
                        machineTextures(kind + "_hopper_front_0", "hopper_side", kind + "_hopper_top", "metal_bottom"), blocks.modelOutput)
                        : ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(block, "_level_" + level,
                        machineTextures(kind + "_hopper_front_" + level, "hopper_side", kind + "_hopper_top", "metal_bottom"),
                        blocks.modelOutput))))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private static void soil(BlockModelGenerators blocks, Block block) {
        Material side = TextureMapping.getBlockTexture(block, "_side");
        MultiVariant dry = BlockModelGenerators.plainVariant(ModelTemplates.FARMLAND.create(block,
                new TextureMapping().put(TextureSlot.DIRT, side).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top")), blocks.modelOutput));
        MultiVariant moist = BlockModelGenerators.plainVariant(ModelTemplates.FARMLAND.createWithSuffix(block, "_moist",
                new TextureMapping().put(TextureSlot.DIRT, side).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top_moist")), blocks.modelOutput));
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(
                PropertyDispatch.initial(ManagedSoilBlock.MOISTURE).generate(m -> m >= 7 ? moist : dry)));
    }
}
