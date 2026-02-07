package org.example;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。

import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
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
        window = glfwCreateWindow(800, 600, "My Minecraft (Notch Style)", NULL, NULL);
        if (window == NULL) throw new RuntimeException("窗口创建失败");

        // 设置当前 OpenGL 上下文
        glfwMakeContextCurrent(window);
        GL.createCapabilities(); // 这行非常重要，它连接了 Java 和 OpenGL

        // 设置背景颜色（天空蓝）


        glClearColor(0.5f, 0.8f, 1.0f, 0.0f);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // 清屏
            // 开始画画
            // 开启深度测试，这样近处的方块才会遮挡远处的，否则画面会乱掉
            glEnable(GL_DEPTH_TEST);
            glRotatef(0.5f, 1.0f, 1.0f, 0.0f);
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
            glColor3f(0.0f, 0.8f, 0.0f);
            glVertex3f( 0.5f,  0.5f,  0.5f);
            glVertex3f(-0.5f,  0.5f,  0.5f);
            glVertex3f(-0.5f, -0.5f,  0.5f);
            glVertex3f( 0.5f, -0.5f,  0.5f);

            // ... 为了节省篇幅，你可以先运行这三个面
            glEnd();

            // 交换缓冲区，显示画面
            glfwSwapBuffers(window);
            glfwPollEvents();

        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}