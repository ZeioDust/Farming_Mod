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

public class DroneModel extends EntityModel<MachineRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Cultivar.MODID, "pollinator_drone"), "main");
    private final ModelPart[] rotors = new ModelPart[4];

    public DroneModel(ModelPart root) {
        super(root);
        for (int i = 0; i < 4; i++) {
            rotors[i] = root.getChild("rotor" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3, 19, -3, 6, 3, 6)
                .texOffs(0, 9).addBox(-1, 22, -1, 2, 1, 2), PartPose.ZERO);
        root.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(24, 0).addBox(-7, 20, -0.5f, 14, 1, 1)
                .texOffs(24, 2).addBox(-0.5f, 20, -7, 1, 1, 14), PartPose.ZERO);
        int[][] tips = {{-6, -0}, {6, 0}, {0, -6}, {0, 6}};
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("rotor" + i, CubeListBuilder.create().texOffs(0, 16).addBox(-2.5f, -0.25f, -0.5f, 5, 0.5f, 1)
                    .texOffs(0, 18).addBox(-0.5f, -0.25f, -2.5f, 1, 0.5f, 5), PartPose.offset(tips[i][0], 19.5f, tips[i][1]));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(MachineRenderState state) {
        super.setupAnim(state);
        for (int i = 0; i < rotors.length; i++) {
            rotors[i].yRot = state.ageInTicks * (i % 2 == 0 ? 1.6f : -1.6f);
        }
    }
}
