package com.modelviewer.Window;

import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.Input.KeyListener;
import com.modelviewer.Window.Input.MouseListener;
import com.modelviewer.Window.NuklearLayer.NuklearTheme;
import com.modelviewer.Window.NuklearLayer.NuklearLayer;
import com.modelviewer.Window.NuklearLayer.NuklearLayerTheme;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.nuklear.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static com.modelviewer.Utils.IOUtil.ioResourceToByteBuffer;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.NK_FORMAT_COUNT;
import static org.lwjgl.opengl.ARBDebugOutput.*;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glMapBuffer;
import static org.lwjgl.opengl.GL15.glUnmapBuffer;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

abstract public class Window {
    private String title;
    protected int width, height;
    protected long glfwWindow;

    private Vector4f clearColor;
    private int windowFlags;

    private float fieldOfView;
    private float farPlane;
    private float nearPlane;
    protected Matrix4f projectionMatrix;

    private static final int BUFFER_INITIAL_SIZE = 4 * 1024;
    private static final int MAX_VERTEX_BUFFER  = 512 * 1024;
    private static final int MAX_ELEMENT_BUFFER = 128 * 1024;
    private static final NkAllocator ALLOCATOR;
    private final ByteBuffer ttf;
    private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;
    static {
        ALLOCATOR = NkAllocator.create()
                .alloc((handle, old, size) -> nmemAllocChecked(size))
                .mfree((handle, ptr) -> nmemFree(ptr));

        VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
                .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
                .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
                .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
                .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
                .flip();
    }
    private int display_width, display_height;
    protected NkContext ctx          = NkContext.create();
    private NkUserFont default_font = NkUserFont.create();
    private NkBuffer cmds         = NkBuffer.create();
    private NkDrawNullTexture null_texture = NkDrawNullTexture.create();
    private int vbo, vao, ebo;
    private int prog;
    private int vert_shdr;
    private int frag_shdr;
    private int uniform_tex;
    private int uniform_proj;

    protected NuklearLayer info;
    protected boolean isInfoShowing;

    private Callback debugProc;

    public Window(int width, int height, String title) {
        this.height = height;
        this.width = width;

        this.title = title;

        this.clearColor = new Vector4f(0.15f, 0.15f, 0.15f, 1.0f);
        this.windowFlags = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;

        this.fieldOfView = 45;
        this.nearPlane = 0.1f;
        this.farPlane = 100.0f;

        updateProjectionMatrix();

        try {
            this.ttf = ioResourceToByteBuffer("resources/FiraSans-Light.ttf", 1024 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        isInfoShowing = false;
    }


    private void updateProjectionMatrix() {
        float aspectRatio = (float) width / (float) height;
        projectionMatrix = new Matrix4f().perspective(fieldOfView * Constants.TO_RADIAN, aspectRatio, nearPlane, farPlane);
    }

    public void init() {
        // setup error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // init glfw
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to init GLFW.");
        }

        // config glfw
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        //glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // create window
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create GLFW window.");
        }

        // make the context opengl
        glfwMakeContextCurrent(glfwWindow);

        // enable v-sync
        glfwSwapInterval(1);

        // make window visible
        glfwShowWindow(glfwWindow);

        // important line
        GLCapabilities caps      = GL.createCapabilities();

        debugProc = GLUtil.setupDebugMessageCallback();
        if (caps.OpenGL43) {
            GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_API, GL43.GL_DEBUG_TYPE_OTHER, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer)null, false);
        } else if (caps.GL_KHR_debug) {
            KHRDebug.glDebugMessageControl(
                    KHRDebug.GL_DEBUG_SOURCE_API,
                    KHRDebug.GL_DEBUG_TYPE_OTHER,
                    KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                    (IntBuffer)null,
                    false
            );
        } else if (caps.GL_ARB_debug_output) {
            glDebugMessageControlARB(GL_DEBUG_SOURCE_API_ARB, GL_DEBUG_TYPE_OTHER_ARB, GL_DEBUG_SEVERITY_LOW_ARB, (IntBuffer)null, false);
        }

        NkContext ctx = setupWindow(glfwWindow);

        int BITMAP_W = 1024;
        int BITMAP_H = 1024;

        int FONT_HEIGHT = 18;
        int fontTexID   = glGenTextures();

        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTTPackedchar.Buffer cdata    = STBTTPackedchar.create(95);

        float scale;
        float descent;

        try (MemoryStack stack = stackPush()) {
            stbtt_InitFont(fontInfo, ttf);
            scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);

            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0) * scale;

            ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);

            STBTTPackContext pc = STBTTPackContext.malloc(stack);
            stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
            stbtt_PackSetOversampling(pc, 4, 4);
            stbtt_PackFontRange(pc, ttf, 0, FONT_HEIGHT, 32, cdata);
            stbtt_PackEnd(pc);

            // Convert R8 to RGBA8
            ByteBuffer texture = memAlloc(BITMAP_W * BITMAP_H * 4);
            for (int i = 0; i < bitmap.capacity(); i++) {
                texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
            }
            texture.flip();

            glBindTexture(GL_TEXTURE_2D, fontTexID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            memFree(texture);
            memFree(bitmap);
        }

        default_font
                .width((handle, h, text, len) -> {
                    float text_width = 0;
                    try (MemoryStack stack = stackPush()) {
                        IntBuffer unicode = stack.mallocInt(1);

                        int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
                        int text_len  = glyph_len;

                        if (glyph_len == 0) {
                            return 0;
                        }

                        IntBuffer advance = stack.mallocInt(1);
                        while (text_len <= len && glyph_len != 0) {
                            if (unicode.get(0) == NK_UTF_INVALID) {
                                break;
                            }

                            /* query currently drawn glyph information */
                            stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
                            text_width += advance.get(0) * scale;

                            /* offset next glyph */
                            glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
                            text_len += glyph_len;
                        }
                    }
                    return text_width;
                })
                .height(FONT_HEIGHT)
                .query((handle, font_height, glyph, codepoint, next_codepoint) -> {
                    try (MemoryStack stack = stackPush()) {
                        FloatBuffer x = stack.floats(0.0f);
                        FloatBuffer y = stack.floats(0.0f);

                        STBTTAlignedQuad q       = STBTTAlignedQuad.malloc(stack);
                        IntBuffer        advance = stack.mallocInt(1);

                        stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
                        stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);

                        NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);

                        ufg.width(q.x1() - q.x0());
                        ufg.height(q.y1() - q.y0());
                        ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
                        ufg.xadvance(advance.get(0) * scale);
                        ufg.uv(0).set(q.s0(), q.t0());
                        ufg.uv(1).set(q.s1(), q.t1());
                    }
                })
                .texture(it -> it
                        .id(fontTexID));

        nk_style_set_font(ctx, default_font);

        glfwShowWindow(glfwWindow);

        //
        //Enabling Clock Wise Face Culling
        glEnable(GL_CULL_FACE);
        glCullFace(GL_CW);

        // Enabling Depth Test
        glEnable(GL_DEPTH_TEST);
    }

    public void run() {
        init();
        setup();
        mainLoop();
        clear();
    }

    private void setupContext() {
        String NK_SHADER_VERSION = "#version 330\n";
        String vertex_shader =
                NK_SHADER_VERSION +
                        "uniform mat4 ProjMtx;\n" +
                        "in vec2 Position;\n" +
                        "in vec2 TexCoord;\n" +
                        "in vec4 Color;\n" +
                        "out vec2 Frag_UV;\n" +
                        "out vec4 Frag_Color;\n" +
                        "void main() {\n" +
                        "   Frag_UV = TexCoord;\n" +
                        "   Frag_Color = Color;\n" +
                        "   gl_Position = ProjMtx * vec4(Position.xy, 0, 1);\n" +
                        "}\n";
        String fragment_shader =
                NK_SHADER_VERSION +
                        "precision mediump float;\n" +
                        "uniform sampler2D Texture;\n" +
                        "in vec2 Frag_UV;\n" +
                        "in vec4 Frag_Color;\n" +
                        "out vec4 Out_Color;\n" +
                        "void main(){\n" +
                        "   Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n" +
                        "}\n";

        nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);
        prog = glCreateProgram();
        vert_shdr = glCreateShader(GL_VERTEX_SHADER);
        frag_shdr = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(vert_shdr, vertex_shader);
        glShaderSource(frag_shdr, fragment_shader);
        glCompileShader(vert_shdr);
        glCompileShader(frag_shdr);
        if (glGetShaderi(vert_shdr, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new IllegalStateException();
        }
        if (glGetShaderi(frag_shdr, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new IllegalStateException();
        }
        glAttachShader(prog, vert_shdr);
        glAttachShader(prog, frag_shdr);
        glLinkProgram(prog);
        if (glGetProgrami(prog, GL_LINK_STATUS) != GL_TRUE) {
            throw new IllegalStateException();
        }

        uniform_tex = glGetUniformLocation(prog, "Texture");
        uniform_proj = glGetUniformLocation(prog, "ProjMtx");
        int attrib_pos = glGetAttribLocation(prog, "Position");
        int attrib_uv  = glGetAttribLocation(prog, "TexCoord");
        int attrib_col = glGetAttribLocation(prog, "Color");

        {
            // buffer setup
            vbo = glGenBuffers();
            ebo = glGenBuffers();
            vao = glGenVertexArrays();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

            glEnableVertexAttribArray(attrib_pos);
            glEnableVertexAttribArray(attrib_uv);
            glEnableVertexAttribArray(attrib_col);

            glVertexAttribPointer(attrib_pos, 2, GL_FLOAT, false, 20, 0);
            glVertexAttribPointer(attrib_uv, 2, GL_FLOAT, false, 20, 8);
            glVertexAttribPointer(attrib_col, 4, GL_UNSIGNED_BYTE, true, 20, 16);
        }

        {
            // null texture setup
            int nullTexID = glGenTextures();

            null_texture.texture().id(nullTexID);
            null_texture.uv().set(0.5f, 0.5f);

            glBindTexture(GL_TEXTURE_2D, nullTexID);
            try (MemoryStack stack = stackPush()) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
            }
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        }

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private NkContext setupWindow(long win) {
        glfwSetScrollCallback(win, (window, xoffset, yoffset) -> {
            if(MouseListener.getIsAboveMainGlfwWindow()) {
                MouseListener.mouseScrollCallback(window, xoffset, yoffset);
            } else {
                try (MemoryStack stack = stackPush()) {
                    NkVec2 scroll = NkVec2.malloc(stack)
                            .x((float) xoffset)
                            .y((float) yoffset);
                    nk_input_scroll(ctx, scroll);
                }
            }
        });
        glfwSetCharCallback(win, (window, codepoint) -> nk_input_unicode(ctx, codepoint));
        glfwSetKeyCallback(win, (window, key, scancode, action, mods) -> {
            KeyListener.keyCallback(window, key, scancode, action, mods);
            boolean press = action == GLFW_PRESS;
            switch (key) {
                case GLFW_KEY_ESCAPE:
                    glfwSetWindowShouldClose(window, true);
                    break;
                case GLFW_KEY_DELETE:
                    nk_input_key(ctx, NK_KEY_DEL, press);
                    break;
                case GLFW_KEY_ENTER:
                    nk_input_key(ctx, NK_KEY_ENTER, press);
                    break;
                case GLFW_KEY_TAB:
                    nk_input_key(ctx, NK_KEY_TAB, press);
                    break;
                case GLFW_KEY_BACKSPACE:
                    nk_input_key(ctx, NK_KEY_BACKSPACE, press);
                    break;
                case GLFW_KEY_UP:
                    nk_input_key(ctx, NK_KEY_UP, press);
                    break;
                case GLFW_KEY_DOWN:
                    nk_input_key(ctx, NK_KEY_DOWN, press);
                    break;
                case GLFW_KEY_HOME:
                    nk_input_key(ctx, NK_KEY_TEXT_START, press);
                    nk_input_key(ctx, NK_KEY_SCROLL_START, press);
                    break;
                case GLFW_KEY_END:
                    nk_input_key(ctx, NK_KEY_TEXT_END, press);
                    nk_input_key(ctx, NK_KEY_SCROLL_END, press);
                    break;
                case GLFW_KEY_PAGE_DOWN:
                    nk_input_key(ctx, NK_KEY_SCROLL_DOWN, press);
                    break;
                case GLFW_KEY_PAGE_UP:
                    nk_input_key(ctx, NK_KEY_SCROLL_UP, press);
                    break;
                case GLFW_KEY_LEFT_SHIFT:
                case GLFW_KEY_RIGHT_SHIFT:
                    nk_input_key(ctx, NK_KEY_SHIFT, press);
                    break;
                case GLFW_KEY_LEFT_CONTROL:
                case GLFW_KEY_RIGHT_CONTROL:
                    if (press) {
                        nk_input_key(ctx, NK_KEY_COPY, glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_PASTE, glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_CUT, glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_UNDO, glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_REDO, glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_WORD_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_WORD_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_LINE_START, glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_LINE_END, glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS);
                    } else {
                        nk_input_key(ctx, NK_KEY_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_COPY, false);
                        nk_input_key(ctx, NK_KEY_PASTE, false);
                        nk_input_key(ctx, NK_KEY_CUT, false);
                        nk_input_key(ctx, NK_KEY_SHIFT, false);
                    }
                    break;
            }
        });
        glfwSetCursorPosCallback(win, (window, xpos, ypos) -> {
            if(!MouseListener.isDragging()) {
                if (Nuklear.nk_window_is_any_hovered(ctx)) {
                    MouseListener.setIsAboveMainGlfwWindow(false);
                } else {
                    MouseListener.setIsAboveMainGlfwWindow(true);
                }
            }
            MouseListener.mousePosCallback(window, xpos, ypos);
            nk_input_motion(ctx, (int)xpos, (int)ypos);
        });
        glfwSetMouseButtonCallback(win, (window, button, action, mods) -> {
            MouseListener.mouseButtonCallback(window, button, action, mods);

            try (MemoryStack stack = stackPush()) {
                DoubleBuffer cx = stack.mallocDouble(1);
                DoubleBuffer cy = stack.mallocDouble(1);

                glfwGetCursorPos(window, cx, cy);

                int x = (int)cx.get(0);
                int y = (int)cy.get(0);

                int nkButton;
                switch (button) {
                    case GLFW_MOUSE_BUTTON_RIGHT:
                        nkButton = NK_BUTTON_RIGHT;
                        break;
                    case GLFW_MOUSE_BUTTON_MIDDLE:
                        nkButton = NK_BUTTON_MIDDLE;
                        break;
                    default:
                        nkButton = NK_BUTTON_LEFT;
                }
                nk_input_button(ctx, nkButton, x, y, action == GLFW_PRESS);
            }
        });

        nk_init(ctx, ALLOCATOR, null);
        ctx.clip()
                .copy((handle, text, len) -> {
                    if (len == 0) {
                        return;
                    }

                    try (MemoryStack stack = stackPush()) {
                        ByteBuffer str = stack.malloc(len + 1);
                        memCopy(text, memAddress(str), len);
                        str.put(len, (byte)0);

                        glfwSetClipboardString(win, str);
                    }
                })
                .paste((handle, edit) -> {
                    long text = nglfwGetClipboardString(win);
                    if (text != NULL) {
                        nnk_textedit_paste(edit, text, nnk_strlen(text));
                    }
                });

        setupContext();
        return ctx;
    }

    private void newFrame() {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);

        glfwGetWindowSize(glfwWindow, w, h);
        width = w.get(0);
        height = h.get(0);

        glfwGetFramebufferSize(glfwWindow, w, h);
        display_width = w.get(0);
        display_height = h.get(0);

        updateProjectionMatrix();

        nk_input_begin(ctx);
        glfwPollEvents();
    }

    private void render(int AA, int max_vertex_buffer, int max_element_buffer) {
        try (MemoryStack stack = stackPush()) {
            // setup global state
            glEnable(GL_BLEND);
            glBlendEquation(GL_FUNC_ADD);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_SCISSOR_TEST);
            glActiveTexture(GL_TEXTURE0);

            // setup program
            glUseProgram(prog);
            glUniform1i(uniform_tex, 0);
            glUniformMatrix4fv(uniform_proj, false, stack.floats(
                    2.0f / width, 0.0f, 0.0f, 0.0f,
                    0.0f, -2.0f / height, 0.0f, 0.0f,
                    0.0f, 0.0f, -1.0f, 0.0f,
                    -1.0f, 1.0f, 0.0f, 1.0f
            ));
            glViewport(0, 0, display_width, display_height);
        }

        {
            // convert from command queue into draw list and draw to screen

            // allocate vertex and element buffer
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

            glBufferData(GL_ARRAY_BUFFER, max_vertex_buffer, GL_STREAM_DRAW);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, max_element_buffer, GL_STREAM_DRAW);

            // load draw vertices & elements directly into vertex + element buffer
            ByteBuffer vertices = Objects.requireNonNull(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, max_vertex_buffer, null));
            ByteBuffer elements = Objects.requireNonNull(glMapBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_WRITE_ONLY, max_element_buffer, null));
            try (MemoryStack stack = stackPush()) {
                // fill convert configuration
                NkConvertConfig config = NkConvertConfig.calloc(stack)
                        .vertex_layout(VERTEX_LAYOUT)
                        .vertex_size(20)
                        .vertex_alignment(4)
                        .null_texture(null_texture)
                        .circle_segment_count(22)
                        .curve_segment_count(22)
                        .arc_segment_count(22)
                        .global_alpha(1.0f)
                        .shape_AA(AA)
                        .line_AA(AA);

                // setup buffers to load vertices and elements
                NkBuffer vbuf = NkBuffer.malloc(stack);
                NkBuffer ebuf = NkBuffer.malloc(stack);

                nk_buffer_init_fixed(vbuf, vertices/*, max_vertex_buffer*/);
                nk_buffer_init_fixed(ebuf, elements/*, max_element_buffer*/);
                nk_convert(ctx, cmds, vbuf, ebuf, config);
            }
            glUnmapBuffer(GL_ELEMENT_ARRAY_BUFFER);
            glUnmapBuffer(GL_ARRAY_BUFFER);

            // iterate over and execute each draw command
            float fb_scale_x = (float)display_width / (float)width;
            float fb_scale_y = (float)display_height / (float)height;

            long offset = NULL;
            for (NkDrawCommand cmd = nk__draw_begin(ctx, cmds); cmd != null; cmd = nk__draw_next(cmd, cmds, ctx)) {
                if (cmd.elem_count() == 0) {
                    continue;
                }
                glBindTexture(GL_TEXTURE_2D, cmd.texture().id());
                glScissor(
                        (int)(cmd.clip_rect().x() * fb_scale_x),
                        (int)((height - (int)(cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y),
                        (int)(cmd.clip_rect().w() * fb_scale_x),
                        (int)(cmd.clip_rect().h() * fb_scale_y)
                );
                glDrawElements(GL_TRIANGLES, cmd.elem_count(), GL_UNSIGNED_SHORT, offset);
                offset += cmd.elem_count() * 2;
            }
            nk_clear(ctx);
            nk_buffer_clear(cmds);
        }

        // default OpenGL state
        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glDisable(GL_BLEND);
        glDisable(GL_SCISSOR_TEST);
        glEnable(GL_DEPTH_TEST);
    }

    public void mainLoop() {
        float beginTime = 0.0f;
        float endTime = 0.0f;
        float dt = 0.0f;

        float infoWindowWidth = 410, infoWindowHeight = 410;
        NkRect infoRect = Utils.createNkRect((width-infoWindowWidth)/2, (height-infoWindowHeight)/2, infoWindowWidth, infoWindowHeight);
        info = new NuklearLayer(ctx,
                "About",
                infoRect,
                NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_SCALABLE
                );

        while (!glfwWindowShouldClose(glfwWindow)) {
            beginTime = Utils.getTime();
            newFrame();

            infoRect.x((width-infoWindowWidth)/2).y((height-infoWindowHeight)/2).w(infoWindowWidth).h(infoWindowHeight);

            glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
            glClear(windowFlags);

            loop(dt);
            render(NK_ANTI_ALIASING_ON, MAX_VERTEX_BUFFER, MAX_ELEMENT_BUFFER);

            glfwSwapBuffers(glfwWindow);

            if(isInfoShowing) {
                if (info.begin()) {
                    nk_layout_row_dynamic(ctx, 20, 1);
                    String fps = "FPS: " + Integer.toString((int) (1 / dt));
                    nk_text(ctx, fps, NK_TEXT_ALIGN_CENTERED);

                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "3D Model Viewer", NK_TEXT_ALIGN_CENTERED);

                    nk_layout_row_dynamic(ctx, 20, 1);
                    String openglVersion = "OpenGL Version: " + glGetString(GL_VERSION);
                    nk_text(ctx, openglVersion, NK_TEXT_ALIGN_CENTERED);

                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "", NK_TEXT_LEFT);

                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "Apply Theme", NK_TEXT_ALIGN_CENTERED);

                    nk_layout_row_dynamic(ctx, 20, 3);
                    if (nk_option_label(ctx, "Default", NuklearTheme.appliedTheme == NuklearLayerTheme.DEFAULT)) {
                        NuklearTheme.apply(ctx, NuklearLayerTheme.DEFAULT);
                    }
                    if (nk_option_label(ctx, "White", NuklearTheme.appliedTheme == NuklearLayerTheme.WHITE)) {
                        NuklearTheme.apply(ctx, NuklearLayerTheme.WHITE);
                    }
                    if (nk_option_label(ctx, "Red", NuklearTheme.appliedTheme == NuklearLayerTheme.RED)) {
                        NuklearTheme.apply(ctx, NuklearLayerTheme.RED);
                    }
                    nk_layout_row_dynamic(ctx, 20, 3);
                    if (nk_option_label(ctx, "Blue", NuklearTheme.appliedTheme == NuklearLayerTheme.BLUE)) {
                        NuklearTheme.apply(ctx, NuklearLayerTheme.BLUE);
                    }
                    if (nk_option_label(ctx, "Dark", NuklearTheme.appliedTheme == NuklearLayerTheme.DARK)) {
                        NuklearTheme.apply(ctx, NuklearLayerTheme.DARK);
                    }

                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "", NK_TEXT_LEFT);
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "How to Use:", NK_TEXT_LEFT);
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "# Press W, A, S, D, Q, E key for Moving.", NK_TEXT_LEFT);
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "# Hold and Drag Left Mouse Button to Orbit.", NK_TEXT_LEFT);
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "# Hold and Drag Right Mouse Button for FPS style Looking.", NK_TEXT_LEFT);
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "Combine with W, A, S, D for FPS style Moving.", NK_TEXT_LEFT);
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "# Scroll for Zoom.", NK_TEXT_LEFT);
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, "# Press R to reset the Camera.", NK_TEXT_LEFT);
                }
                nk_end(ctx);
                if(MouseListener.isAnyClickOverMainGlfwWindow()) {
                    isInfoShowing = false;
                }
            }

            endTime = Utils.getTime();
            dt = endTime - beginTime;
        }
    }

    public void clear() {
        shutdown();

        // Free memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        if (debugProc != null) {
            debugProc.free();
        }

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void shutdown() {
        Objects.requireNonNull(ctx.clip().copy()).free();
        Objects.requireNonNull(ctx.clip().paste()).free();
        nk_free(ctx);
        destroy();
        Objects.requireNonNull(default_font.query()).free();
        Objects.requireNonNull(default_font.width()).free();
        Objects.requireNonNull(ALLOCATOR.alloc()).free();
        Objects.requireNonNull(ALLOCATOR.mfree()).free();
    }

    private void destroy() {
        glDetachShader(prog, vert_shdr);
        glDetachShader(prog, frag_shdr);
        glDeleteShader(vert_shdr);
        glDeleteShader(frag_shdr);
        glDeleteProgram(prog);
        glDeleteTextures(default_font.texture().id());
        glDeleteTextures(null_texture.texture().id());
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        nk_buffer_free(cmds);
        GL.setCapabilities(null);
    }

    abstract public void loop(float dt);
    abstract public void setup();

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getGlfwWindow() {
        return glfwWindow;
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public float getFarPlane() {
        return farPlane;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWidth(int width) {
        this.width = width;
        updateProjectionMatrix();
    }

    public void setHeight(int height) {
        this.height = height;
        updateProjectionMatrix();
    }

    public void setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
        updateProjectionMatrix();
    }

    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
        updateProjectionMatrix();
    }

    public void setNearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
        updateProjectionMatrix();
    }

    public void setClearColor(Vector4f clearColor) {
        this.clearColor = clearColor;
    }
}