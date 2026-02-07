package org.example;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。

import org.lwjgl.opengl.*;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    // 定义相机的 Z 位置（初始放在远处看方块）
    float cameraZ = -3.0f;
    float cameraX = 0f;
    float cameraY = -1.5f;

    float yaw = 0.0f;    // 左右转动 (围绕 Y 轴)
    float pitch = 0.0f;  // 上下转动 (围绕 X 轴)
    double lastMouseX = 400, lastMouseY = 300; // 假设窗口中心开始

    public void run() {
        init();
        loop();
        // 释放内存
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("无法初始化 GLFW");

        // 配置窗口
        window = glfwCreateWindow(800, 600, "My Minecraft", NULL, NULL);
        if (window == NULL) throw new RuntimeException("窗口创建失败");

        // 设置当前 OpenGL 上下文
        glfwMakeContextCurrent(window);
        GL.createCapabilities(); // 这行非常重要，它连接了 Java 和 OpenGL

        // 设置背景颜色（天空蓝）
        glClearColor(0.5f, 0.8f, 1.0f, 0.0f);
        // 开启深度测试，这样近处的方块才会遮挡远处的，否则画面会乱掉
        glEnable(GL_DEPTH_TEST);


        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    private void loop() {

        while (!glfwWindowShouldClose(window)) {
            double[] mouseX = new double[1];
            double[] mouseY = new double[1];
            glfwGetCursorPos(window, mouseX, mouseY);

// 计算偏移量
            float deltaX = (float) (mouseX[0] - lastMouseX);
            float deltaY = (float) (mouseY[0] - lastMouseY);
            lastMouseX = mouseX[0];
            lastMouseY = mouseY[0];

// 灵敏度调节
            float sensitivity = 0.1f;
            yaw += deltaX * sensitivity;
            pitch += deltaY * sensitivity;

// 限制仰角，防止“倒立”
            if (pitch > 89.0f) pitch = 89.0f;
            if (pitch < -89.0f) pitch = -89.0f;


            // 控制逻辑
            float speed = 0.05f;
// 将角度转换为弧度，因为 Java 的 Math.sin/cos 只吃弧度
            float radYaw = (float) Math.toRadians(yaw);

// W 键：向前走
            if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
                cameraX -= (float) Math.sin(radYaw) * speed;
                cameraZ += (float) Math.cos(radYaw) * speed;
            }
// S 键：向后走
            if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
                cameraX += (float) Math.sin(radYaw) * speed;
                cameraZ -= (float) Math.cos(radYaw) * speed;
            }
// A 键：向左平移（角度偏移 90 度，即 radYaw + PI/2）
            if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
                cameraX += (float) Math.cos(radYaw) * speed;
                cameraZ += (float) Math.sin(radYaw) * speed;
            }
// D 键：向右平移
            if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
                cameraX -= (float) Math.cos(radYaw) * speed;
                cameraZ -= (float) Math.sin(radYaw) * speed;
            }
            if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS){
                cameraY-= speed;
            }
            if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS){
                cameraY+=speed;
            }
            // --- 第一步：设置投影（眼睛的属性） ---
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            float fh = (float) Math.tan(Math.toRadians(45.0f / 2.0f)) * 0.1f;
            float fw = fh * (800.0f / 600.0f);
            glFrustum(-fw, fw, -fh, fh, 0.1f, 100.0f);

            // --- 第二步：设置视图和模型（身体的位置和姿态） ---
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity(); // 必须在这里清空，开始计算新的帧

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // 【核心线代顺序】：先旋转，后平移
            // 想象一下：你要先告诉显卡你头偏了多少（旋转），再告诉它世界被推开了多少（位移）
            glRotatef(pitch, 1.0f, 0.0f, 0.0f); // 抬头低头
            glRotatef(yaw, 0.0f, 1.0f, 0.0f);   // 左右转头

            // 平移世界：注意这里的 Y 设为 -1.5f 能让你感觉自己“站”在地面上
            glTranslatef(cameraX, cameraY, cameraZ);

            // --- 第三步：开始批量画方块 ---
            for (int x = -5; x < 5; x++) {
                for (int z = -5; z < 5; z++) {
                    glPushMatrix();
                    // 每一个方块相对于当前相机位置进行偏移
                    glTranslatef(x * 1.0f, 0f, z * 1.0f);
                    drawCube();
                    glPopMatrix();
                }
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void drawCube() {
        glBegin(GL_QUADS);
        // --- 顶面 (草地绿) ---
        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f( 0.5f,  0.5f, -0.5f);
        glVertex3f(-0.5f,  0.5f, -0.5f);
        glVertex3f(-0.5f,  0.5f,  0.5f);
        glVertex3f( 0.5f,  0.5f,  0.5f);

        // --- 底面 (泥土棕) ---
        glColor3f(0.5f, 0.35f, 0.05f);
        glVertex3f( 0.5f, -0.5f,  0.5f);
        glVertex3f(-0.5f, -0.5f,  0.5f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f( 0.5f, -0.5f, -0.5f);

        // --- 正面 (深绿) ---
        glColor3f(0.5f, 0.3f, 0.5f);
        glVertex3f( 0.5f,  0.5f,  0.5f);
        glVertex3f(-0.5f,  0.5f,  0.5f);
        glVertex3f(-0.5f, -0.5f,  0.5f);
        glVertex3f( 0.5f, -0.5f,  0.5f);

        //--- 背面
        glColor3f(0.9f, 0.01f, 0.02f);
        glVertex3f( 0.5f,  -0.5f,  -0.5f);
        glVertex3f( -0.5f,  -0.5f,  -0.5f);
        glVertex3f( -0.5f,  0.5f,  -0.5f);
        glVertex3f( 0.5f,  0.5f,  -0.5f);

        //--- 左面
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex3f( -0.5f,  0.5f,  -0.5f);
        glVertex3f(-0.5f,  0.5f,  0.5f);
        glVertex3f(-0.5f, -0.5f,  0.5f);
        glVertex3f(-0.5f, -0.5f, -0.5f);

        //--- 右面
        glColor3f(1.0f, 1.0f, 0f);
        glVertex3f( 0.5f,  0.5f,  0.5f);
        glVertex3f(0.5f,  -0.5f,  0.5f);
        glVertex3f(0.5f,  -0.5f,  -0.5f);
        glVertex3f(0.5f,  0.5f,  -0.5f);
        // ... 为了节省篇幅，你可以先运行这三个面
        glEnd();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}