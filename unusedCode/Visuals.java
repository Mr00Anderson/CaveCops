package com.github.tommyettinger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.tommyettinger.colorful.Palette;
import squidpony.squidmath.NumberTools;

/**
 * Created by Tommy Ettinger on 9/12/2019.
 */
public class Visuals {
    public static final float FLOAT_WHITE = Palette.WHITE;
    public static final float FLOAT_BLACK = Palette.BLACK;
    public static final float FLOAT_GRAY = Palette.IRON;
    public static final float FLOAT_NEUTRAL = Palette.GRAY;
    public static final float FLOAT_HOT = Palette.RED;
    public static final float FLOAT_COLD = Palette.AQUAMARINE;
    public static final float FLOAT_LIGHT = Palette.PENCIL_YELLOW;

    /**
     * The "luma" of the given libGDX Color, which is like its lightness, in YCwCm format; ranges from 0f to 1f .
     * @param color a libGDX Color
     * @return the luma as a float from 0.0f to 1.0f
     */
    public static float luma(final Color color)
    {
        return color.r * 0.375f + color.g * 0.5f + color.b * 0.125f;
    }
    /**
     * The "chroma warm" of the given libGDX Color, which when combined with chroma mild describes the shade and
     * saturation of a color, in YCwCm format; ranges from 0f to 1f .
     * @param color a libGDX Color
     * @return the chroma warm as a float from 0f to 1f
     */
    public static float chromaWarm(final Color color)
    {
        return (color.r - color.b) * 0.5f + 0.5f;
    }

    /**
     * The "chroma mild" of the given libGDX Color, which when combined with chroma warm describes the shade and
     * saturation of a color, in YCwCm format; ranges from 0f to 1f .
     * @param color a libGDX Color
     * @return the chroma warm as a float from 0f to 1f
     */
    public static float chromaMild(final Color color)
    {
        return (color.g - color.b) * 0.5f + 0.5f;
    }
    /**
     * Gets a color as a packed float given floats representing luma (Y, akin to lightness), chroma warm (Cw, one of two
     * kinds of chroma used here), chroma mild (Cm, the other kind of chroma), and opacity. Luma should be between 0 and
     * 1, inclusive, with 0 used for very dark colors (almost only black), and 1 used for very light colors (almost only
     * white). The two chroma values range from 0.0 to 1.0, and unlike YCbCr and YCoCg, there's some aesthetic value in
     * changing just one chroma value. When warm is high and mild is low, the color is more reddish; when both are low
     * it is more bluish, and when mild is high and warm is low, the color tends to be greenish, and when both are high
     * it tends to be brown or yellow. When warm and mild are both near 0.5f, the color is closer to gray. The Sat
     * component is a multiplier for the colorfulness of a color this modifies (warm and mild are additive); Sat is
     * doubled before multiplying it by the warm and mild components of a color, so 0.5 Sat is neutral, 0.0 Sat makes a
     * color grayscale, and 1.0 Sat makes a color more vivid, if possible.
     * <br>
     * This method clamps the resulting color's RGB values, so any values can technically be given to this as luma,
     * warm, and mild, but they will only be reversible from the returned float color to the original Y, Cw, and Cm
     * values if the original values were in the range that {@link #chromaWarm(Color)}, {@link #chromaMild(Color)}, and
     * {@link #luma(Color)} return.
     *
     * @param luma       0f to 1f, luma or Y component of YCwCm, with 0.5f meaning "no change" and 1f brightening
     * @param warm       0f to 1f, "chroma warm" or Cw component of YCwCm, with 1f more red or yellow
     * @param mild       0f to 1f, "chroma mild" or Cm component of YCwCm, with 1f more green or yellow
     * @param saturation 0f to 1f, 0f makes the color grayscale and 1f over-saturates it
     * @return a float encoding a color with the given properties
     */
    public static float getYCwCmSat(float luma, float warm, float mild, float saturation) {
        // the color solid should be:

        //                   > warm >
        // blue    violet     red
        // cyan     gray      orange
        // green    neon      yellow
        //  \/ mild \/

        // so, warm is effectively defined as the presence of red.
        // and mild is, effectively, presence of green.
        // negative warm or negative mild will each contribute to blue.
        // luma is defined as (r * 3 + g * 4 + b) / 8
        // or r * 0.375f + g * 0.5f + b * 0.125f
        // warm is the warm-cool axis, with positive warm between red and yellow and negative warm between blue and green
        // warm is defined as (r - b), with range from -1 to 1
        // mild is the green-purple axis, with positive mild between green and yellow, negative mild between blue and red
        // mild is defined as (g - b), with range from -1 to 1

        //r = (warm * 5 - mild * 4 + luma * 8) / 8; r5 - b5 - g4 + b4 + r3 + g4 + b1
        //g = (mild * 4 - warm * 3 + luma * 8) / 8; g4 - b4 - r3 + b3 + r3 + g4 + b1
        //b = (luma * 8 - warm * 3 - mild * 4) / 8; r3 + g4 + b1 - r3 + b3 - g4 + b4
        return NumberTools.intBitsToFloat(((int) (saturation * 255) << 24 & 0xFE000000) | ((int) (mild * 255) << 16 & 0xFF0000)
                | ((int) (warm * 255) << 8 & 0xFF00) | ((int) (luma * 255) & 0xFF));
//        return floatGet(MathUtils.clamp(luma + warm * 0.625f - mild * 0.5f, 0f, 1f),
//                MathUtils.clamp(luma + mild * 0.5f - warm * 0.375f, 0f, 1f),
//                MathUtils.clamp(luma - warm * 0.375f - mild * 0.5f, 0f, 1f), opacity);
    }
    /**
     * Gets a color as a packed float given floats representing luma (Y, akin to lightness), chroma warm (Cw, one of two
     * kinds of chroma used here), chroma mild (Cm, the other kind of chroma), and opacity. Luma should be between 0 and
     * 255, inclusive, with 0 used for very dark colors (almost only black), and 255 used for very light colors (almost
     * only white). The two chroma values range from 0 to 255, and unlike YCbCr and YCoCg, there's some aesthetic value
     * in changing just one chroma value. When warm is high and mild is low, the color is more reddish; when both are
     * low it is more bluish, and when mild is high and warm is low, the color tends to be greenish, and when both are
     * high it tends to be brown or yellow. When warm and mild are both near 128, the color is closer to gray. The Sat
     * component is a multiplier for the colorfulness of a color this modifies (warm and mild are additive); Sat is
     * divided by 128 before multiplying it by the warm and mild components of a color, so 128 Sat is neutral, 0 Sat
     * makes a color grayscale, and 255 Sat makes a color more vivid, if possible.
     * <br>
     * This method clamps the resulting color's RGB values, so any values can technically be given to this as luma,
     * warm, and mild, but they will only be reversible from the returned float color to the original Y, Cw, and Cm
     * values if the original values were in the range that {@link #chromaWarm(Color)}, {@link #chromaMild(Color)}, and
     * {@link #luma(Color)} return.
     *
     * @param luma       0 to 255, luma or Y component of YCwCm, with 128 meaning "no change" and 255 brightening
     * @param warm       0 to 255, "chroma warm" or Cw component of YCwCm, with 255 more red or yellow
     * @param mild       0 to 255, "chroma mild" or Cm component of YCwCm, with 255 more green or yellow
     * @param saturation 0 to 255, 1 makes the color grayscale and 255 over-saturates it
     * @return a float encoding a color with the given properties
     */
    public static float getYCwCmSat(int luma, int warm, int mild, int saturation) {
        return NumberTools.intBitsToFloat((saturation << 24 & 0xFE000000) | (mild << 16 & 0xFF0000)
                | (warm << 8 & 0xFF00) | (luma & 0xFF));
    }

    public static float lerpFloatColors(final float start, final float end, float change) {
        final int s = NumberTools.floatToIntBits(start), e = NumberTools.floatToIntBits(end),
                ys = (s & 0xFF), cws = (s >>> 8) & 0xFF, cms = (s >>> 16) & 0xFF, sas = s >>> 24 & 0xFE,
                ye = (e & 0xFF), cwe = (e >>> 8) & 0xFF, cme = (e >>> 16) & 0xFF, sae = e >>> 24 & 0xFE;
        return NumberTools.intBitsToFloat(((int) (ys + change * (ye - ys)) & 0xFF)
                | (((int) (cws + change * (cwe - cws)) & 0xFF) << 8)
                | (((int) (cms + change * (cme - cms)) & 0xFF) << 16)
                | (((int) (sas + change * (sae - sas)) & 0xFE) << 24));
    }


    /**
     * This is the default vertex shader from libGDX.
     */
    public static final String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "uniform mat4 u_projTrans;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main()\n"
            + "{\n"
            + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
//            + "   v_color.a = v_color.a * (255.0/254.0);\n"
            + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "}\n";
    public static final String fragmentShader =
            "#ifdef GL_ES\n" +
                    "#define LOWP lowp\n" +
                    "precision mediump float;\n" +
                    "#else\n" +
                    "#define LOWP \n" +
                    "#endif\n" +
                    "varying vec2 v_texCoords;\n" +
                    "varying LOWP vec4 v_color;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "uniform sampler2D u_palette;\n" +
                    "const float b_adj = 31.0 / 32.0;\n" +
                    "const float rb_adj = 32.0 / 1023.0;\n" +
                    "const vec3 bright = vec3(0.375, 0.5, 0.125);\n" +
                    "void main()\n" +
                    "{\n" +
                    "   vec4 tgt = texture2D( u_texture, v_texCoords );\n" +
//                    "   vec3 ycc = 2.0 * vec3(v_color.r * dot(tgt.rgb, bright), 0.5 * ((v_color.g - 0.5) + tgt.r - tgt.b), 0.5 * ((v_color.b - 0.5) + tgt.g - tgt.b));\n" +
                    "   vec3 ycc = 2.0 * vec3(v_color.r * dot(tgt.rgb, bright), v_color.a * ((v_color.g - 0.5) + tgt.r - tgt.b), v_color.a * ((v_color.b - 0.5) + tgt.g - tgt.b));\n" +
                    "   tgt.rgb = clamp(vec3(dot(ycc, vec3(1.0, 0.625, -0.5)), dot(ycc, vec3(1.0, -0.375, 0.5)), dot(ycc, vec3(1.0, -0.375, -0.5))), 0.0, 1.0);\n" +
                    "   vec4 used = texture2D(u_palette, vec2((tgt.b * b_adj + floor(tgt.r * 31.999)) * rb_adj, 1.0 - tgt.g));\n" +

                    "   float adj = (fract(52.9829189 * fract(dot(vec2(0.06711056, 0.00583715), gl_FragCoord.xy))) + 0.09375 - fract(dot(vec2(0.7548776662466927, 0.5698402909980532), gl_FragCoord.xy))) * 0.5125;\n" +

//                    "   float adj = (fract(52.9829189 * fract(dot(vec2(0.06711056, 0.00583715), gl_FragCoord.xy))) + 0.125 - fract(dot(vec2(0.7548776662466927, 0.5698402909980532), gl_FragCoord.xy))) * 0.5625;\n" +

//                    "   float adj = asin(fract(52.9829189 * fract(dot(vec2(0.06711056, 0.00583715), gl_FragCoord.xy))) * 0.875 - fract(dot(vec2(0.7548776662466927, 0.5698402909980532), gl_FragCoord.xy)) * 0.5);\n" +

//                    "   float adj = fract(dot(vec2(0.7548776662466927, 0.5698402909980532), gl_FragCoord.xy)) - 0.5;\n" +
//                    "   adj *= acos(adj - 0.25);\n" +

//                    "   float len = ycc.r + 1.5;\n" +
//                    "   float adj = (fract(52.9829189 * fract(dot(vec2(0.06711056, 0.00583715), gl_FragCoord.xy))) - 0.5) * len;\n" +

//                    "   float len = dot(tgt.rgb, bright * 0.0625) + 1.0;\n" +
//                    "   float adj = fract(52.9829189 * fract(dot(vec4(0.06711056, 0.00583715, 0.7548776662466927, 0.5698402909980532), gl_FragCoord.xyyx))) - 0.5;\n" +
//                    "   float len = fract(dot(tgt.rgb, bright) * dot(sin(gl_FragCoord.xy * 5.6789), vec2(14.743036261279236, 13.580412143837574)));\n" +
//                    "   float adj = len * 0.4 - 0.2;\n" +
//                    "   float len = dot(tgt.rgb, bright * 0.0625) + 0.5;\n" +
//                    "   float adj = (fract(52.9829189 * fract(dot(vec2(0.06711056, 0.00583715), sin(gl_FragCoord.xy + len) + cos(gl_FragCoord.yx)))) - 0.5) * len;\n" +

                    "   tgt.rgb += (tgt.rgb - used.rgb) * adj;\n" +
//                    "   ycc = vec3(ycc.r + (ycc.r - dot(bright, used.rgb)) * adj, (ycc.g + used.r - used.b) * 0.5, (ycc.b + used.g - used.b) * 0.5);\n" +
//                    "   tgt.rgb = clamp(vec3(dot(ycc, vec3(1.0, 0.625, -0.5)), dot(ycc, vec3(1.0, -0.375, 0.5)), dot(ycc, vec3(1.0, -0.375, -0.5))), 0.0, 1.0);\n" +
                    "   tgt.rgb = clamp(tgt.rgb, 0.0, 1.0);" +
                    "   gl_FragColor.rgb = texture2D(u_palette, vec2((tgt.b * b_adj + floor(tgt.r * 31.999)) * rb_adj, 1.0 - tgt.g)).rgb;\n" +
                    "   gl_FragColor.a = tgt.a;\n" +
                    "}";
    public static final String fragmentShaderTrue =
            "#ifdef GL_ES\n" +
                    "#define LOWP lowp\n" +
                    "precision mediump float;\n" +
                    "#else\n" +
                    "#define LOWP \n" +
                    "#endif\n" +
                    "varying vec2 v_texCoords;\n" +
                    "varying LOWP vec4 v_color;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "uniform sampler2D u_palette;\n" +
                    "const float b_adj = 31.0 / 32.0;\n" +
                    "const float rb_adj = 32.0 / 1023.0;\n" +
                    "const vec3 bright = vec3(0.375, 0.5, 0.125);\n" +
                    "void main()\n" +
                    "{\n" +
                    "   vec4 tgt = texture2D( u_texture, v_texCoords );\n" +
                    "   vec3 ycc = 2.0 * vec3(v_color.r * dot(tgt.rgb, bright), v_color.a * ((v_color.g - 0.5) + tgt.r - tgt.b), v_color.a * ((v_color.b - 0.5) + tgt.g - tgt.b));\n" +
                    "   gl_FragColor.rgb = clamp(vec3(dot(ycc, vec3(1.0, 0.625, -0.5)), dot(ycc, vec3(1.0, -0.375, 0.5)), dot(ycc, vec3(1.0, -0.375, -0.5))), 0.0, 1.0);\n" +
                    "   gl_FragColor.a = tgt.a;\n" +
                    "}";
    public static final String fragmentShaderWarmMildLimited =
            "#ifdef GL_ES\n" +
                    "#define LOWP lowp\n" +
                    "precision mediump float;\n" +
                    "#else\n" +
                    "#define LOWP \n" +
                    "#endif\n" +
                    "varying vec2 v_texCoords;\n" +
                    "varying LOWP vec4 v_color;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "uniform sampler2D u_palette;\n" +
                    "uniform vec3 u_add;\n" +
                    "uniform vec3 u_mul;\n" +
                    "const float b_adj = 31.0 / 32.0;\n" +
                    "const float rb_adj = 32.0 / 1023.0;\n" +
                    "const vec3 bright = vec3(0.375, 0.5, 0.125);\n" +
                    "void main()\n" +
                    "{\n" +
                    "   vec4 tgt = v_color * texture2D( u_texture, v_texCoords );\n" +
                    "   vec4 used = texture2D(u_palette, vec2((tgt.b * b_adj + floor(tgt.r * 31.999)) * rb_adj, 1.0 - tgt.g));\n" +
                    "   float len = dot(used.rgb, bright) + 1.5;\n" +
                    "   float adj = (fract(52.9829189 * fract(dot(vec2(0.06711056, 0.00583715), gl_FragCoord.xy + len))) - 0.5) * len;\n" +
                    "   tgt.rgb = clamp(tgt.rgb + (tgt.rgb - used.rgb) * adj, 0.0, 1.0);\n" +
                    "   tgt.rgb = u_add + u_mul * vec3(dot(tgt.rgb, bright), tgt.r - tgt.b, tgt.g - tgt.b);\n" +
                    "   tgt.rgb = clamp(vec3(dot(tgt.rgb, vec3(1.0, 0.625, -0.5)), dot(tgt.rgb, vec3(1.0, -0.375, 0.5)), dot(tgt.rgb, vec3(1.0, -0.375, -0.5))), 0.0, 1.0);\n" +
                    "   gl_FragColor.rgb = texture2D(u_palette, vec2((tgt.b * b_adj + floor(tgt.r * 31.999)) * rb_adj, 1.0 - tgt.g)).rgb;\n" +
                    "   gl_FragColor.a = tgt.a;\n" +
                    "}";
}
