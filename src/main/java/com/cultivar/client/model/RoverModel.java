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

public class RoverModel extends EntityModel<MachineRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Cultivar.MODID, "soil_probe_rover"), "main");
    private final ModelPart probe;

    public RoverModel(ModelPart root) {
        super(root);
        this.probe = root.getChild("probe");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("hull", CubeListBuilder.create().texOffs(0, 0).addBox(-4, 19, -5, 8, 3, 10), PartPose.ZERO);
        root.addOrReplaceChild("tracks", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-6, 21, -6, 2, 3, 12)
                .texOffs(0, 14).addBox(4, 21, -6, 2, 3, 12), PartPose.ZERO);
        root.addOrReplaceChild("mast", CubeListBuilder.create().texOffs(36, 0).addBox(-0.5f, 14, 2, 1, 5, 1)
                .texOffs(40, 0).addBox(-1.5f, 13, 1, 3, 1, 3), PartPose.ZERO);
        root.addOrReplaceChild("probe", CubeListBuilder.create().texOffs(36, 8).addBox(-0.5f, 0, -0.5f, 1, 4, 1),
                PartPose.offset(0, 21, -4));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(MachineRenderState state) {
        super.setupAnim(state);
        probe.y = state.working ? 23.5f : 21f;
    }
}
