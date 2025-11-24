package com.example.cvgl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CVGLRenderer : GLSurfaceView.Renderer {

    private var textureId = 0
    private var width = 0
    private var height = 0
    private var buffer: ByteBuffer? = null

    // Simple vertex and fragment shaders
    private val vertexShaderCode = """
        attribute vec4 vPosition;
        attribute vec2 vTexCoord;
        varying vec2 texCoord;
        void main() {
            gl_Position = vPosition;
            texCoord = vTexCoord;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        uniform sampler2D uTexture;
        varying vec2 texCoord;
        void main() {
            gl_FragColor = texture2D(uTexture, texCoord);
        }
    """

    private var program = 0

    private val vertices = floatArrayOf(
        // X,   Y,  S,  T
        -1.0f, -1.0f, 0f, 1f, // Bottom Left
         1.0f, -1.0f, 1f, 1f, // Bottom Right
        -1.0f,  1.0f, 0f, 0f, // Top Left
         1.0f,  1.0f, 1f, 0f  // Top Right
    )

    private val vertexBuffer: FloatBuffer

    init {
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        buffer?.let {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width, height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, it)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glUseProgram(program)

            val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
            val texCoordHandle = GLES20.glGetAttribLocation(program, "vTexCoord")

            vertexBuffer.position(0)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)

            vertexBuffer.position(2)
            GLES20.glEnableVertexAttribArray(texCoordHandle)
            GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(texCoordHandle)
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    fun updateTexture(data: ByteArray, width: Int, height: Int) {
        this.width = width
        this.height = height
        this.buffer = ByteBuffer.wrap(data)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}
