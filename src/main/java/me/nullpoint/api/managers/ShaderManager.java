/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  ladysnake.satin.api.managed.ManagedShaderEffect
 *  ladysnake.satin.api.managed.ShaderEffectManager
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.Framebuffer
 *  net.minecraft.client.gl.PostEffectProcessor
 *  net.minecraft.util.Identifier
 *  org.jetbrains.annotations.NotNull
 */
package me.nullpoint.api.managers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import me.nullpoint.api.interfaces.IShaderEffect;
import me.nullpoint.api.utils.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ShaderManager
implements Wrapper {
    private static final List<RenderTask> tasks = new ArrayList<RenderTask>();
    private MyFramebuffer shaderBuffer;
    public float time = 0.0f;
    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect SMOKE_OUTLINE;
    public static ManagedShaderEffect GRADIENT_OUTLINE;
    public static ManagedShaderEffect SNOW_OUTLINE;
    public static ManagedShaderEffect FLOW_OUTLINE;
    public static ManagedShaderEffect RAINBOW_OUTLINE;
    public static ManagedShaderEffect DEFAULT;
    public static ManagedShaderEffect SMOKE;
    public static ManagedShaderEffect GRADIENT;
    public static ManagedShaderEffect SNOW;
    public static ManagedShaderEffect FLOW;
    public static ManagedShaderEffect RAINBOW;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }

    public void renderShaders() {
        tasks.forEach(t -> this.applyShader(t.task(), t.shader()));
        tasks.clear();
    }

    public void applyFlow(Runnable runnable) {
        if (this.fullNullCheck()) {
            return;
        }
        Framebuffer MCBuffer = MinecraftClient.getInstance().getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        if (this.shaderBuffer.textureWidth != MCBuffer.textureWidth || this.shaderBuffer.textureHeight != MCBuffer.textureHeight) {
            this.shaderBuffer.resize(MCBuffer.textureWidth, MCBuffer.textureHeight, false);
        }
        GlStateManager._glBindFramebuffer(36009, this.shaderBuffer.fbo);
        this.shaderBuffer.beginWrite(true);
        runnable.run();
        this.shaderBuffer.endWrite();
        GlStateManager._glBindFramebuffer(36009, MCBuffer.fbo);
        MCBuffer.beginWrite(false);
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        PostEffectProcessor effect = FLOW.getShaderEffect();
        if (effect != null) {
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufIn", this.shaderBuffer);
        }
        Framebuffer outBuffer = FLOW.getShaderEffect().getSecondaryTarget("bufOut");
        FLOW.setUniformValue("resolution", (float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
        FLOW.setUniformValue("time", this.time);
        FLOW.render(mc.getTickDelta());
        this.time += 0.01f;
        this.shaderBuffer.clear(false);
        mainBuffer.beginWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.backupProjectionMatrix();
        outBuffer.draw(outBuffer.textureWidth, outBuffer.textureHeight, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public void applyShader(Runnable runnable, Shader mode) {
        if (this.fullNullCheck()) {
            return;
        }
        Framebuffer MCBuffer = MinecraftClient.getInstance().getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        if (this.shaderBuffer.textureWidth != MCBuffer.textureWidth || this.shaderBuffer.textureHeight != MCBuffer.textureHeight) {
            this.shaderBuffer.resize(MCBuffer.textureWidth, MCBuffer.textureHeight, false);
        }
        GlStateManager._glBindFramebuffer(36009, this.shaderBuffer.fbo);
        this.shaderBuffer.beginWrite(true);
        runnable.run();
        this.shaderBuffer.endWrite();
        GlStateManager._glBindFramebuffer(36009, MCBuffer.fbo);
        MCBuffer.beginWrite(false);
        ManagedShaderEffect shader = this.getShader(mode);
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        PostEffectProcessor effect = shader.getShaderEffect();
        if (effect != null) {
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufIn", this.shaderBuffer);
        }
        Framebuffer outBuffer = shader.getShaderEffect().getSecondaryTarget("bufOut");
        this.setupShader(mode, shader);
        this.shaderBuffer.clear(false);
        mainBuffer.beginWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.backupProjectionMatrix();
        outBuffer.draw(outBuffer.textureWidth, outBuffer.textureHeight, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public ManagedShaderEffect getShader(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT;
            case Smoke -> SMOKE;
            case Snow -> SNOW;
            case Flow -> FLOW;
            case Rainbow -> RAINBOW;
            default -> DEFAULT;
        };
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT_OUTLINE;
            case Smoke -> SMOKE_OUTLINE;
            case Snow -> SNOW_OUTLINE;
            case Flow -> FLOW_OUTLINE;
            case Rainbow -> RAINBOW_OUTLINE;
            default -> DEFAULT_OUTLINE;
        };
    }

    public void setupShader(Shader shader, ManagedShaderEffect effect) {
        me.nullpoint.mod.modules.impl.render.Shader shaderChams = me.nullpoint.mod.modules.impl.render.Shader.INSTANCE;
        if (shader == Shader.Rainbow) {
            effect.setUniformValue("alpha2", (float)shaderChams.fill.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("radius", shaderChams.radius.getValueFloat());
            effect.setUniformValue("quality", shaderChams.smoothness.getValueFloat());
            effect.setUniformValue("divider", shaderChams.divider.getValueFloat());
            effect.setUniformValue("maxSample", shaderChams.maxSample.getValueFloat());
            effect.setUniformValue("resolution", (float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getTickDelta());
            this.time += (float)shaderChams.speed.getValue() * 0.002f;
        } else if (shader == Shader.Gradient) {
            effect.setUniformValue("alpha2", (float)shaderChams.fill.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("oct", (int)shaderChams.octaves.getValue());
            effect.setUniformValue("radius", shaderChams.radius.getValueFloat());
            effect.setUniformValue("quality", shaderChams.smoothness.getValueFloat());
            effect.setUniformValue("divider", shaderChams.divider.getValueFloat());
            effect.setUniformValue("maxSample", shaderChams.maxSample.getValueFloat());
            effect.setUniformValue("factor", (float)shaderChams.factor.getValue());
            effect.setUniformValue("moreGradient", (float)shaderChams.gradient.getValue());
            effect.setUniformValue("resolution", (float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getTickDelta());
            this.time += (float)shaderChams.speed.getValue() * 0.002f;
        } else if (shader == Shader.Smoke) {
            effect.setUniformValue("alpha1", (float)shaderChams.fill.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("radius", shaderChams.radius.getValueFloat());
            effect.setUniformValue("quality", shaderChams.smoothness.getValueFloat());
            effect.setUniformValue("divider", shaderChams.divider.getValueFloat());
            effect.setUniformValue("maxSample", shaderChams.maxSample.getValueFloat());
            effect.setUniformValue("first", (float)shaderChams.smoke1.getValue().getRed() / 255.0f, (float)shaderChams.smoke1.getValue().getGreen() / 255.0f, (float)shaderChams.smoke1.getValue().getBlue() / 255.0f, (float)shaderChams.smoke1.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("second", (float)shaderChams.smoke2.getValue().getRed() / 255.0f, (float)shaderChams.smoke2.getValue().getGreen() / 255.0f, (float)shaderChams.smoke2.getValue().getBlue() / 255.0f);
            effect.setUniformValue("third", (float)shaderChams.smoke3.getValue().getRed() / 255.0f, (float)shaderChams.smoke3.getValue().getGreen() / 255.0f, (float)shaderChams.smoke3.getValue().getBlue() / 255.0f);
            effect.setUniformValue("oct", (int)shaderChams.octaves.getValue());
            effect.setUniformValue("resolution", (float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getTickDelta());
            this.time += (float)shaderChams.speed.getValue() * 0.002f;
        } else if (shader == Shader.Solid) {
            effect.setUniformValue("mixFactor", (float)shaderChams.fill.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("minAlpha", shaderChams.alpha.getValueFloat() / 255.0f);
            effect.setUniformValue("radius", shaderChams.radius.getValueFloat());
            effect.setUniformValue("quality", shaderChams.smoothness.getValueFloat());
            effect.setUniformValue("divider", shaderChams.divider.getValueFloat());
            effect.setUniformValue("maxSample", shaderChams.maxSample.getValueFloat());
            effect.setUniformValue("color", (float)shaderChams.fill.getValue().getRed() / 255.0f, (float)shaderChams.fill.getValue().getGreen() / 255.0f, (float)shaderChams.fill.getValue().getBlue() / 255.0f);
            effect.setUniformValue("resolution", (float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
            effect.render(mc.getTickDelta());
        } else if (shader == Shader.Snow) {
            effect.setUniformValue("color", (float)shaderChams.fill.getValue().getRed() / 255.0f, (float)shaderChams.fill.getValue().getGreen() / 255.0f, (float)shaderChams.fill.getValue().getBlue() / 255.0f, (float)shaderChams.fill.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("radius", shaderChams.radius.getValueFloat());
            effect.setUniformValue("quality", shaderChams.smoothness.getValueFloat());
            effect.setUniformValue("divider", shaderChams.divider.getValueFloat());
            effect.setUniformValue("maxSample", shaderChams.maxSample.getValueFloat());
            effect.setUniformValue("resolution", (float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getTickDelta());
            this.time += (float)shaderChams.speed.getValue() * 0.002f;
        } else if (shader == Shader.Flow) {
            effect.setUniformValue("mixFactor", (float)shaderChams.fill.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("radius", shaderChams.radius.getValueFloat());
            effect.setUniformValue("quality", shaderChams.smoothness.getValueFloat());
            effect.setUniformValue("divider", shaderChams.divider.getValueFloat());
            effect.setUniformValue("maxSample", shaderChams.maxSample.getValueFloat());
            effect.setUniformValue("resolution", (float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getTickDelta());
            this.time += (float)shaderChams.speed.getValue() * 0.002f;
        }
    }

    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/outline.json"));
        SMOKE = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/smoke.json"));
        GRADIENT = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/gradient.json"));
        SNOW = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/snow.json"));
        FLOW = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/flow.json"));
        RAINBOW = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/rainbow.json"));
        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/outline.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/smoke.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        GRADIENT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/gradient.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        SNOW_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/snow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        FLOW_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/flow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        RAINBOW_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/rainbow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).nullpoint_nextgen_master$addFakeTargetHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
    }

    public boolean fullNullCheck() {
        if (GRADIENT == null || SMOKE == null || DEFAULT == null || FLOW == null || RAINBOW == null || GRADIENT_OUTLINE == null || SMOKE_OUTLINE == null || DEFAULT_OUTLINE == null || FLOW_OUTLINE == null || RAINBOW_OUTLINE == null || this.shaderBuffer == null) {
            if (mc.getFramebuffer() == null) {
                return true;
            }
            this.shaderBuffer = new MyFramebuffer(ShaderManager.mc.getFramebuffer().textureWidth, ShaderManager.mc.getFramebuffer().textureHeight);
            this.reloadShaders();
            return true;
        }
        return false;
    }

    public record RenderTask(Runnable task, Shader shader) {
    }

    public enum Shader {
        Solid,
        Smoke,
        Gradient,
        Snow,
        Flow,
        Rainbow

    }

    public static class MyFramebuffer
    extends Framebuffer {
        public MyFramebuffer(int width, int height) {
            super(false);
            RenderSystem.assertOnRenderThreadOrInit();
            this.resize(width, height, true);
            this.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }
    }
}

