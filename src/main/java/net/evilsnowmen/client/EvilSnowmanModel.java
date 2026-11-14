package net.evilsnowmen.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * Та же форма, что у обычного снеговика, но тыква, свой вырез и пень
 * нарисованы прямо в текстуре (128x64), поэтому вырез можно сделать злым.
 */
public class EvilSnowmanModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart head;
    private final ModelPart upperBody;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public EvilSnowmanModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.upperBody = root.getChild("upper_body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation shrink = new CubeDeformation(-0.5F);

        // Голова + тыква + пенёк (всё вращается вместе с головой).
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, shrink)
                        .texOffs(64, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(64, 24).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        CubeListBuilder arm = CubeListBuilder.create()
                .texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 12.0F, 2.0F, 2.0F, shrink);
        root.addOrReplaceChild("left_arm", arm,
                PartPose.offsetAndRotation(5.0F, 6.0F, 1.0F, 0.0F, 0.0F, 1.0F));
        root.addOrReplaceChild("right_arm", arm,
                PartPose.offsetAndRotation(-5.0F, 6.0F, -1.0F, 0.0F, (float) Math.PI, 1.0F));

        root.addOrReplaceChild("upper_body",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, shrink),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        root.addOrReplaceChild("lower_body",
                CubeListBuilder.create()
                        .texOffs(0, 36).addBox(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F, shrink),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float yaw = state.yRot * Mth.DEG_TO_RAD;
        this.head.yRot = yaw;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.upperBody.yRot = yaw * 0.25F;

        float sin = Mth.sin(this.upperBody.yRot);
        float cos = Mth.cos(this.upperBody.yRot);
        this.rightArm.yRot = this.upperBody.yRot;
        this.leftArm.yRot = this.upperBody.yRot + (float) Math.PI;
        this.rightArm.x = cos * -5.0F;
        this.rightArm.z = sin * 5.0F;
        this.leftArm.x = cos * 5.0F;
        this.leftArm.z = sin * -5.0F;

        // Злобно машет «руками»-ветками.
        float wave = Mth.sin(state.ageInTicks * 0.35F) * 0.12F;
        this.rightArm.zRot = 1.0F + wave;
        this.leftArm.zRot = 1.0F - wave;
    }
}
