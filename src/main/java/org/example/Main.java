package org.example;

import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;

    // 位置与相机
    float cameraX = 0f, cameraY = -5f, cameraZ = -3f;
    float yaw = 0.0f, pitch = 0.0f;
    double lastMouseX = 400, lastMouseY = 300;

    // 物理参数
    float verticalVelocity = 0.0f;
    float gravity = -0.001f;       // 重力强度
    float jumpStrength = 0.3f;    // 跳跃高度
    boolean isGrounded = false;
    float playerHeight = 1.8f;     // 眼睛高度

    public void run() {
        init();
        loop();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("无法初始化 GLFW");

        window = glfwCreateWindow(800, 600, "QuFlow's Minecraft", NULL, NULL);
        if (window == NULL) throw new RuntimeException("窗口创建失败");

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glClearColor(0.5f, 0.8f, 1.0f, 1.0f); // 天空蓝
        glEnable(GL_DEPTH_TEST);

        // 隐藏鼠标
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            handleInput();
            applyPhysics();
            render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void handleInput() {
        // --- 鼠标旋转 ---
        double[] mouseX = new double[1];
        double[] mouseY = new double[1];
        glfwGetCursorPos(window, mouseX, mouseY);

        float deltaX = (float) (mouseX[0] - lastMouseX);
        float deltaY = (float) (mouseY[0] - lastMouseY);
        lastMouseX = mouseX[0];
        lastMouseY = mouseY[0];

        yaw += deltaX * 0.12f;
        pitch += deltaY * 0.12f;
        if (pitch > 89f) pitch = 89f;
        if (pitch < -89f) pitch = -89f;

        // --- 键盘移动与碰撞预判 ---
        float speed = 0.08f;
        float radYaw = (float) Math.toRadians(yaw);
        float nextX = cameraX;
        float nextZ = cameraZ;

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            nextX -= (float) Math.sin(radYaw) * speed;
            nextZ += (float) Math.cos(radYaw) * speed;
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            nextX += (float) Math.sin(radYaw) * speed;
            nextZ -= (float) Math.cos(radYaw) * speed;
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            nextX += (float) Math.cos(radYaw) * speed;
            nextZ += (float) Math.sin(radYaw) * speed;
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            nextX -= (float) Math.cos(radYaw) * speed;
            nextZ -= (float) Math.sin(radYaw) * speed;
        }

        // 侧面碰撞检测 (简易 AABB)
        float pRadius = 0.3f;
        if (!isSolid(-nextX - pRadius, -cameraZ) && !isSolid(-nextX + pRadius, -cameraZ)) {
            cameraX = nextX;
        }
        if (!isSolid(-cameraX, -nextZ - pRadius) && !isSolid(-cameraX, -nextZ + pRadius)) {
            cameraZ = nextZ;
        }

        // 跳跃
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && isGrounded) {
            verticalVelocity = jumpStrength;
            isGrounded = false;
        }
    }

    private void applyPhysics() {
        // 重力应用
        verticalVelocity += gravity;
        cameraY -= verticalVelocity;

        // 地面碰撞检测
        float terrainH = getHeight(-cameraX, -cameraZ);
        float footPos = -cameraY - playerHeight;

        if (footPos <= terrainH) {
            cameraY = -(terrainH + playerHeight);
            verticalVelocity = 0;
            isGrounded = true;
        } else {
            isGrounded = false;
        }
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // --- 投影矩阵 ---
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float fh = (float) Math.tan(Math.toRadians(35.0f)) * 0.1f; // 缩窄点视野更有深度感
        float fw = fh * (800.0f / 600.0f);
        glFrustum(-fw, fw, -fh, fh, 0.1f, 300.0f);

        // --- 视图矩阵 ---
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        glTranslatef(cameraX, cameraY, cameraZ);

        // --- 批量渲染世界 ---
        int renderDist = 40; // 视距
        for (int x = (int)(-cameraX) - renderDist; x < (int)(-cameraX) + renderDist; x++) {
            for (int z = (int)(-cameraZ) - renderDist; z < (int)(-cameraZ) + renderDist; z++) {

                float y = getHeight(x, z);

                glPushMatrix();
                glTranslatef(x, y, z);

                // 简单的海拔着色逻辑
                if (y > 2.0f) glColor3f(1.0f, 1.0f, 1.0f);      // 雪
                else if (y > 0.0f) glColor3f(0.2f, 0.7f, 0.2f); // 草
                else glColor3f(0.4f, 0.3f, 0.1f);               // 泥

                drawCube();
                glPopMatrix();
            }
        }
    }

    private float getHeight(float x, float z) {
        // 正弦波叠加模拟分形地形
        float h = (float) (Math.sin(x * 0.15f) * Math.cos(z * 0.15f) * 4.0f);
        h += (float) (Math.sin(x * 0.4f) * 1.5f); // 增加小起伏
        return h;
    }

    private boolean isSolid(float x, float z) {
        // 如果高度差太大，就认为它是堵墙
        float h = getHeight(x, z);
        float myFootH = -cameraY - playerHeight;
        return (h - myFootH) > 0.6f; // 超过 0.6 个单位的高度差就算撞墙
    }

    private void drawCube() {
        glBegin(GL_QUADS);
        // 顶
        glVertex3f(0.5f, 0.5f, -0.5f); glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f); glVertex3f(0.5f, 0.5f, 0.5f);
        // 底
        glVertex3f(0.5f, -0.5f, 0.5f); glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, -0.5f); glVertex3f(0.5f, -0.5f, -0.5f);
        // 前
        glVertex3f(0.5f, 0.5f, 0.5f); glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f); glVertex3f(0.5f, -0.5f, 0.5f);
        // 后
        glVertex3f(0.5f, -0.5f, -0.5f); glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f); glVertex3f(0.5f, 0.5f, -0.5f);
        // 左
        glVertex3f(-0.5f, 0.5f, -0.5f); glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f); glVertex3f(-0.5f, -0.5f, -0.5f);
        // 右
        glVertex3f(0.5f, 0.5f, 0.5f); glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f); glVertex3f(0.5f, -0.5f, 0.5f);
        glEnd();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}