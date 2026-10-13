package com.cultivar.client.model;

import com.cultivar.Cultivar;
import com.cultivar.client.render.MachineRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

public class CartModel extends EntityModel<MachineRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Cultivar.MODID, "harvester_cart"), "main");
    private final ModelPart[] wheels = new ModelPart[4];
    private final ModelPart load;

    public CartModel(ModelPart root) {
        super(root);
        this.load = root.getChild("load");
        for (int i = 0; i < 4; i++) {
            wheels[i] = root.getChild("wheel" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("chassis", CubeListBuilder.create().texOffs(0, 0).addBox(-7, 16, -6, 14, 4, 12), PartPose.ZERO);
        root.addOrReplaceChild("bin", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-7, 11, -6, 14, 5, 1)
                .texOffs(0, 16).addBox(-7, 11, 5, 14, 5, 1)
                .texOffs(30, 16).addBox(-7, 11, -5, 1, 5, 10)
                .texOffs(30, 16).addBox(6, 11, -5, 1, 5, 10), PartPose.ZERO);
        root.addOrReplaceChild("load", CubeListBuilder.create().texOffs(0, 22).addBox(-6, 12, -5, 12, 4, 10), PartPose.ZERO);
        root.addOrReplaceChild("cutter", CubeListBuilder.create().texOffs(40, 0).addBox(-8, 19, -9, 16, 2, 3), PartPose.ZERO);
        float[][] wheelPos = {{-7.5f, -4}, {7.5f, -4}, {-7.5f, 4}, {7.5f, 4}};
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("wheel" + i, CubeListBuilder.create().texOffs(52, 16).addBox(-1, -2, -2, 2, 4, 4),
                    PartPose.offset(wheelPos[i][0], 22, wheelPos[i][1]));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(MachineRenderState state) {
        super.setupAnim(state);
        load.visible = state.working;
        if (state.moving) {
            for (ModelPart wheel : wheels) {
                wheel.xRot = state.ageInTicks * 0.4f;
            }
        }
    }
}
